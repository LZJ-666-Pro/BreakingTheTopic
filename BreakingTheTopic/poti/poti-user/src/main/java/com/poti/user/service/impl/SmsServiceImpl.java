package com.poti.user.service.impl;

import com.poti.common.redis.service.RedisService;
import com.poti.user.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    @Autowired
    private RedisService redisService;

    @Value("${sms.spug.template-id:}")
    private String spugTemplateId;

    @Value("${sms.spug.app-name:破题刷题}")
    private String spugAppName;

    @Value("${sms.verify.code.expire:300}")
    private long codeExpireSeconds;

    @Value("${sms.verify.code.cooldown:60}")
    private long cooldownSeconds;

    @Value("${sms.verify.code.max-per-day:10}")
    private int maxPerDay;

    private static final String CODE_PREFIX = "sms:code:";
    private static final String COOLDOWN_PREFIX = "sms:cooldown:";
    private static final String COUNT_PREFIX = "sms:count:";
    private static final String SPUG_URL = "https://push.spug.cc/send/";

    @Override
    public boolean sendVerifyCode(String phone) {
        if (!canSendCode(phone)) {
            log.warn("手机号 {} 发送验证码频率受限", phone);
            return false;
        }

        String code = generateCode();

        if (spugTemplateId == null || spugTemplateId.isEmpty()) {
            log.info("【开发模式】手机号 {} 的验证码为: {}", phone, code);
        } else {
            boolean sent = sendViaSpug(phone, code);
            if (!sent) {
                return false;
            }
        }

        redisService.set(CODE_PREFIX + phone, code, codeExpireSeconds, TimeUnit.SECONDS);
        redisService.set(COOLDOWN_PREFIX + phone, "1", cooldownSeconds, TimeUnit.SECONDS);
        
        String todayCountKey = COUNT_PREFIX + phone + ":" + java.time.LocalDate.now();
        Long count = redisService.increment(todayCountKey, 1);
        if (count != null && count == 1) {
            redisService.expire(todayCountKey, 1, TimeUnit.DAYS);
        }

        log.info("验证码已发送至手机号: {}", phone);
        return true;
    }

    @Override
    public boolean verifyCode(String phone, String code) {
        if (phone == null || code == null) {
            return false;
        }

        String storedCode = (String) redisService.get(CODE_PREFIX + phone);
        if (storedCode == null) {
            log.warn("手机号 {} 的验证码已过期或不存在", phone);
            return false;
        }

        boolean valid = code.equals(storedCode);
        if (valid) {
            log.info("手机号 {} 验证码验证成功", phone);
        } else {
            log.warn("手机号 {} 验证码验证失败，输入: {}, 正确: {}", phone, code, storedCode);
        }

        return valid;
    }

    @Override
    public void deleteCode(String phone) {
        redisService.delete(CODE_PREFIX + phone);
        log.info("手机号 {} 的验证码已删除", phone);
    }

    @Override
    public boolean canSendCode(String phone) {
        String todayCountKey = COUNT_PREFIX + phone + ":" + java.time.LocalDate.now();
        Object countObj = redisService.get(todayCountKey);
        int count = countObj != null ? Integer.parseInt(countObj.toString()) : 0;
        
        if (count >= maxPerDay) {
            log.warn("手机号 {} 今日发送次数已达上限: {}", phone, maxPerDay);
            return false;
        }

        if (redisService.hasKey(COOLDOWN_PREFIX + phone)) {
            log.warn("手机号 {} 正在冷却中", phone);
            return false;
        }

        return true;
    }

    @Override
    public long getRemainingCooldown(String phone) {
        Long ttl = redisService.getExpire(COOLDOWN_PREFIX + phone, TimeUnit.SECONDS);
        return ttl != null && ttl > 0 ? ttl : 0;
    }

    private String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    private boolean sendViaSpug(String phone, String code) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Object> body = new HashMap<>();
            body.put("name", spugAppName);
            body.put("code", code);
            body.put("targets", phone);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            
            String url = SPUG_URL + spugTemplateId;
            String response = restTemplate.postForObject(url, request, String.class);
            log.info("Spug 推送响应: {}", response);
            
            if (response == null) {
                log.error("Spug 推送响应为空");
                return false;
            }
            
            if (response.contains("\"code\"") && !response.contains("\"code\": 200") && !response.contains("\"code\":200")) {
                log.error("Spug 推送失败: {}", response);
                return false;
            }
            
            return true;
        } catch (Exception e) {
            log.error("Spug 推送发送失败", e);
            return false;
        }
    }
}
