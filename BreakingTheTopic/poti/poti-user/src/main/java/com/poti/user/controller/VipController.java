package com.poti.user.controller;

import com.poti.common.R;
import com.poti.user.service.VipService;
import com.poti.user.service.WechatPayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 会员（VIP）：状态查询 / 开通续费（模拟支付）
 * <p>
 * 用户身份统一取自鉴权过滤器注入的 X-User-Id（由 JWT 解析而来，无法伪造），不信任前端传参；
 * /vip/internal/** 服务间接口由内部令牌（X-Internal-Token）放行，供 poti-agent 等调用。
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/vip")
public class VipController {

    @Autowired
    private VipService vipService;

    @Autowired
    private WechatPayService wechatPayService;

    /** 用户端：会员状态（是否VIP/到期时间/价格/时长） */
    @GetMapping("/status")
    public R<Map<String, Object>> status(@RequestHeader("X-User-Id") Long userId) {
        try {
            return R.success(vipService.status(userId));
        } catch (Exception e) {
            log.error("获取会员状态失败: userId={}", userId, e);
            return R.error("获取会员状态失败: " + e.getMessage());
        }
    }

    /** 用户端：开通/续费（当前无支付商户号，模拟支付——点击支付即成功；months: 1月卡/3季卡/12年卡） */
    @PostMapping("/purchase")
    public R<Map<String, Object>> purchase(@RequestHeader("X-User-Id") Long userId,
                                           @RequestBody(required = false) Map<String, Object> body) {
        try {
            int months = 1;
            if (body != null && body.get("months") != null) {
                months = Integer.parseInt(String.valueOf(body.get("months")));
            }
            return R.success(vipService.purchase(userId, months));
        } catch (IllegalArgumentException e) {
            return R.error(e.getMessage());
        } catch (Exception e) {
            log.error("会员开通失败: userId={}", userId, e);
            return R.error("会员开通失败: " + e.getMessage());
        }
    }

    /** 用户端：微信支付下单（统一支付入口）——微信支付就绪返回 wx.requestPayment 参数，未就绪降级模拟支付 */
    @PostMapping("/pay/create")
    public R<Map<String, Object>> payCreate(@RequestHeader("X-User-Id") Long userId,
                                            @RequestBody(required = false) Map<String, Object> body) {
        try {
            int months = 1;
            if (body != null && body.get("months") != null) {
                months = Integer.parseInt(String.valueOf(body.get("months")));
            }
            return R.success(vipService.createPayOrder(userId, months));
        } catch (IllegalArgumentException e) {
            return R.error(e.getMessage());
        } catch (Exception e) {
            log.error("微信支付下单失败: userId={}", userId, e);
            return R.error("支付下单失败: " + e.getMessage());
        }
    }

    /**
     * 微信支付结果回调（微信服务器调用，无 JWT——由服务过滤器白名单放行，签名验证在本接口完成）。
     * 按微信规范返回 JSON：成功 {"code":"SUCCESS"}；验签/处理失败返回 500 {"code":"FAIL"} 让微信重试。
     */
    @PostMapping("/pay/notify")
    public Map<String, String> payNotify(@RequestBody String body,
                                         @RequestHeader(value = "Wechatpay-Serial", required = false) String serial,
                                         @RequestHeader(value = "Wechatpay-Nonce", required = false) String nonce,
                                         @RequestHeader(value = "Wechatpay-Signature", required = false) String signature,
                                         @RequestHeader(value = "Wechatpay-Timestamp", required = false) String timestamp) {
        Map<String, String> success = new HashMap<>();
        success.put("code", "SUCCESS");
        success.put("message", "成功");
        Map<String, String> fail = new HashMap<>();
        fail.put("code", "FAIL");
        try {
            Map<String, Object> result = wechatPayService.parseNotify(body, serial, nonce, signature, timestamp);
            String outTradeNo = String.valueOf(result.get("outTradeNo"));
            String tradeState = String.valueOf(result.get("tradeState"));
            if (!"SUCCESS".equals(tradeState)) {
                log.info("微信支付回调非成功状态: orderNo={}, tradeState={}", outTradeNo, tradeState);
                return success;   // 非成功状态确认收到即可，无需重试
            }
            boolean handled = vipService.handlePayNotify(outTradeNo, String.valueOf(result.get("transactionId")));
            return handled ? success : fail;
        } catch (Exception e) {
            log.error("微信支付回调验签/处理失败", e);
            fail.put("message", "验签或处理失败");
            return fail;
        }
    }

    /** 用户端：查询支付结果（wx.requestPayment 成功后的确认补偿，支持轮询） */
    @GetMapping("/pay/query")
    public R<Map<String, Object>> payQuery(@RequestHeader("X-User-Id") Long userId,
                                           @RequestParam("orderNo") String orderNo) {
        try {
            return R.success(vipService.queryPayResult(userId, orderNo));
        } catch (IllegalArgumentException e) {
            return R.error(e.getMessage());
        } catch (Exception e) {
            log.error("查询支付结果失败: userId={}, orderNo={}", userId, orderNo, e);
            return R.error("查询支付结果失败");
        }
    }

    /** internal：会员状态查询（poti-agent 每日提问限额联动等，userId 由调用方显式指定） */
    @GetMapping("/internal/status")
    public R<Map<String, Object>> internalStatus(@RequestParam("userId") Long userId) {
        try {
            return R.success(vipService.internalStatus(userId));
        } catch (Exception e) {
            log.error("internal 获取会员状态失败: userId={}", userId, e);
            return R.error("获取会员状态失败: " + e.getMessage());
        }
    }

    /** internal：题解查看校验（poti-question 转发调用）——会员无限查看，非会员每日 5 篇；校验依赖异常时降级放行 */
    @GetMapping("/internal/check-solution")
    public R<Map<String, Object>> internalCheckSolution(@RequestParam("userId") Long userId,
                                                        @RequestParam(value = "questionId", required = false) Long questionId) {
        try {
            return R.success(vipService.checkSolutionView(userId, questionId));
        } catch (Exception e) {
            log.error("internal 题解查看校验失败: userId={}", userId, e);
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("allow", true);
            fallback.put("vip", false);
            return R.success(fallback);
        }
    }
}
