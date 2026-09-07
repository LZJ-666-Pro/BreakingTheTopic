package com.poti.user.controller;

import com.poti.common.R;
import com.poti.user.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/sms")
public class SmsController {

    @Autowired
    private SmsService smsService;

    @PostMapping("/send")
    public R<Map<String, Object>> sendVerifyCode(@RequestBody Map<String, String> request) {
        try {
            String phone = request.get("phone");
            
            log.info("发送验证码请求，phone: {}", phone);
            
            if (phone == null || phone.trim().isEmpty()) {
                return R.error("手机号不能为空");
            }
            
            if (!phone.matches("^1[3-9]\\d{9}$")) {
                return R.error("手机号格式不正确");
            }
            
            if (!smsService.canSendCode(phone)) {
                long remaining = smsService.getRemainingCooldown(phone);
                return R.error("发送频率过高，请" + remaining + "秒后再试");
            }
            
            boolean sent = smsService.sendVerifyCode(phone);
            if (sent) {
                Map<String, Object> data = new HashMap<>();
                data.put("cooldown", 60);
                return R.success(data);
            } else {
                return R.error("验证码发送失败，请稍后重试");
            }
        } catch (Exception e) {
            log.error("发送验证码失败", e);
            return R.error("发送验证码失败: " + e.getMessage());
        }
    }

    @GetMapping("/cooldown")
    public R<Map<String, Object>> getCooldown(@RequestParam String phone) {
        try {
            long remaining = smsService.getRemainingCooldown(phone);
            Map<String, Object> data = new HashMap<>();
            data.put("remainingCooldown", remaining);
            return R.success(data);
        } catch (Exception e) {
            log.error("获取冷却时间失败", e);
            return R.error("获取冷却时间失败");
        }
    }
}
