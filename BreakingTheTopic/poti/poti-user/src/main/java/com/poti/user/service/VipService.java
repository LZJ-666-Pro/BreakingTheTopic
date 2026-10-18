package com.poti.user.service;

import java.util.Map;

/**
 * 会员（VIP）服务：状态查询与开通/续费（模拟支付）
 */
public interface VipService {

    /** 是否 VIP：按 vip_expire_time 动态判断（未过期即 VIP），不依赖定时任务 */
    boolean isVip(Long userId);

    /** 用户端会员状态：{vip, expireTime, price, months, plans}（plans 为三档套餐） */
    Map<String, Object> status(Long userId);

    /**
     * 开通/续费（模拟支付）：生成订单并直接支付成功，
     * 未过期续费顺延 expire = 当前到期 + months*30 天，否则 expire = now + months*30 天
     * @param months 套餐时长（月）：1月卡 / 3季卡 / 12年卡
     * @return {expireTime, amount, orderNo}
     */
    Map<String, Object> purchase(Long userId, int months);

    /**
     * 微信支付下单（统一支付入口）：
     * 微信支付配置就绪时创建待支付订单并返回 wx.requestPayment 参数；
     * 未就绪时自动降级为模拟支付（订单直接成功并开通会员）
     * @return 模拟支付 {mock=true, paid=true, orderNo, expireTime, amount}
     *         真实支付 {mock=false, orderNo, payParams={timeStamp,nonceStr,package,signType,paySign}}
     */
    Map<String, Object> createPayOrder(Long userId, int months);

    /**
     * 微信支付结果回调处理（验签解密由 WechatPayService 完成）：
     * 幂等更新订单为已支付并开通会员（仅 pay_status=0 的订单生效）
     * @return 是否处理成功（true 时微信侧返回成功应答）
     */
    boolean handlePayNotify(String outTradeNo, String transactionId);

    /**
     * 查询支付结果（前端支付完成后的确认补偿）：
     * 本地已支付直接返回；待支付且微信支付就绪时向微信查单对账
     * @return {paid, orderNo, expireTime?}
     */
    Map<String, Object> queryPayResult(Long userId, String orderNo);

    /** internal：会员状态（{vip, expireTime}），供 poti-agent 等服务间调用，不读价格配置 */
    Map<String, Object> internalStatus(Long userId);

    /** internal：题解查看校验——会员无限查看；非会员每日限 5 篇（Redis Set 按题去重计数），供 poti-question 转发调用 */
    Map<String, Object> checkSolutionView(Long userId, Long questionId);

    /** 延长会员时长（签到奖励等内部场景）：未过期顺延 expire + days 天，否则 expire = now + days 天 */
    void extendMembership(Long userId, int days);
}
