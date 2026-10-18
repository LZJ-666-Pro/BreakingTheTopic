package com.poti.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.poti.admin.entity.User;
import com.poti.admin.entity.VipOrder;
import com.poti.admin.mapper.UserMapper;
import com.poti.admin.mapper.VipOrderMapper;
import com.poti.common.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 会员管理接口（后台）。
 * 订单与用户数据均在 poti_user 库，通过 dynamic-datasource @DS("user") 直查。
 */
@RestController
@RequestMapping("/admin/vip")
@RequiredArgsConstructor
public class VipAdminController {

    private final VipOrderMapper vipOrderMapper;
    private final UserMapper userMapper;

    /**
     * 会员订单分页（按下单时间倒序），支持昵称模糊筛选。
     * 昵称筛选：先模糊查 user id 列表，再 in 订单表。
     */
    @GetMapping("/order/page")
    public R<Map<String, Object>> orderPage(@RequestParam(defaultValue = "1") long pageNum,
                                            @RequestParam(defaultValue = "10") long pageSize,
                                            @RequestParam(required = false) String nickname) {
        LambdaQueryWrapper<VipOrder> wrapper = new LambdaQueryWrapper<>();
        if (nickname != null && !nickname.isBlank()) {
            List<Long> userIds = userIdsByNickname(nickname.trim());
            if (userIds.isEmpty()) {
                // 昵称无匹配用户，直接返回空页
                return R.success(pageData(new ArrayList<>(), 0L));
            }
            wrapper.in(VipOrder::getUserId, userIds);
        }
        wrapper.orderByDesc(VipOrder::getId);
        Page<VipOrder> page = vipOrderMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        // 批量回填昵称
        List<Long> userIds = page.getRecords().stream().map(VipOrder::getUserId).distinct().toList();
        Map<Long, User> userMap = loadUsers(userIds);

        List<Map<String, Object>> list = new ArrayList<>();
        for (VipOrder o : page.getRecords()) {
            User u = userMap.get(o.getUserId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", o.getId());
            item.put("orderNo", o.getOrderNo());
            item.put("userId", o.getUserId());
            item.put("nickname", u != null ? u.getNickname() : null);
            item.put("amount", o.getAmount());
            item.put("months", o.getMonths());
            item.put("payStatus", o.getPayStatus());
            item.put("simulated", o.getSimulated());
            item.put("createTime", o.getCreateTime());
            item.put("payTime", o.getPayTime());
            list.add(item);
        }
        return R.success(pageData(list, page.getTotal()));
    }

    /**
     * 会员用户分页：user 表 where vip_expire_time > now（当前有效会员）。
     * 附带累计消费（vip_order 成功订单金额合计）。
     */
    @GetMapping("/user/page")
    public R<Map<String, Object>> userPage(@RequestParam(defaultValue = "1") long pageNum,
                                           @RequestParam(defaultValue = "10") long pageSize) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.gt(User::getVipExpireTime, LocalDateTime.now())
                .orderByDesc(User::getVipExpireTime);
        Page<User> page = userMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        // 累计消费批量统计
        List<Long> ids = page.getRecords().stream().map(User::getId).toList();
        Map<Long, BigDecimal> amounts = new HashMap<>();
        if (!ids.isEmpty()) {
            for (Map<String, Object> row : vipOrderMapper.selectTotalAmountBatch(ids)) {
                amounts.put(((Number) row.get("userId")).longValue(),
                        new BigDecimal(String.valueOf(row.get("totalAmount"))));
            }
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (User u : page.getRecords()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", u.getId());
            item.put("nickname", u.getNickname());
            item.put("avatarUrl", u.getAvatarUrl());
            item.put("vipExpireTime", u.getVipExpireTime());
            item.put("totalAmount", amounts.getOrDefault(u.getId(), BigDecimal.ZERO));
            list.add(item);
        }
        return R.success(pageData(list, page.getTotal()));
    }

    /**
     * 删除订单（完整闭环）：删除订单记录后，按该用户剩余已支付订单重算会员权益——
     * 被删订单赋予的会员时长随之回收，避免「订单已删、会员仍在」的数据不一致。
     */
    @DeleteMapping("/order/{id}")
    public R<Map<String, Object>> deleteOrder(@PathVariable Long id) {
        VipOrder order = vipOrderMapper.selectById(id);
        if (order == null) {
            return R.error("订单不存在或已被删除");
        }
        Long userId = order.getUserId();
        vipOrderMapper.deleteById(id);
        LocalDateTime expire = recalcVipExpire(userId);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userId", userId);
        data.put("vipExpireTime", expire);   // null 表示会员已全部回收
        return R.success(data);
    }

    /**
     * 重算用户会员到期时间：剩余已支付订单按支付时间升序逐单顺延
     * （每单 months*30 天，与开通逻辑一致，未过期部分跨单叠加）；
     * 无剩余订单则回收会员（level=0、到期时间置空）。
     */
    private LocalDateTime recalcVipExpire(Long userId) {
        List<VipOrder> paidOrders = vipOrderMapper.selectList(new LambdaQueryWrapper<VipOrder>()
                .eq(VipOrder::getUserId, userId)
                .eq(VipOrder::getPayStatus, 1)
                .orderByAsc(VipOrder::getPayTime));
        LocalDateTime expire = null;
        for (VipOrder o : paidOrders) {
            LocalDateTime base = o.getPayTime();
            if (expire != null && expire.isAfter(base)) {
                base = expire;   // 未过期续费：自当前到期顺延
            }
            expire = base.plusDays(o.getMonths() * 30L);
        }
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setVipExpireTime(expire);
            user.setVipLevel(expire != null && expire.isAfter(LocalDateTime.now()) ? 1 : 0);
            userMapper.updateById(user);
        }
        return expire;
    }

    /**
     * 移除会员身份（完整闭环）：回收该用户会员权益（level=0、到期时间清空，立即掉出会员列表）。
     * 不删除用户账号，历史订单记录保留可追溯。
     */
    @DeleteMapping("/user/{id}")
    public R<Void> removeVipUser(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return R.error("用户不存在");
        }
        // updateById 默认忽略 null 字段，置空到期时间必须显式 set
        userMapper.update(null, new LambdaUpdateWrapper<User>()
                .eq(User::getId, id)
                .set(User::getVipLevel, 0)
                .set(User::getVipExpireTime, null));
        return R.success(null);
    }

    /** 昵称模糊查用户ID列表（poti_user 库） */
    private List<Long> userIdsByNickname(String nickname) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(User::getNickname, nickname).select(User::getId);
        return userMapper.selectList(wrapper).stream().map(User::getId).toList();
    }

    /** 批量查用户（poti_user 库） */
    private Map<Long, User> loadUsers(List<Long> userIds) {
        Map<Long, User> map = new HashMap<>();
        if (userIds == null || userIds.isEmpty()) {
            return map;
        }
        for (User u : userMapper.selectBatchIds(userIds)) {
            map.put(u.getId(), u);
        }
        return map;
    }

    private Map<String, Object> pageData(List<?> list, long total) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("list", list);
        data.put("total", total);
        return data;
    }
}
