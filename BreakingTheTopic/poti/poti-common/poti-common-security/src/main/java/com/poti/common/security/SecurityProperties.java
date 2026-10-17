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
            "/user/register", "/user/login/**", "/user/password/reset", "/user/avatar/upload",
            "/sms/**",
            "/config/**",

            // ===== 题目浏览公开接口 =====
            "/question/categories", "/question/list", "/question/random/**", "/question/search",
            "/question/detail/**", "/question/view/**", "/question/category/list",
            // 题库页分组+分类聚合数据（公共只读，AI 导师 Feign 也复用）
            "/question/category/groups",
            // 刷题专题与每日一题（公开浏览）
            "/question/special/**", "/question/daily/**",

            // ===== 搜索公开接口 =====
            "/search/question", "/search/questions", "/search/hot",
            // 题目索引同步（运维触发，ES 数据写入入口）
            "/search/sync/**", "/question/sync-to-es",

            // ===== 管理后台登录 =====
            "/admin/login", "/admin/register", "/admin/captcha",

            // ===== 监控、文档、静态资源 =====
            "/actuator/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/webjars/**", "/doc.html",
            // 说明：/xxx/internal/** 服务间接口与 /checkin/** 均已移出白名单，
            // 内部接口统一走 X-Internal-Token（仅 /internal/** 路径生效），签到接口前端已补 JWT
            "/uploads/**", "/avatars/**", "/error",
            // 聊天媒体文件（小程序 <image>/audio 组件加载资源时无法携带 JWT，需放行直连访问）
            "/chat/images/**", "/chat/voices/**"
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