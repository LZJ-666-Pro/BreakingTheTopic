package com.poti.admin.service;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 图形验证码服务（内存存储，单实例部署够用）
 */
@Slf4j
@Service
public class CaptchaService {

    /** 验证码有效期：2 分钟 */
    private static final long EXPIRE_MS = 2 * 60 * 1000L;

    /** 超过该数量时触发过期清理 */
    private static final int CLEAN_THRESHOLD = 1000;

    private final Map<String, CaptchaEntry> store = new ConcurrentHashMap<>();

    public Map<String, Object> generate() {
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(130, 40, 4, 24);
        String key = UUID.randomUUID().toString().replace("-", "");
        store.put(key, new CaptchaEntry(captcha.getCode().toLowerCase(), System.currentTimeMillis() + EXPIRE_MS));
        cleanupIfNeeded();

        Map<String, Object> data = new HashMap<>();
        data.put("captchaKey", key);
        data.put("captchaImage", captcha.getImageBase64Data());
        return data;
    }

    public boolean validate(String key, String code) {
        if (key == null || key.isEmpty() || code == null || code.trim().isEmpty()) {
            return false;
        }
        CaptchaEntry entry = store.remove(key);
        return entry != null
                && System.currentTimeMillis() <= entry.expireAt
                && entry.code.equals(code.trim().toLowerCase());
    }

    private void cleanupIfNeeded() {
        if (store.size() < CLEAN_THRESHOLD) {
            return;
        }
        long now = System.currentTimeMillis();
        store.entrySet().removeIf(e -> now > e.getValue().expireAt);
    }

    private record CaptchaEntry(String code, long expireAt) {
    }
}
