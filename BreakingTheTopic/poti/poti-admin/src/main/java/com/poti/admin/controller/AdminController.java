package com.poti.admin.controller;

import cn.hutool.crypto.digest.BCrypt;
import com.poti.admin.entity.Admin;
import com.poti.admin.service.AdminService;
import com.poti.admin.service.CaptchaService;
import com.poti.common.R;
import com.poti.common.security.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CaptchaService captchaService;

    @GetMapping("/captcha")
    public R<Map<String, Object>> captcha() {
        try {
            return R.success(captchaService.generate());
        } catch (Exception e) {
            log.error("生成验证码失败", e);
            return R.error("获取验证码失败");
        }
    }

    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        try {
            log.info("收到管理员登录请求");
            
            String username = request.get("username");
            String password = request.get("password");
            
            if (username == null || username.trim().isEmpty()) {
                return R.error("用户名不能为空");
            }
            if (password == null || password.trim().isEmpty()) {
                return R.error("密码不能为空");
            }

            String captchaKey = request.get("captchaKey");
            String captchaCode = request.get("captchaCode");
            if (!captchaService.validate(captchaKey, captchaCode)) {
                return R.error("验证码错误或已过期");
            }

            Admin admin = adminService.login(username, password);
            if (admin == null) {
                log.warn("管理员登录失败，用户名或密码错误: {}", username);
                return R.error("用户名或密码错误");
            }
            
            String ip = getClientIp(httpRequest);
            adminService.updateLastLogin(admin.getId(), ip);
            
            String openid = "admin_" + admin.getId();
            String token = jwtUtil.generateToken(admin.getId(), openid);
            
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            
            Map<String, Object> user = new HashMap<>();
            user.put("id", admin.getId());
            user.put("username", admin.getUsername());
            user.put("nickname", admin.getNickname());
            user.put("avatar", admin.getAvatar());
            user.put("role", admin.getRole());
            result.put("user", user);
            
            log.info("管理员登录成功: {}", username);
            return R.success(result);
        } catch (Exception e) {
            log.error("管理员登录失败", e);
            return R.error("登录失败：" + e.getMessage());
        }
    }

    @PostMapping("/register")
    public R<Void> register(@RequestBody Map<String, String> request) {
        try {
            log.info("收到管理员注册请求，请求数据：{}", request);
            
            String username = request.get("username");
            String password = request.get("password");
            String nickname = request.get("nickname");
            String email = request.get("email");
            String phone = request.get("phone");
            String avatar = request.get("avatar");
            
            log.info("解析后的字段 - username: {}, nickname: {}, email: {}, phone: {}, avatar: {}", 
                username, nickname, email, phone, avatar);
            
            if (username == null || username.trim().isEmpty()) {
                return R.error("用户名不能为空");
            }
            if (password == null || password.trim().isEmpty()) {
                return R.error("密码不能为空");
            }
            if (nickname == null || nickname.trim().isEmpty()) {
                return R.error("昵称不能为空");
            }
            
            if (username.length() < 3 || username.length() > 20) {
                return R.error("用户名长度必须在3-20个字符之间");
            }
            if (password.length() < 6 || password.length() > 20) {
                return R.error("密码长度必须在6-20个字符之间");
            }
            if (nickname.length() < 2 || nickname.length() > 20) {
                return R.error("昵称长度必须在2-20个字符之间");
            }
            
            if (email != null && !email.trim().isEmpty()) {
                if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                    return R.error("邮箱格式不正确");
                }
            }
            
            if (phone != null && !phone.trim().isEmpty()) {
                if (!phone.matches("^1[3-9]\\d{9}$")) {
                    return R.error("手机号格式不正确");
                }
            }
            
            Admin existAdmin = adminService.getByUsername(username);
            if (existAdmin != null) {
                return R.error("用户名已存在");
            }
            
            Admin admin = new Admin();
            admin.setUsername(username);
            admin.setPassword(BCrypt.hashpw(password));
            admin.setNickname(nickname);
            admin.setEmail(email != null && !email.trim().isEmpty() ? email.trim() : null);
            admin.setPhone(phone != null && !phone.trim().isEmpty() ? phone.trim() : null);
            admin.setAvatar(avatar != null && !avatar.trim().isEmpty() ? avatar.trim() : null);
            admin.setStatus(1);
            admin.setRole(1);
            
            adminService.save(admin);
            
            log.info("管理员注册成功: {}", username);
            return R.success(null);
        } catch (Exception e) {
            log.error("管理员注册失败", e);
            return R.error("注册失败：" + e.getMessage());
        }
    }

    @GetMapping("/info")
    public R<Map<String, Object>> getInfo(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            if (userId == null) {
                return R.error("未登录");
            }
            
            Admin admin = adminService.getById(userId);
            if (admin == null) {
                return R.error("用户不存在");
            }
            
            Map<String, Object> user = new HashMap<>();
            user.put("id", admin.getId());
            user.put("username", admin.getUsername());
            user.put("nickname", admin.getNickname());
            user.put("avatar", admin.getAvatar());
            user.put("email", admin.getEmail());
            user.put("phone", admin.getPhone());
            user.put("role", admin.getRole());
            
            return R.success(user);
        } catch (Exception e) {
            log.error("获取管理员信息失败", e);
            return R.error("获取信息失败");
        }
    }

    @PostMapping("/password")
    public R<Void> updatePassword(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestBody Map<String, String> request) {
        try {
            if (userId == null) {
                return R.error("未登录");
            }
            
            String oldPassword = request.get("oldPassword");
            String newPassword = request.get("newPassword");
            
            if (oldPassword == null || newPassword == null) {
                return R.error("参数错误");
            }
            
            Admin admin = adminService.getById(userId);
            if (admin == null) {
                return R.error("用户不存在");
            }
            
            if (!BCrypt.checkpw(oldPassword, admin.getPassword())) {
                return R.error("原密码错误");
            }
            
            admin.setPassword(BCrypt.hashpw(newPassword));
            adminService.updateById(admin);
            
            return R.success(null);
        } catch (Exception e) {
            log.error("修改密码失败", e);
            return R.error("修改密码失败");
        }
    }

    @PostMapping("/update")
    public R<Void> updateInfo(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestBody Map<String, String> request) {
        try {
            if (userId == null) {
                return R.error("未登录");
            }
            
            Admin admin = adminService.getById(userId);
            if (admin == null) {
                return R.error("用户不存在");
            }
            
            String nickname = request.get("nickname");
            String email = request.get("email");
            String phone = request.get("phone");
            String avatar = request.get("avatar");
            
            if (nickname != null && !nickname.trim().isEmpty()) {
                if (nickname.length() < 2 || nickname.length() > 20) {
                    return R.error("昵称长度必须在2-20个字符之间");
                }
                admin.setNickname(nickname.trim());
            }
            
            if (email != null && !email.trim().isEmpty()) {
                if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                    return R.error("邮箱格式不正确");
                }
                admin.setEmail(email.trim());
            } else if (email != null && email.trim().isEmpty()) {
                admin.setEmail(null);
            }
            
            if (phone != null && !phone.trim().isEmpty()) {
                if (!phone.matches("^1[3-9]\\d{9}$")) {
                    return R.error("手机号格式不正确");
                }
                admin.setPhone(phone.trim());
            } else if (phone != null && phone.trim().isEmpty()) {
                admin.setPhone(null);
            }
            
            if (avatar != null && !avatar.trim().isEmpty()) {
                admin.setAvatar(avatar.trim());
            }
            
            adminService.updateById(admin);
            
            log.info("管理员信息更新成功: {}", admin.getUsername());
            return R.success(null);
        } catch (Exception e) {
            log.error("更新管理员信息失败", e);
            return R.error("更新失败：" + e.getMessage());
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
