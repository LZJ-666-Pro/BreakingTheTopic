package com.poti.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 统一鉴权过滤器配置。
 * <p>
 * 配置前缀：{@code poti.security}
 * </p>
 * <pre>
 * poti:
 *   security:
 *     enabled: true
 *     exclude-paths:
 *       - /auth/login
 *     internal-token: ${INTERNAL_TOKEN:}
 * </pre>
 */
@ConfigurationProperties(prefix = "poti.security")
public class SecurityProperties {

    /**
     * 默认免鉴权路径（Ant 风格）。各服务可通过 {@code poti.security.exclude-paths} 覆盖。
     */
    public static final List<String> DEFAULT_EXCLUDE_PATHS = List.of(
            // ===== 认证服务公开接口 =====
            "/auth/login", "/auth/admin/login", "/auth/dev/login", "/auth/validate",

            // ===== 用户服务公开接口 =====
            "/user/register", "/user/login/**", "/user/password/reset", "/user/internal/**", "/user/avatar/upload",
            "/sms/**",
            "/config/**",
            // 小程序首页签到接口（小程序端未带 Token 直接调用，需后续改前端补 Token）
            "/checkin/**",

            // ===== 题目浏览公开接口 =====
            "/question/categories", "/question/list", "/question/random/**", "/question/search",
            "/question/detail/**", "/question/view/**", "/question/category/list", "/question/internal/**",

            // ===== 内部聚合接口（服务间 Feign 调用） =====
            "/practice/internal/**",
            "/wrongbook/internal/**",
            "/favorite/internal/**",

            // ===== 搜索公开接口 =====
            "/search/question", "/search/questions", "/search/hot",

            // ===== 管理后台登录 =====
            "/admin/login", "/admin/register", "/admin/captcha",

            // ===== 监控、文档、静态资源 =====
            "/actuator/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/webjars/**", "/doc.html",
            "/uploads/**", "/avatars/**", "/error"
    );

    /**
     * 是否启用统一鉴权过滤器，默认 true。
     */
    private boolean enabled = true;

    /**
     * Token 请求头名称。
     */
    private String tokenHeader = "Authorization";

    /**
     * Token 前缀。
     */
    private String tokenPrefix = "Bearer ";

    /**
     * 用户 ID 请求头名称（过滤器会从 Token 中解析并覆盖该请求头，防止伪造）。
     */
    private String userIdHeader = "X-User-Id";

    /**
     * openid 请求头名称。
     */
    private String openidHeader = "X-User-Openid";

    /**
     * 内部调用令牌请求头名称。
     */
    private String internalTokenHeader = "X-Internal-Token";

    /**
     * 内部调用令牌。为空则不启用内部令牌放行机制。
     */
    private String internalToken = "";

    /**
     * 免鉴权路径（Ant 风格）。默认值为 {@link #DEFAULT_EXCLUDE_PATHS}。
     */
    private List<String> excludePaths = new ArrayList<>(DEFAULT_EXCLUDE_PATHS);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getTokenHeader() {
        return tokenHeader;
    }

    public void setTokenHeader(String tokenHeader) {
        this.tokenHeader = tokenHeader;
    }

    public String getTokenPrefix() {
        return tokenPrefix;
    }

    public void setTokenPrefix(String tokenPrefix) {
        this.tokenPrefix = tokenPrefix;
    }

    public String getUserIdHeader() {
        return userIdHeader;
    }

    public void setUserIdHeader(String userIdHeader) {
        this.userIdHeader = userIdHeader;
    }

    public String getOpenidHeader() {
        return openidHeader;
    }

    public void setOpenidHeader(String openidHeader) {
        this.openidHeader = openidHeader;
    }

    public String getInternalTokenHeader() {
        return internalTokenHeader;
    }

    public void setInternalTokenHeader(String internalTokenHeader) {
        this.internalTokenHeader = internalTokenHeader;
    }

    public String getInternalToken() {
        return internalToken;
    }

    public void setInternalToken(String internalToken) {
        this.internalToken = internalToken;
    }

    public List<String> getExcludePaths() {
        return excludePaths;
    }

    public void setExcludePaths(List<String> excludePaths) {
        this.excludePaths = excludePaths;
    }
}