package com.poti.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.poti.user.entity.User;
import com.poti.user.entity.VipOrder;
import com.poti.user.mapper.AdminConfigMapper;
import com.poti.user.mapper.UserMapper;
import com.poti.user.mapper.VipOrderMapper;
import com.poti.user.service.VipService;
import com.poti.user.service.WechatPayService;
import com.wechat.pay.java.service.payments.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 会员服务实现：
 * <ul>
 *     <li>是否 VIP 按 vip_expire_time 动态判断（M3），不依赖定时任务</li>
 *     <li>价格读后台 sys_config 的 vip_price（M1，跨库 AdminConfigMapper），未配置缺省 9.9 元/月</li>
 *     <li>当前无支付商户号，模拟支付——点击支付即成功，订单标记 simulated=1（M4）</li>
 * </ul>
 */
@Slf4j
@Service
public class VipServiceImpl implements VipService {

    /** 会员默认价格（sys_config 未配置 vip_price 时的缺省值，元/月） */
    private static final BigDecimal DEFAULT_PRICE = new BigDecimal("1.99");
    /** 季卡/年卡缺省价格（元），正常走 sys_config 的 vip_price_quarter/vip_price_year */
    private static final BigDecimal PRICE_QUARTER = new BigDecimal("4.99");
    private static final BigDecimal PRICE_YEAR = new BigDecimal("9.99");
    /** 一个月对应的顺延天数 */
    private static final int DAYS_PER_MONTH = 30;
    /** 非会员每日题解查看上限（篇），会员无限 */
    private static final int SOLUTION_DAILY_LIMIT = 5;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private VipOrderMapper vipOrderMapper;

    @Autowired
    private AdminConfigMapper adminConfigMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private WechatPayService wechatPayService;

    @Override
    public boolean isVip(Long userId) {
        return isVip(userMapper.selectById(userId));
    }

    @Override
    public Map<String, Object> status(Long userId) {
        User user = userMapper.selectById(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("vip", isVip(user));
        result.put("expireTime", user != null ? user.getVipExpireTime() : null);
        // 三档价格实时读取后台「会员设置」（sys_config），保存后小程序端立即生效
        BigDecimal monthPrice = readPrice("vip_price", DEFAULT_PRICE);
        BigDecimal quarterPrice = readPrice("vip_price_quarter", PRICE_QUARTER);
        BigDecimal yearPrice = readPrice("vip_price_year", PRICE_YEAR);
        result.put("price", monthPrice);
        result.put("months", 1);
        // 三档套餐（对齐会员中心原型：月卡/季卡/年卡），省钱标签按月卡价动态计算
        List<Map<String, Object>> plans = new ArrayList<>();
        plans.add(plan(1, "月卡", monthPrice, "/月", null));
        Integer quarterSave = savePercent(monthPrice, quarterPrice, 3);
        plans.add(plan(3, "季卡", quarterPrice, "/季", quarterSave != null ? "省" + quarterSave + "%" : null));
        Integer yearSave = savePercent(monthPrice, yearPrice, 12);
        plans.add(plan(12, "年卡", yearPrice, "/年", yearSave != null ? "省" + yearSave + "%" : null));
        result.put("plans", plans);
        return result;
    }

    private Map<String, Object> plan(int months, String name, BigDecimal price, String unit, String badge) {
        Map<String, Object> p = new HashMap<>();
        p.put("months", months);
        p.put("name", name);
        p.put("price", price);
        p.put("unit", unit);
        p.put("badge", badge);
        return p;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> purchase(Long userId, int months) {
        if (months != 1 && months != 3 && months != 12) {
            throw new IllegalArgumentException("套餐时长非法，仅支持 1/3/12 个月");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        // 模拟支付（M2）：无真实商户号，点击支付即成功；接入真实支付后替换此段下单/回调逻辑
        BigDecimal amount = months == 1 ? readPrice("vip_price", DEFAULT_PRICE)
                : (months == 3 ? readPrice("vip_price_quarter", PRICE_QUARTER) : readPrice("vip_price_year", PRICE_YEAR));
        LocalDateTime now = LocalDateTime.now();

        VipOrder order = new VipOrder();
        order.setUserId(userId);
        order.setOrderNo("VIP" + now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + ThreadLocalRandom.current().nextInt(100000, 1000000));
        order.setAmount(amount);
        order.setMonths(months);
        order.setPayStatus(1);      // 支付成功
        order.setSimulated(1);      // 模拟支付标记
        order.setPayTime(now);
        vipOrderMapper.insert(order);
        applyMembership(userId, months * DAYS_PER_MONTH);

        LocalDateTime expireTime = userMapper.selectById(userId).getVipExpireTime();
        log.info("会员开通成功（模拟支付）: userId={}, orderNo={}, amount={}, expireTime={}",
                userId, order.getOrderNo(), amount, expireTime);

        Map<String, Object> result = new HashMap<>();
        result.put("expireTime", expireTime);
        result.put("amount", amount);
        result.put("orderNo", order.getOrderNo());
        return result;
    }

    @Override
    public Map<String, Object> createPayOrder(Long userId, int months) {
        if (months != 1 && months != 3 && months != 12) {
            throw new IllegalArgumentException("套餐时长非法，仅支持 1/3/12 个月");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        BigDecimal amount = months == 1 ? readPrice("vip_price", DEFAULT_PRICE)
                : (months == 3 ? readPrice("vip_price_quarter", PRICE_QUARTER) : readPrice("vip_price_year", PRICE_YEAR));
        LocalDateTime now = LocalDateTime.now();

        VipOrder order = new VipOrder();
        order.setUserId(userId);
        order.setOrderNo("VIP" + now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + ThreadLocalRandom.current().nextInt(100000, 1000000));
        order.setAmount(amount);
        order.setMonths(months);
        order.setPayStatus(0);      // 待支付
        order.setSimulated(0);
        vipOrderMapper.insert(order);

        // 微信支付未就绪：自动降级模拟支付，订单即时成功并开通会员
        if (!wechatPayService.ready()) {
            markOrderPaid(order, "MOCK" + order.getOrderNo());
            applyMembership(userId, months * DAYS_PER_MONTH);
            LocalDateTime expireTime = userMapper.selectById(userId).getVipExpireTime();
            log.info("会员开通成功（模拟支付，微信支付未配置）: userId={}, orderNo={}, amount={}", userId, order.getOrderNo(), amount);
            Map<String, Object> result = new HashMap<>();
            result.put("mock", true);
            result.put("paid", true);
            result.put("orderNo", order.getOrderNo());
            result.put("amount", amount);
            result.put("expireTime", expireTime);
            return result;
        }

        // 真实支付：JSAPI 下单需要 openid（微信登录链路写入）
        if (user.getOpenid() == null || user.getOpenid().trim().isEmpty()) {
            throw new IllegalArgumentException("当前账号未绑定微信，请使用微信登录后再支付");
        }
        int amountFen = amount.multiply(new BigDecimal(100)).setScale(0, java.math.RoundingMode.HALF_UP).intValue();
        Map<String, String> payParams = wechatPayService.createJsapiOrder(order.getOrderNo(), amountFen, user.getOpenid(), "破题刷题会员" + months + "个月");
        Map<String, Object> result = new HashMap<>();
        result.put("mock", false);
        result.put("paid", false);
        result.put("orderNo", order.getOrderNo());
        result.put("amount", amount);
        result.put("payParams", payParams);
        return result;
    }

    @Override
    public boolean handlePayNotify(String outTradeNo, String transactionId) {
        VipOrder order = vipOrderMapper.selectOne(new LambdaQueryWrapper<VipOrder>()
                .eq(VipOrder::getOrderNo, outTradeNo));
        if (order == null) {
            log.warn("微信支付回调订单不存在: orderNo={}", outTradeNo);
            return false;
        }
        // 幂等：仅待支付订单处理，重复通知直接返回成功
        if (order.getPayStatus() != null && order.getPayStatus() == 1) {
            return true;
        }
        markOrderPaid(order, transactionId);
        applyMembership(order.getUserId(), order.getMonths() * DAYS_PER_MONTH);
        log.info("微信支付成功，会员开通: userId={}, orderNo={}, transactionId={}, months={}",
                order.getUserId(), outTradeNo, transactionId, order.getMonths());
        return true;
    }

    @Override
    public Map<String, Object> queryPayResult(Long userId, String orderNo) {
        Map<String, Object> result = new HashMap<>();
        result.put("orderNo", orderNo);
        VipOrder order = vipOrderMapper.selectOne(new LambdaQueryWrapper<VipOrder>()
                .eq(VipOrder::getOrderNo, orderNo)
                .eq(VipOrder::getUserId, userId));
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (order.getPayStatus() != null && order.getPayStatus() == 1) {
            result.put("paid", true);
            User user = userMapper.selectById(userId);
            result.put("expireTime", user != null ? user.getVipExpireTime() : null);
            return result;
        }
        // 待支付且微信支付就绪：主动向微信查单对账（回调丢失/延迟的补偿）
        if (wechatPayService.ready()) {
            try {
                Transaction transaction = wechatPayService.queryTransaction(orderNo);
                if (transaction != null && transaction.getTradeState() == Transaction.TradeStateEnum.SUCCESS) {
                    markOrderPaid(order, transaction.getTransactionId());
                    applyMembership(userId, order.getMonths() * DAYS_PER_MONTH);
                    log.info("微信查单确认支付成功: userId={}, orderNo={}", userId, orderNo);
                    result.put("paid", true);
                    User user = userMapper.selectById(userId);
                    result.put("expireTime", user != null ? user.getVipExpireTime() : null);
                    return result;
                }
            } catch (Exception e) {
                log.warn("微信查单失败（按未支付处理）: orderNo={}", orderNo, e);
            }
        }
        result.put("paid", false);
        return result;
    }

    /** 订单置为已支付并记录微信交易号（真实支付）；simulated 标记保持区分支付渠道 */
    private void markOrderPaid(VipOrder order, String transactionId) {
        order.setPayStatus(1);
        order.setPayTime(LocalDateTime.now());
        order.setTransactionId(transactionId);
        vipOrderMapper.updateById(order);
    }

    /** 会员权益应用：未过期续费顺延，否则自当前时间起算（数据库端原子计算，避免并发购买基于过期读到旧值） */
    private void applyMembership(Long userId, int days) {
        userMapper.update(null, new LambdaUpdateWrapper<User>()
                .eq(User::getId, userId)
                .set(User::getVipLevel, 1)
                .setSql("vip_expire_time = CASE WHEN vip_expire_time IS NOT NULL AND vip_expire_time > NOW() " +
                        "THEN DATE_ADD(vip_expire_time, INTERVAL " + days + " DAY) " +
                        "ELSE DATE_ADD(NOW(), INTERVAL " + days + " DAY) END"));
    }

    @Override
    public Map<String, Object> internalStatus(Long userId) {
        User user = userMapper.selectById(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("vip", isVip(user));
        result.put("expireTime", user != null ? user.getVipExpireTime() : null);
        return result;
    }

    /** 是否 VIP：到期时间非空且晚于当前时间即为有效会员 */
    private boolean isVip(User user) {
        return user != null && user.getVipExpireTime() != null
                && user.getVipExpireTime().isAfter(LocalDateTime.now());
    }

    /** 读取后台配置的会员价格（sys_config，后台「会员设置」可改，保存后实时生效），未配置、非法或非正数时按缺省处理 */
    private BigDecimal readPrice(String key, BigDecimal defaultValue) {
        String value = adminConfigMapper.selectValueByKey(key);
        if (value != null && !value.trim().isEmpty()) {
            try {
                BigDecimal price = new BigDecimal(value.trim());
                if (price.compareTo(BigDecimal.ZERO) > 0) {
                    return price;
                }
                log.warn("{} 配置为非正数，按缺省价格处理: {}", key, value);
            } catch (NumberFormatException e) {
                log.warn("{} 配置非法，按缺省价格处理: {}", key, value);
            }
        }
        return defaultValue;
    }

    /** 计算相对月卡的省钱百分比，向下取整（如 16 表示省16%）；月卡价格非正时返回 null 不展示 */
    private Integer savePercent(BigDecimal monthlyPrice, BigDecimal planPrice, int months) {
        if (monthlyPrice == null || planPrice == null
                || monthlyPrice.compareTo(BigDecimal.ZERO) <= 0 || planPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        BigDecimal full = monthlyPrice.multiply(new BigDecimal(months));
        if (full.compareTo(planPrice) <= 0) {
            return null;
        }
        BigDecimal percent = full.subtract(planPrice)
                .multiply(new BigDecimal(100))
                .divide(full, 0, java.math.RoundingMode.DOWN);
        return percent.intValue();
    }

    @Override
    public Map<String, Object> checkSolutionView(Long userId, Long questionId) {
        Map<String, Object> result = new HashMap<>();
        // 会员：官方题解无限查看
        if (isVip(userId)) {
            result.put("allow", true);
            result.put("vip", true);
            return result;
        }
        // 非会员：每日 5 篇——Redis Set 按题去重（同题重复看不计），当天首键 2 天兜底过期
        String viewedKey = "solution:viewed:" + userId + ":" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        if (questionId != null && Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember(viewedKey, String.valueOf(questionId)))) {
            // 已看过的题重复查看，不消耗额度
            long viewed = redisSize(viewedKey);
            result.put("allow", true);
            result.put("vip", false);
            result.put("used", viewed);
            result.put("limit", SOLUTION_DAILY_LIMIT);
            return result;
        }
        long viewed = redisSize(viewedKey);
        if (viewed >= SOLUTION_DAILY_LIMIT) {
            result.put("allow", false);
            result.put("vip", false);
            result.put("used", viewed);
            result.put("limit", SOLUTION_DAILY_LIMIT);
            return result;
        }
        if (questionId != null) {
            stringRedisTemplate.opsForSet().add(viewedKey, String.valueOf(questionId));
            stringRedisTemplate.expire(viewedKey, Duration.ofDays(2));
            viewed++;
        }
        result.put("allow", true);
        result.put("vip", false);
        result.put("used", viewed);
        result.put("limit", SOLUTION_DAILY_LIMIT);
        return result;
    }

    /** Redis Set 大小（连接异常等返回 0，由上层降级放行） */
    private long redisSize(String key) {
        Long size = stringRedisTemplate.opsForSet().size(key);
        return size == null ? 0 : size;
    }
}
