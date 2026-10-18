package com.poti.admin.service;

import cn.hutool.crypto.digest.BCrypt;
import com.poti.admin.entity.Admin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 后台忘记密码（邮箱验证码重置）服务
 *
 * 验证码内存存储（与 CaptchaService 同模式，单实例部署够用）：
 * - 6 位数字，10 分钟有效
 * - 同一账号 60 秒发送冷却
 * - 校验通过即销毁，一次性使用
 */
@Slf4j
@Service
public class PasswordResetService {

    private static final long CODE_EXPIRE_MS = 10 * 60 * 1000L;
    private static final long SEND_COOLDOWN_MS = 60 * 1000L;

    private final Map<String, CodeEntry> store = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    @Autowired
    private AdminService adminService;

    @Autowired
    private MailService mailService;

    /** 发送验证码，返回脱敏邮箱用于前端展示 */
    public String sendCode(String username) {
        Admin admin = adminService.getByUsername(username);
        if (admin == null) {
            throw new IllegalArgumentException("账号不存在");
        }
        String email = admin.getEmail();
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("该账号未绑定邮箱，请联系超级管理员重置密码");
        }

        // 过期清理，防止 map 膨胀
        store.entrySet().removeIf(e -> System.currentTimeMillis() > e.getValue().expireAt);

        CodeEntry entry = store.get(username);
        if (entry != null) {
            long since = System.currentTimeMillis() - entry.sendAt;
            if (since < SEND_COOLDOWN_MS) {
                throw new IllegalArgumentException("发送过于频繁，请 " + ((SEND_COOLDOWN_MS - since) / 1000 + 1) + " 秒后再试");
            }
        }

        String code = String.format("%06d", random.nextInt(1000000));
        mailService.sendCaptcha(email.trim(), code);
        store.put(username, new CodeEntry(code, System.currentTimeMillis() + CODE_EXPIRE_MS, System.currentTimeMillis()));
        return mask(email.trim());
    }

    /** 校验验证码并重置密码 */
    public void reset(String username, String code, String newPassword) {
        if (newPassword == null || newPassword.length() < 6 || newPassword.length() > 20) {
            throw new IllegalArgumentException("新密码长度必须在6-20个字符之间");
        }
        CodeEntry entry = store.get(username);
        if (entry == null || System.currentTimeMillis() > entry.expireAt) {
            throw new IllegalArgumentException("验证码已过期，请重新获取");
        }
        if (!entry.code.equals(code == null ? "" : code.trim())) {
            throw new IllegalArgumentException("验证码错误");
        }

        Admin admin = adminService.getByUsername(username);
        if (admin == null) {
            throw new IllegalArgumentException("账号不存在");
        }

        Admin update = new Admin();
        update.setId(admin.getId());
        update.setPassword(BCrypt.hashpw(newPassword));
        adminService.updateById(update);

        // 一次性使用，无论后续操作成功与否都销毁
        store.remove(username);
        log.info("管理员 {} 已通过邮箱验证码重置密码", username);
    }

    private String mask(String email) {
        int at = email.indexOf('@');
        if (at <= 0) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(at);
    }

    private record CodeEntry(String code, long expireAt, long sendAt) {
    }
}
