package com.poti.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.poti.common.R;
import com.poti.common.security.JwtUtil;
import com.poti.user.entity.User;
import com.poti.user.service.SmsService;
import com.poti.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/user")
@Tag(name = "用户管理", description = "用户信息相关接口")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${avatar.upload.path:./uploads/avatars/}")
    private String avatarUploadPath;

    @Value("${avatar.url.prefix:http://localhost:8200/avatars/}")
    private String avatarUrlPrefix;

    @GetMapping("/info")
    @Operation(summary = "获取用户信息", description = "根据用户ID获取用户详细信息")
    public R<User> getUserInfo(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            if (userId == null) {
                return R.error("用户未登录");
            }
            User user = userService.getById(userId);
            if (user == null) {
                return R.error("用户不存在");
            }
            return R.success(user);
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return R.error("获取用户信息失败");
        }
    }

    @PutMapping("/info")
    @Operation(summary = "更新用户信息", description = "更新用户的昵称、手机号、邮箱等信息")
    public R<Void> updateUserInfo(@RequestHeader("X-User-Id") Long userId,
                                   @RequestBody User user) {
        try {
            log.info("更新用户信息，userId: {}, user: {}", userId, user);
            
            LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(User::getId, userId);
            
            if (user.getNickname() != null) {
                updateWrapper.set(User::getNickname, user.getNickname());
            }
            if (user.getPhone() != null) {
                updateWrapper.set(User::getPhone, user.getPhone());
            }
            if (user.getEmail() != null) {
                updateWrapper.set(User::getEmail, user.getEmail());
            }
            if (user.getGender() != null) {
                updateWrapper.set(User::getGender, user.getGender());
            }
            if (user.getAvatarUrl() != null) {
                updateWrapper.set(User::getAvatarUrl, user.getAvatarUrl());
            }
            if (user.getUniqueId() != null && !user.getUniqueId().trim().isEmpty()) {
                if (user.getUniqueId().length() < 4 || user.getUniqueId().length() > 20) {
                    return R.error("唯一ID长度必须在4-20个字符之间");
                }
                if (!user.getUniqueId().matches("^[a-zA-Z0-9_]+$")) {
                    return R.error("唯一ID只能包含字母、数字和下划线");
                }
                LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(User::getUniqueId, user.getUniqueId())
                           .ne(User::getId, userId);
                User existingUser = userService.getOne(queryWrapper);
                if (existingUser != null) {
                    return R.error("该唯一ID已被使用");
                }
                updateWrapper.set(User::getUniqueId, user.getUniqueId());
            }
            
            boolean success = userService.update(updateWrapper);
            log.info("更新结果: {}", success);
            if (success) {
                return R.success();
            } else {
                return R.error("更新用户信息失败");
            }
        } catch (Exception e) {
            log.error("更新用户信息失败", e);
            return R.error("更新用户信息失败");
        }
    }

    @GetMapping("/statistics")
    public R<Map<String, Object>> getStatistics(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            if (userId == null) {
                return R.error("用户未登录");
            }
            Map<String, Object> statistics = userService.getStatistics(userId);
            return R.success(statistics);
        } catch (Exception e) {
            log.error("获取学习统计失败", e);
            return R.error("获取学习统计失败");
        }
    }

    @GetMapping("/calendar")
    public R<Map<String, Object>> getCalendar(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                              @RequestParam("year") Integer year,
                                              @RequestParam("month") Integer month) {
        try {
            if (userId == null) {
                return R.error("用户未登录");
            }
            Map<String, Object> calendar = userService.getCalendar(userId, year, month);
            return R.success(calendar);
        } catch (Exception e) {
            log.error("获取刷题日历失败", e);
            return R.error("获取刷题日历失败");
        }
    }

    @PostMapping("/internal/create")
    public R<User> createOrUpdateUser(@RequestBody Map<String, String> request) {
        try {
            String openid = request.get("openid");
            if (openid == null || openid.isEmpty()) {
                return R.error("openid 不能为空");
            }
            User user = userService.findOrCreateByOpenid(openid);
            return R.success(user);
        } catch (Exception e) {
            log.error("创建用户失败", e);
            return R.error("创建用户失败");
        }
    }

    @PostMapping("/avatar")
    public R<String> uploadAvatar(@RequestHeader("X-User-Id") Long userId,
                                   @RequestParam("file") MultipartFile file) {
        try {
            log.info("开始上传头像，userId: {}, 文件大小: {}", userId, file.getSize());
            
            if (file.isEmpty()) {
                return R.error("请选择要上传的文件");
            }
            
            String originalFilename = file.getOriginalFilename();
            log.info("原始文件名: {}", originalFilename);
            
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            String fileName = UUID.randomUUID().toString() + extension;
            
            String absoluteUploadPath = new File(avatarUploadPath).getAbsolutePath();
            File uploadDir = new File(absoluteUploadPath);
            if (!uploadDir.exists()) {
                boolean created = uploadDir.mkdirs();
                log.info("创建上传目录: {}, 结果: {}", uploadDir.getAbsolutePath(), created);
            }
            
            File destFile = new File(uploadDir, fileName);
            log.info("目标文件路径: {}", destFile.getAbsolutePath());
            
            file.transferTo(destFile.getAbsoluteFile());
            
            String avatarUrl = avatarUrlPrefix + fileName;
            
            LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(User::getId, userId).set(User::getAvatarUrl, avatarUrl);
            userService.update(updateWrapper);
            
            log.info("用户 {} 头像上传成功: {}", userId, avatarUrl);
            return R.success(avatarUrl);
        } catch (Exception e) {
            log.error("头像上传失败", e);
            return R.error("头像上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/avatar/upload")
    public R<String> uploadAvatarForRegister(@RequestParam("file") MultipartFile file) {
        try {
            log.info("开始上传头像（注册），文件大小: {}", file.getSize());
            
            if (file.isEmpty()) {
                return R.error("请选择要上传的文件");
            }
            
            String originalFilename = file.getOriginalFilename();
            log.info("原始文件名: {}", originalFilename);
            
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            String fileName = UUID.randomUUID().toString() + extension;
            
            String absoluteUploadPath = new File(avatarUploadPath).getAbsolutePath();
            File uploadDir = new File(absoluteUploadPath);
            if (!uploadDir.exists()) {
                boolean created = uploadDir.mkdirs();
                log.info("创建上传目录: {}, 结果: {}", uploadDir.getAbsolutePath(), created);
            }
            
            File destFile = new File(uploadDir, fileName);
            log.info("目标文件路径: {}", destFile.getAbsolutePath());
            
            file.transferTo(destFile.getAbsoluteFile());
            
            String avatarUrl = avatarUrlPrefix + fileName;
            
            log.info("头像上传成功: {}", avatarUrl);
            return R.success(avatarUrl);
        } catch (Exception e) {
            log.error("头像上传失败", e);
            return R.error("头像上传失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/search")
    public R<List<Map<String, Object>>> searchUser(@RequestParam String keyword) {
        try {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.and(wrapper -> wrapper
                    .like(User::getUniqueId, keyword)
                    .or()
                    .like(User::getNickname, keyword)
                    .or()
                    .like(User::getPhone, keyword))
                    .eq(User::getStatus, 1)
                    .last("LIMIT 20");
            
            List<User> users = userService.list(queryWrapper);
            
            List<Map<String, Object>> result = users.stream().map(user -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", user.getId());
                map.put("nickname", user.getNickname());
                map.put("avatarUrl", user.getAvatarUrl());
                map.put("uniqueId", user.getUniqueId());
                return map;
            }).collect(Collectors.toList());
            
            return R.success(result);
        } catch (Exception e) {
            log.error("搜索用户失败", e);
            return R.error("搜索用户失败");
        }
    }

    @PostMapping("/register")
    public R<Map<String, Object>> register(@RequestBody Map<String, Object> request) {
        try {
            String nickname = (String) request.get("nickname");
            String phone = (String) request.get("phone");
            String email = (String) request.get("email");
            String password = (String) request.get("password");
            String uniqueId = (String) request.get("uniqueId");
            String avatarUrl = (String) request.get("avatarUrl");
            String verifyCode = (String) request.get("verifyCode");
            
            Integer gender = 0;
            Object genderObj = request.get("gender");
            if (genderObj != null) {
                if (genderObj instanceof Integer) {
                    gender = (Integer) genderObj;
                } else if (genderObj instanceof String) {
                    try {
                        gender = Integer.parseInt((String) genderObj);
                    } catch (NumberFormatException e) {
                        gender = 0;
                    }
                }
            }
            
            if (nickname == null || nickname.trim().isEmpty()) {
                return R.error("昵称不能为空");
            }
            
            if (phone == null || phone.trim().isEmpty()) {
                return R.error("手机号不能为空");
            }
            
            if (!phone.matches("^1[3-9]\\d{9}$")) {
                return R.error("手机号格式不正确");
            }
            
            if (verifyCode == null || verifyCode.trim().isEmpty()) {
                return R.error("验证码不能为空");
            }
            
            if (!smsService.verifyCode(phone, verifyCode)) {
                return R.error("验证码错误或已过期");
            }
            
            if (password == null || password.length() < 6) {
                return R.error("密码长度至少6位");
            }
            
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);
            User existingUser = userService.getOne(queryWrapper);
            if (existingUser != null) {
                return R.error("该手机号已注册");
            }
            
            if (uniqueId != null && !uniqueId.trim().isEmpty()) {
                if (uniqueId.length() < 4 || uniqueId.length() > 20) {
                    return R.error("唯一ID长度必须在4-20个字符之间");
                }
                if (!uniqueId.matches("^[a-zA-Z0-9_]+$")) {
                    return R.error("唯一ID只能包含字母、数字和下划线");
                }
                LambdaQueryWrapper<User> uniqueIdWrapper = new LambdaQueryWrapper<>();
                uniqueIdWrapper.eq(User::getUniqueId, uniqueId);
                User existingUniqueId = userService.getOne(uniqueIdWrapper);
                if (existingUniqueId != null) {
                    return R.error("该唯一ID已被使用");
                }
            }
            
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String encodedPassword = passwordEncoder.encode(password);
            
            User user = new User();
            user.setNickname(nickname.trim());
            user.setPhone(phone.trim());
            user.setEmail(email != null ? email.trim() : null);
            user.setPassword(encodedPassword);
            user.setUniqueId(uniqueId != null ? uniqueId.trim() : null);
            user.setOpenid("register_" + System.currentTimeMillis());
            user.setGender(gender);
            user.setAvatarUrl(avatarUrl);
            user.setStatus(1);
            user.setLastLoginTime(LocalDateTime.now());
            
            boolean success = userService.save(user);
            
            if (success) {
                smsService.deleteCode(phone);
                String token = jwtUtil.generateToken(user.getId(), user.getOpenid());
                
                Map<String, Object> result = new HashMap<>();
                result.put("token", token);
                result.put("userId", user.getId());
                result.put("nickname", user.getNickname());
                result.put("phone", user.getPhone());
                result.put("email", user.getEmail());
                result.put("uniqueId", user.getUniqueId());
                result.put("avatarUrl", user.getAvatarUrl());
                
                log.info("用户注册成功: {}", user.getId());
                return R.success(result);
            } else {
                return R.error("注册失败，请重试");
            }
        } catch (Exception e) {
            log.error("用户注册失败", e);
            return R.error("注册失败: " + e.getMessage());
        }
    }

    @PostMapping("/login/phone")
    public R<Map<String, Object>> loginByPhone(@RequestBody Map<String, String> request) {
        try {
            String phone = request.get("phone");
            String password = request.get("password");
            
            log.info("手机号密码登录请求，phone: {}", phone);
            
            if (phone == null || phone.trim().isEmpty()) {
                return R.error("手机号不能为空");
            }
            
            if (!phone.matches("^1[3-9]\\d{9}$")) {
                return R.error("手机号格式不正确");
            }
            
            if (password == null || password.trim().isEmpty()) {
                return R.error("密码不能为空");
            }
            
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone.trim());
            User user = userService.getOne(queryWrapper);
            
            if (user == null) {
                return R.error("该手机号未注册");
            }
            
            if (user.getStatus() != 1) {
                return R.error("账号已被禁用");
            }
            
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            if (!passwordEncoder.matches(password, user.getPassword())) {
                return R.error("密码错误");
            }
            
            LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(User::getId, user.getId()).set(User::getLastLoginTime, LocalDateTime.now());
            userService.update(updateWrapper);
            
            String token = jwtUtil.generateToken(user.getId(), user.getOpenid());
            
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("userId", user.getId());
            result.put("nickname", user.getNickname());
            result.put("phone", user.getPhone());
            result.put("email", user.getEmail());
            result.put("uniqueId", user.getUniqueId());
            result.put("avatarUrl", user.getAvatarUrl());
            
            log.info("用户登录成功: {}", user.getId());
            return R.success(result);
        } catch (Exception e) {
            log.error("手机号登录失败", e);
            return R.error("登录失败: " + e.getMessage());
        }
    }

    @PostMapping("/login/code")
    public R<Map<String, Object>> loginByCode(@RequestBody Map<String, String> request) {
        try {
            String phone = request.get("phone");
            String verifyCode = request.get("verifyCode");
            
            log.info("验证码登录请求，phone: {}", phone);
            
            if (phone == null || phone.trim().isEmpty()) {
                return R.error("手机号不能为空");
            }
            
            if (!phone.matches("^1[3-9]\\d{9}$")) {
                return R.error("手机号格式不正确");
            }
            
            if (verifyCode == null || verifyCode.trim().isEmpty()) {
                return R.error("验证码不能为空");
            }
            
            if (!smsService.verifyCode(phone, verifyCode)) {
                return R.error("验证码错误或已过期");
            }
            
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone.trim());
            User user = userService.getOne(queryWrapper);
            
            if (user == null) {
                return R.error("该手机号未注册");
            }
            
            if (user.getStatus() != 1) {
                return R.error("账号已被禁用");
            }
            
            LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(User::getId, user.getId()).set(User::getLastLoginTime, LocalDateTime.now());
            userService.update(updateWrapper);
            
            smsService.deleteCode(phone);
            
            String token = jwtUtil.generateToken(user.getId(), user.getOpenid());
            
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("userId", user.getId());
            result.put("nickname", user.getNickname());
            result.put("phone", user.getPhone());
            result.put("email", user.getEmail());
            result.put("uniqueId", user.getUniqueId());
            result.put("avatarUrl", user.getAvatarUrl());
            
            log.info("验证码登录成功: {}", user.getId());
            return R.success(result);
        } catch (Exception e) {
            log.error("验证码登录失败", e);
            return R.error("登录失败: " + e.getMessage());
        }
    }

    @PostMapping("/password/reset")
    public R<Void> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String phone = request.get("phone");
            String verifyCode = request.get("verifyCode");
            String newPassword = request.get("newPassword");
            
            log.info("重置密码请求，phone: {}", phone);
            
            if (phone == null || phone.trim().isEmpty()) {
                return R.error("手机号不能为空");
            }
            
            if (!phone.matches("^1[3-9]\\d{9}$")) {
                return R.error("手机号格式不正确");
            }
            
            if (verifyCode == null || verifyCode.trim().isEmpty()) {
                return R.error("验证码不能为空");
            }
            
            if (!smsService.verifyCode(phone, verifyCode)) {
                return R.error("验证码错误或已过期");
            }
            
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return R.error("新密码不能为空");
            }
            
            if (newPassword.length() < 6 || newPassword.length() > 20) {
                return R.error("密码长度需6-20位");
            }
            
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone.trim());
            User user = userService.getOne(queryWrapper);
            
            if (user == null) {
                return R.error("该手机号未注册");
            }
            
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String encodedPassword = passwordEncoder.encode(newPassword);
            
            LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(User::getId, user.getId()).set(User::getPassword, encodedPassword);
            boolean success = userService.update(updateWrapper);
            
            if (success) {
                smsService.deleteCode(phone);
                log.info("用户 {} 密码重置成功", user.getId());
                return R.success();
            } else {
                return R.error("密码重置失败，请重试");
            }
        } catch (Exception e) {
            log.error("密码重置失败", e);
            return R.error("密码重置失败: " + e.getMessage());
        }
    }
}
