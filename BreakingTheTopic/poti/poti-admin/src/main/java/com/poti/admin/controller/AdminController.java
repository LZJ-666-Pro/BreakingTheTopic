package com.poti.admin.controller;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.poti.admin.entity.Admin;
import com.poti.admin.entity.Role;
import com.poti.admin.mapper.AdminMapper;
import com.poti.admin.mapper.RoleMapper;
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

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private com.poti.admin.service.ConfigService configService;

    @Autowired
    private com.poti.admin.service.PasswordResetService passwordResetService;

    @GetMapping("/captcha")
    public R<Map<String, Object>> captcha() {
        try {
            return R.success(captchaService.generate());
        } catch (Exception e) {
            log.error("生成验证码失败", e);
            return R.error("获取验证码失败");
        }
    }

    /**
     * JWT 无感刷新（滑动续期）：旧 token 仍有效时换发新 token，后台操作期间不掉线；
     * 已过期则拒绝，需重新登录。前端在请求拦截器按剩余有效期节流调用。
     */
    @PostMapping("/refresh")
    public R<Map<String, Object>> refreshToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return R.error("未登录");
            }
            String oldToken = authHeader.substring(7);
            if (!jwtUtil.validateToken(oldToken)) {
                return R.error("登录已过期，请重新登录");
            }
            Long adminId = jwtUtil.getUserIdFromToken(oldToken);
            if (adminService.getById(adminId) == null) {
                return R.error("账号不存在，请重新登录");
            }
            String newToken = jwtUtil.refreshToken(oldToken);
            if (newToken == null) {
                return R.error("刷新失败，请重新登录");
            }
            Map<String, Object> result = new HashMap<>();
            result.put("token", newToken);
            log.info("管理员 token 无感刷新成功: {}", adminId);
            return R.success(result);
        } catch (Exception e) {
            log.error("管理员 token 刷新失败", e);
            return R.error("刷新失败，请重新登录");
        }
    }

    /**
     * 忘记密码第一步：发送邮箱验证码（未登录可用，白名单放行；接口内部校验账号与绑定邮箱）
     */
    @PostMapping("/password/forgot")
    public R<Map<String, Object>> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            if (username == null || username.trim().isEmpty()) {
                return R.error("请输入用户名");
            }
            String emailMasked = passwordResetService.sendCode(username.trim());
            Map<String, Object> result = new HashMap<>();
            result.put("emailMasked", emailMasked);
            return R.success(result);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return R.error(e.getMessage());
        } catch (Exception e) {
            log.error("发送密码重置验证码失败", e);
            return R.error("发送失败，请稍后重试");
        }
    }

    /**
     * 忘记密码第二步：验证码校验 + 重置密码（未登录可用，白名单放行）
     */
    @PostMapping("/password/reset")
    public R<Void> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String code = request.get("code");
            String newPassword = request.get("newPassword");
            if (username == null || username.trim().isEmpty() || code == null || code.trim().isEmpty()) {
                return R.error("请输入用户名和验证码");
            }
            passwordResetService.reset(username.trim(), code.trim(), newPassword);
            return R.success(null);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return R.error(e.getMessage());
        } catch (Exception e) {
            log.error("重置密码失败", e);
            return R.error("重置失败，请稍后重试");
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
            // 后台「业务开关-登录验证码」关闭时跳过校验（配置缺失视为开启）
            boolean captchaEnabled = isSwitchOn("login_captcha_enabled");
            if (captchaEnabled && !captchaService.validate(captchaKey, captchaCode)) {
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
            user.put("roleId", admin.getRoleId());
            user.put("permissions", resolvePermissions(admin));
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
            String username = request.get("username");
            String password = request.get("password");
            String nickname = request.get("nickname");
            String email = request.get("email");
            String phone = request.get("phone");
            String avatar = request.get("avatar");

            log.info("收到管理员注册请求: username={}", username);
            
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
            user.put("roleId", admin.getRoleId());
            user.put("permissions", resolvePermissions(admin));

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

    // ==================== 管理员账号管理（权限管理页） ====================

    /** 管理员账号列表（不返回密码） */
    @GetMapping("/account/list")
    public R<java.util.List<Map<String, Object>>> accountList() {
        try {
            java.util.List<Admin> admins = adminMapper.selectList(
                    new LambdaQueryWrapper<Admin>().orderByAsc(Admin::getId));
            java.util.List<Map<String, Object>> result = new java.util.ArrayList<>();
            java.util.Map<Long, String> roleNames = new HashMap<>();
            for (Role role : roleMapper.selectList(new LambdaQueryWrapper<Role>())) {
                roleNames.put(role.getId(), role.getName());
            }
            for (Admin a : admins) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", a.getId());
                item.put("username", a.getUsername());
                item.put("nickname", a.getNickname());
                item.put("email", a.getEmail());
                item.put("phone", a.getPhone());
                item.put("avatar", a.getAvatar());
                item.put("status", a.getStatus());
                item.put("role", a.getRole());
                item.put("roleId", a.getRoleId());
                item.put("roleName", a.getRoleId() != null ? roleNames.get(a.getRoleId()) : null);
                item.put("lastLoginTime", a.getLastLoginTime());
                item.put("lastLoginIp", a.getLastLoginIp());
                item.put("createTime", a.getCreateTime());
                result.add(item);
            }
            return R.success(result);
        } catch (Exception e) {
            log.error("获取管理员账号列表失败", e);
            return R.error("获取管理员账号列表失败");
        }
    }

    /** 新增管理员账号 */
    @PostMapping("/account/create")
    public R<Void> accountCreate(@RequestBody Map<String, Object> request) {
        try {
            String username = trim(request.get("username"));
            String password = trim(request.get("password"));
            String nickname = trim(request.get("nickname"));
            String email = trim(request.get("email"));
            String phone = trim(request.get("phone"));
            Long roleId = toLong(request.get("roleId"));

            if (username == null || password == null) {
                return R.error("用户名和密码不能为空");
            }
            if (username.length() < 3 || username.length() > 20) {
                return R.error("用户名长度必须在3-20个字符之间");
            }
            if (password.length() < 6 || password.length() > 20) {
                return R.error("密码长度必须在6-20个字符之间");
            }
            if (adminService.getByUsername(username) != null) {
                return R.error("用户名已存在");
            }
            if (roleId != null && roleMapper.selectById(roleId) == null) {
                return R.error("所选角色不存在");
            }

            Admin admin = new Admin();
            admin.setUsername(username);
            admin.setPassword(BCrypt.hashpw(password));
            admin.setNickname(nickname);
            admin.setEmail(email);
            admin.setPhone(phone);
            admin.setStatus(1);
            admin.setRole(1);
            admin.setRoleId(roleId);
            adminService.save(admin);
            return R.success(null);
        } catch (Exception e) {
            log.error("新增管理员失败", e);
            return R.error("新增管理员失败");
        }
    }

    /** 编辑管理员：昵称/邮箱/手机号/角色/状态 */
    @PutMapping("/account/update")
    public R<Void> accountUpdate(
            @RequestHeader(value = "X-User-Id", required = false) Long operatorId,
            @RequestBody Map<String, Object> request) {
        try {
            Long id = toLong(request.get("id"));
            if (id == null) {
                return R.error("参数错误");
            }
            Admin admin = adminService.getById(id);
            if (admin == null) {
                return R.error("账号不存在");
            }
            // 系统内置超管账号只允许编辑基础资料，不允许禁用/改角色（防止锁死）
            boolean builtin = "admin".equals(admin.getUsername());
            Integer status = toInteger(request.get("status"));
            Long roleId = toLong(request.get("roleId"));

            if (builtin && (roleId != null && !roleId.equals(admin.getRoleId()))) {
                return R.error("内置超级管理员账号不允许修改角色");
            }
            if (builtin && status != null && status != 1) {
                return R.error("内置超级管理员账号不允许禁用");
            }
            if (operatorId != null && operatorId.equals(id) && status != null && status != 1) {
                return R.error("不能禁用自己的账号");
            }
            if (roleId != null && roleMapper.selectById(roleId) == null) {
                return R.error("所选角色不存在");
            }

            String nickname = trim(request.get("nickname"));
            if (nickname != null) {
                admin.setNickname(nickname);
            }
            String email = trim(request.get("email"));
            if (email != null) {
                admin.setEmail(email.isEmpty() ? null : email);
            }
            String phone = trim(request.get("phone"));
            if (phone != null) {
                admin.setPhone(phone.isEmpty() ? null : phone);
            }
            if (roleId != null) {
                admin.setRoleId(roleId);
            }
            if (status != null) {
                admin.setStatus(status);
            }
            adminService.updateById(admin);
            return R.success(null);
        } catch (Exception e) {
            log.error("编辑管理员失败", e);
            return R.error("编辑管理员失败");
        }
    }

    /** 重置管理员密码 */
    @PostMapping("/account/reset-password/{id}")
    public R<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String newPassword = request.get("newPassword");
            if (newPassword == null || newPassword.length() < 6 || newPassword.length() > 20) {
                return R.error("新密码长度必须在6-20个字符之间");
            }
            Admin admin = adminService.getById(id);
            if (admin == null) {
                return R.error("账号不存在");
            }
            admin.setPassword(BCrypt.hashpw(newPassword));
            adminService.updateById(admin);
            return R.success(null);
        } catch (Exception e) {
            log.error("重置密码失败", e);
            return R.error("重置密码失败");
        }
    }

    /** 删除管理员账号 */
    @DeleteMapping("/account/delete/{id}")
    public R<Void> accountDelete(
            @RequestHeader(value = "X-User-Id", required = false) Long operatorId,
            @PathVariable Long id) {
        try {
            Admin admin = adminService.getById(id);
            if (admin == null) {
                return R.error("账号不存在");
            }
            if ("admin".equals(admin.getUsername())) {
                return R.error("内置超级管理员账号不允许删除");
            }
            if (operatorId != null && operatorId.equals(id)) {
                return R.error("不能删除自己的账号");
            }
            adminMapper.deleteById(id);
            return R.success(null);
        } catch (Exception e) {
            log.error("删除管理员失败", e);
            return R.error("删除管理员失败");
        }
    }

    /**
     * 解析管理员菜单权限：
     * super 角色 / 权限为空 / 旧 role=2 → 全部权限(*)；
     * 否则返回角色配置的路由 index 串；无任何角色时默认仅仪表盘。
     */
    private String resolvePermissions(Admin admin) {
        if (admin.getRoleId() != null) {
            Role role = roleMapper.selectById(admin.getRoleId());
            if (role != null && role.getStatus() != null && role.getStatus() == 1) {
                if ("super".equals(role.getCode())
                        || role.getPermissions() == null || role.getPermissions().trim().isEmpty()) {
                    return "*";
                }
                return role.getPermissions();
            }
        }
        if (admin.getRole() != null && admin.getRole() == 2) {
            return "*";
        }
        return "/dashboard";
    }

    /** 读取 sys_config 开关：配置缺失/禁用视为开启，显式 false 才算关闭 */
    private boolean isSwitchOn(String key) {
        com.poti.admin.entity.Config config = configService.getByKey(key);
        String v = config != null ? config.getConfigValue() : null;
        return v == null || !v.trim().equalsIgnoreCase("false");
    }

    private String trim(Object value) {
        if (value == null) return null;
        String s = String.valueOf(value);
        return s.trim().isEmpty() ? null : s.trim();
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer toInteger(Object value) {
        if (value == null) return null;
        try {
            return Integer.valueOf(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
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
