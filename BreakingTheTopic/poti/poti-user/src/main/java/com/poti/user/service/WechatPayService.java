package com.poti.user.service;

import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import com.wechat.pay.java.service.payments.jsapi.model.QueryOrderByOutTradeNoRequest;
import com.wechat.pay.java.service.payments.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信支付 V3 JSAPI 封装（官方 wechatpay-java SDK）。
 * <p>
 * 配置 wx.pay.enabled=true 且商户参数齐全时启用真实支付；
 * 任一条件不满足时 ready()=false，会员开通自动降级为模拟支付。
 */
@Slf4j
@Service
public class WechatPayService {

    @Value("${wx.appid}")
    private String appid;

    @Value("${wx.pay.enabled:false}")
    private boolean enabled;

    @Value("${wx.pay.mch-id:}")
    private String mchId;

    @Value("${wx.pay.mch-serial-no:}")
    private String mchSerialNo;

    @Value("${wx.pay.api-v3-key:}")
    private String apiV3Key;

    @Value("${wx.pay.private-key-path:}")
    private String privateKeyPath;

    @Value("${wx.pay.notify-url:}")
    private String notifyUrl;

    /** SDK 客户端懒加载（首次下单/查单时初始化并自动下载微信平台证书） */
    private volatile JsapiServiceExtension jsapiService;
    private volatile RSAAutoCertificateConfig config;

    /** 微信支付是否可用：开关开启且商户五参数齐全 */
    public boolean ready() {
        return enabled && notBlank(mchId) && notBlank(mchSerialNo)
                && notBlank(apiV3Key) && notBlank(privateKeyPath) && notBlank(notifyUrl);
    }

    /**
     * JSAPI 下单，返回小程序 wx.requestPayment 所需参数
     * （timeStamp/nonceStr/package/signType/paySign）
     */
    public Map<String, String> createJsapiOrder(String orderNo, int amountFen, String openid, String description) {
        initIfNeeded();
        PrepayRequest request = new PrepayRequest();
        request.setAppid(appid);
        request.setMchid(mchId);
        request.setDescription(description);
        request.setOutTradeNo(orderNo);
        request.setNotifyUrl(notifyUrl);
        Amount amount = new Amount();
        amount.setTotal(amountFen);
        amount.setCurrency("CNY");
        request.setAmount(amount);
        Payer payer = new Payer();
        payer.setOpenid(openid);
        request.setPayer(payer);

        // SDK 自动完成 prepay_id 获取与二次签名
        PrepayWithRequestPaymentResponse payment = jsapiService.prepayWithRequestPayment(request);
        Map<String, String> result = new HashMap<>();
        result.put("timeStamp", payment.getTimeStamp());
        result.put("nonceStr", payment.getNonceStr());
        result.put("package", payment.getPackageVal());
        result.put("signType", payment.getSignType());
        result.put("paySign", payment.getPaySign());
        return result;
    }

    /**
     * 验签并解密微信支付结果回调。
     * 返回 {outTradeNo, transactionId, tradeState}；验签失败抛 SDK 异常由调用方拒绝。
     */
    public Map<String, Object> parseNotify(String body, String serial, String nonce, String signature, String timestamp) {
        initIfNeeded();
        RequestParam requestParam = new RequestParam.Builder()
                .serialNumber(serial)
                .nonce(nonce)
                .signature(signature)
                .timestamp(timestamp)
                .body(body)
                .build();
        NotificationParser parser = new NotificationParser(config);
        Transaction transaction = parser.parse(requestParam, Transaction.class);
        Map<String, Object> result = new HashMap<>();
        result.put("outTradeNo", transaction.getOutTradeNo());
        result.put("transactionId", transaction.getTransactionId());
        result.put("tradeState", transaction.getTradeState() == null ? null : transaction.getTradeState().name());
        return result;
    }

    /** 商户订单号主动查单（支付结果补偿），订单不存在时抛 SDK 异常 */
    public Transaction queryTransaction(String orderNo) {
        initIfNeeded();
        QueryOrderByOutTradeNoRequest request = new QueryOrderByOutTradeNoRequest();
        request.setMchid(mchId);
        request.setOutTradeNo(orderNo);
        return jsapiService.queryOrderByOutTradeNo(request);
    }

    private synchronized void initIfNeeded() {
        if (jsapiService != null) {
            return;
        }
        config = new RSAAutoCertificateConfig.Builder()
                .merchantId(mchId)
                .privateKeyFromPath(privateKeyPath)
                .merchantSerialNumber(mchSerialNo)
                .apiV3Key(apiV3Key)
                .build();
        jsapiService = new JsapiServiceExtension.Builder().config(config).build();
        log.info("微信支付 V3 客户端初始化成功: mchId={}, notifyUrl={}", mchId, notifyUrl);
    }

    private boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
