package com.poti.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 统一 JWT 鉴权过滤器。
 * <p>
 * 作用：
 * <ul>
 *     <li>对白名单之外的接口强制校验 {@code Authorization: Bearer &lt;token&gt;}</li>
 *     <li>校验通过后，将 JWT 中的用户 ID / openid 写入请求头（覆盖客户端伪造的 {@code X-User-Id}）</li>
 *     <li>支持可选的内部调用令牌（{@code X-Internal-Token}）用于服务间调用</li>
 * </ul>
 * </p>
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final SecurityProperties properties;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthenticationFilter(JwtUtil jwtUtil, SecurityProperties properties, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!properties.isEnabled()) {
            return true;
        }
        // CORS 预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = getRequestPath(request);
        boolean excluded = properties.getExcludePaths().stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
        if (excluded) {
            log.debug("鉴权过滤器放行白名单路径: {}", path);
        }
        return excluded;
    }

    private String getRequestPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (StringUtils.hasText(contextPath) && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        return path;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);

        // 内部调用令牌放行（用于 auth -> user 等没有用户 Token 的服务间调用）
        if (token == null && StringUtils.hasText(properties.getInternalToken())) {
            String internalToken = request.getHeader(properties.getInternalTokenHeader());
            if (properties.getInternalToken().equals(internalToken)) {
                log.debug("鉴权过滤器通过内部令牌放行: {}", request.getRequestURI());
                filterChain.doFilter(request, response);
                return;
            }
        }

        if (token == null || !jwtUtil.validateToken(token)) {
            writeUnauthorized(response);
            return;
        }

        Claims claims = jwtUtil.parseToken(token);
        Long userId;
        try {
            userId = Long.parseLong(claims.getSubject());
        } catch (NumberFormatException e) {
            log.warn("Token 中 userId 非法: {}", claims.getSubject());
            writeUnauthorized(response);
            return;
        }
        String openid = claims.get("openid", String.class);

        HeaderMapRequestWrapper wrapper = new HeaderMapRequestWrapper(request);
        wrapper.addHeader(properties.getUserIdHeader(), String.valueOf(userId));
        if (openid != null) {
            wrapper.addHeader(properties.getOpenidHeader(), openid);
        }
        log.debug("鉴权通过 userId={}, uri={}", userId, request.getRequestURI());
        filterChain.doFilter(wrapper, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String auth = request.getHeader(properties.getTokenHeader());
        if (StringUtils.hasText(auth) && auth.startsWith(properties.getTokenPrefix())) {
            return auth.substring(properties.getTokenPrefix().length());
        }
        return null;
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> body = new HashMap<>();
        body.put("code", 401);
        body.put("msg", "未登录或Token已失效");
        body.put("data", null);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}