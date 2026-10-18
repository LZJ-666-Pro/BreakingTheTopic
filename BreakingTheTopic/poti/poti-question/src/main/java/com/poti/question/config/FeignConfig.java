package com.poti.question.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 服务间调用统一携带内部令牌（X-Internal-Token），
 * 该令牌仅对业务服务的 /**\/internal/** 路径生效，无法访问普通业务接口。
 */
@Configuration
public class FeignConfig {

    @Value("${poti.security.internal-token}")
    private String internalToken;

    @Bean
    public RequestInterceptor internalTokenInterceptor() {
        return (RequestTemplate template) -> template.header("X-Internal-Token", internalToken);
    }
}
