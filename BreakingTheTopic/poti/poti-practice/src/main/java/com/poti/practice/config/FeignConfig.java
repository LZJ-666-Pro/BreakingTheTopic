package com.poti.practice.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign 通用配置：服务间调用统一携带内部令牌
 * （配合 JwtAuthenticationFilter 仅对 /internal/** 路径放行）。
 */
@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor internalTokenInterceptor(@Value("${poti.security.internal-token:}") String internalToken) {
        return template -> {
            if (internalToken != null && !internalToken.isEmpty()) {
                template.header("X-Internal-Token", internalToken);
            }
        };
    }
}
