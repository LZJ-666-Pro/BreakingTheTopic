package com.poti.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poti.common.security.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 白名单路径，无需鉴权
     */
    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            "/auth/login",
            "/auth/dev/login",
            "/auth/admin/login",
            "/auth/validate",
            "/auth/refresh",
            "/user/login",
            "/user/register",
            "/user/password/reset",
            "/user/avatar/upload",
            "/user/internal/",
            "/user/info",
            "/sms/send",
            "/sms/cooldown",
            "/checkin/do",
            "/checkin/status",
            "/question/list",
            "/question/categories",
            "/question/detail/",
            "/search/question",
            "/search/questions",
            "/actuator",
            "/monitor",
            "/admin/login",
            "/admin/register",
            "/admin/upload/",
            "/admin/test"
    );

    /**
     * 过滤器执行顺序，数值越小优先级越高
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        log.info("网关收到请求，路径：{}", path);

        if (isExcludePath(path)) {
            log.info("白名单路径，放行：{}", path);
            return chain.filter(exchange);
        }

        String token = request.getHeaders().getFirst("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            log.warn("未提供有效的 Token，路径：{}", path);
            return unauthorized(exchange);
        }

        token = token.substring(7);
        try {
            if (!jwtUtil.validateToken(token)) {
                log.warn("Token 无效或已过期，路径：{}", path);
                return unauthorized(exchange);
            }

            Claims claims = jwtUtil.parseToken(token);
            Long userId = Long.parseLong(claims.getSubject());
            String openid = claims.get("openid", String.class);

            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", String.valueOf(userId))
                    .header("X-Openid", openid)
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (Exception e) {
            log.error("Token 解析失败，路径：{}", path, e);
            return unauthorized(exchange);
        }
    }

    /**.
     * 判断是否为白名单路径
     * @param path
     * @return
     */
    private boolean isExcludePath(String path) {
        for (String excludePath : EXCLUDE_PATHS) {
            if (excludePath.endsWith("/")) {
                if (path.startsWith(excludePath)) {
                    return true;
                }
            } else {
                if (path.equals(excludePath) || path.startsWith(excludePath + "/")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 未授权响应
     * @param exchange
     * @return
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 401);
        result.put("msg", "未授权，请先登录");
        result.put("data", null);

        ObjectMapper objectMapper = new ObjectMapper();
        String body;
        try {
            body = objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            body = "{\"code\":401,\"msg\":\"未授权\"}";
        }

        DataBuffer buffer = response.bufferFactory().wrap(
                body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 过滤器执行顺序，数值越小优先级越高
     * @return
     */
    @Override
    public int getOrder() {
        return -100;
    }
}
