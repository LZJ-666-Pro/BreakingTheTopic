package com.poti.auth.controller;

import com.poti.common.R;
import com.poti.common.security.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Value("${wx.appid}")
    private String appid;

    @Value("${wx.secret}")
    private String secret;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${poti.security.internal-token:}")
    private String internalToken;

/**
 * Handles the login request using WeChat's authentication flow
 * @param request Contains the WeChat authentication code
 * @return Response object containing token and user information or an error message
 */
    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        try {
            log.info("收到登录请求，请求参数：{}", request);
            
            String code = request.get("code");
            log.info("获取到的 code：{}", code);
            
            if (code == null || code.isEmpty()) {
                log.warn("code 为空");
                return R.error("code 不能为空");
            }

            String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + appid +
                    "&secret=" + secret + "&js_code=" + code + "&grant_type=authorization_code";
            log.info("微信 API URL：{}", url);
            
            Map<String, Object> wxResult = restTemplate.getForObject(url, Map.class);
            log.info("微信 API 返回结果：{}", wxResult);
            
            if (wxResult == null) {
                log.error("微信 API 返回结果为空");
                return R.error("微信登录失败");
            }
            
            if (wxResult.containsKey("errcode")) {
                Integer errcode = (Integer) wxResult.get("errcode");
                String errmsg = (String) wxResult.get("errmsg");
                log.error("微信登录失败，错误码：{}，错误信息：{}", errcode, errmsg);
                return R.error("微信登录失败：" + errmsg);
            }
            
            String openid = (String) wxResult.get("openid");
            String sessionKey = (String) wxResult.get("session_key");
            log.info("获取到的 openid：{}，session_key：{}", openid, sessionKey);
            
            if (openid == null || openid.isEmpty()) {
                log.error("openid 为空");
                return R.error("获取 openid 失败");
            }
            
            Long userId = Math.abs((long) openid.hashCode());
            String nickname = "微信用户";
            String avatarUrl = "";
            log.info("生成的 userId：{}", userId);

            String userCreateUrl = "http://127.0.0.1:8200/user/internal/create";
            Map<String, String> createRequest = new HashMap<>();
            createRequest.put("openid", openid);
            
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                if (StringUtils.hasText(internalToken)) {
                    headers.set("X-Internal-Token", internalToken);
                }
                HttpEntity<Map<String, String>> entity = new HttpEntity<>(createRequest, headers);
                
                @SuppressWarnings("unchecked")
                Map<String, Object> userResult = restTemplate.postForObject(userCreateUrl, entity, Map.class);
                log.info("用户服务返回结果：{}", userResult);
                
                if (userResult != null && Integer.valueOf(200).equals(userResult.get("code"))) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> userData = (Map<String, Object>) userResult.get("data");
                    if (userData != null && userData.get("id") != null) {
                        userId = Long.valueOf(userData.get("id").toString());
                        log.info("从用户服务获取到的 userId：{}", userId);
                        
                        if (userData.get("nickname") != null) {
                            nickname = (String) userData.get("nickname");
                        }
                        if (userData.get("avatarUrl") != null) {
                            avatarUrl = (String) userData.get("avatarUrl");
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("调用用户服务失败，使用默认值", e);
            }
            
            String token = jwtUtil.generateToken(userId, openid);
            log.info("生成的 token：{}", token);
            
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("userId", userId);
            result.put("nickname", nickname);
            result.put("avatarUrl", avatarUrl);
            
            log.info("登录成功，返回结果：{}", result);
            return R.success(result);
        } catch (Exception e) {
            log.error("登录失败", e);
            return R.error("登录失败：" + e.getMessage());
        }
    }

    @GetMapping("/validate")
    public R<Boolean> validate(@RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.replace("Bearer ", "");
            boolean valid = jwtUtil.validateToken(token);
            return R.success(valid);
        } catch (Exception e) {
            log.error("Token 验证失败", e);
            return R.success(false);
        }
    }

    @GetMapping("/userinfo")
    public R<Map<String, Object>> getUserInfo(@RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.replace("Bearer ", "");
            Claims claims = jwtUtil.parseToken(token);
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", claims.getSubject());
            userInfo.put("openid", claims.get("openid"));
            
            return R.success(userInfo);
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return R.error("获取用户信息失败");
        }
    }

    @PostMapping("/refresh")
    public R<Map<String, String>> refresh(@RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.replace("Bearer ", "");
            String newToken = jwtUtil.refreshToken(token);
            
            if (newToken == null) {
                return R.error("Token 刷新失败");
            }
            
            Map<String, String> result = new HashMap<>();
            result.put("token", newToken);
            
            return R.success(result);
        } catch (Exception e) {
            log.error("Token 刷新失败", e);
            return R.error("Token 刷新失败");
        }
    }

    @PostMapping("/dev/login")
    public R<Map<String, Object>> devLogin(@RequestBody Map<String, String> request) {
        try {
            log.info("收到开发环境登录请求，请求参数：{}", request);
            
            String userIdStr = request.get("userId");
            String uniqueId = request.get("uniqueId");
            
            if (userIdStr == null && uniqueId == null) {
                return R.error("请提供 userId 或 uniqueId");
            }
            
            Long userId = null;
            String openid = null;
            String nickname = "测试用户";
            String avatarUrl = "";
            
            if (userIdStr != null) {
                try {
                    userId = Long.valueOf(userIdStr);
                } catch (NumberFormatException e) {
                    return R.error("userId 格式错误");
                }
            }
            
            String userQueryUrl = "http://127.0.0.1:8200/user/info";
            if (userId != null) {
                userQueryUrl += "?userId=" + userId;
            }
            
            try {
                HttpHeaders headers = new HttpHeaders();
                if (userId != null) {
                    headers.set("X-User-Id", userId.toString());
                }
                if (StringUtils.hasText(internalToken)) {
                    headers.set("X-Internal-Token", internalToken);
                }
                HttpEntity<String> entity = new HttpEntity<>(headers);
                
                @SuppressWarnings("unchecked")
                Map<String, Object> userResult = restTemplate.exchange(
                    userQueryUrl, 
                    org.springframework.http.HttpMethod.GET, 
                    entity, 
                    Map.class
                ).getBody();
                
                log.info("用户服务返回结果：{}", userResult);
                
                if (userResult != null && Integer.valueOf(200).equals(userResult.get("code"))) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> userData = (Map<String, Object>) userResult.get("data");
                    if (userData != null) {
                        if (userData.get("id") != null) {
                            userId = Long.valueOf(userData.get("id").toString());
                        }
                        if (userData.get("openid") != null) {
                            openid = (String) userData.get("openid");
                        }
                        if (userData.get("nickname") != null) {
                            nickname = (String) userData.get("nickname");
                        }
                        if (userData.get("avatarUrl") != null) {
                            avatarUrl = (String) userData.get("avatarUrl");
                        }
                    }
                } else {
                    return R.error("用户不存在");
                }
            } catch (Exception e) {
                log.error("查询用户信息失败", e);
                return R.error("查询用户信息失败");
            }
            
            if (openid == null || openid.isEmpty()) {
                openid = "dev_" + userId;
            }
            
            String token = jwtUtil.generateToken(userId, openid);
            log.info("生成的 token：{}", token);
            
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("userId", userId);
            result.put("nickname", nickname);
            result.put("avatarUrl", avatarUrl);
            
            log.info("开发环境登录成功，返回结果：{}", result);
            return R.success(result);
        } catch (Exception e) {
            log.error("开发环境登录失败", e);
            return R.error("登录失败：" + e.getMessage());
        }
    }

    @PostMapping("/admin/login")
    public R<Map<String, Object>> adminLogin(@RequestBody Map<String, String> request) {
        try {
            log.info("收到管理员登录请求");
            
            String username = request.get("username");
            String password = request.get("password");
            
            if (username == null || password == null) {
                return R.error("用户名或密码不能为空");
            }
            
            if ("admin".equals(username) && "admin123".equals(password)) {
                Long adminId = 1L;
                String openid = "admin_" + adminId;
                String token = jwtUtil.generateToken(adminId, openid);
                
                Map<String, Object> result = new HashMap<>();
                result.put("token", token);
                
                Map<String, Object> user = new HashMap<>();
                user.put("id", adminId);
                user.put("nickname", "管理员");
                user.put("username", username);
                result.put("user", user);
                
                log.info("管理员登录成功");
                return R.success(result);
            }
            
            log.warn("管理员登录失败，用户名或密码错误");
            return R.error("用户名或密码错误");
        } catch (Exception e) {
            log.error("管理员登录失败", e);
            return R.error("登录失败：" + e.getMessage());
        }
    }
}
