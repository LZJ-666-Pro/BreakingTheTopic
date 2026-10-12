package com.poti.user.config;

import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import feign.Retryer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class FeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(5, TimeUnit.SECONDS, 10, TimeUnit.SECONDS, true);
    }

    @Bean
    public Retryer retryer() {
        return new Retryer.Default(100, TimeUnit.SECONDS.toMillis(1), 2);
    }

    /**
     * 服务间调用统一携带内部令牌（配合 JwtAuthenticationFilter 仅对 /internal/** 路径放行）。
     */
    @Bean
    public RequestInterceptor internalTokenInterceptor(@Value("${poti.security.internal-token:}") String internalToken) {
        return template -> {
            if (internalToken != null && !internalToken.isEmpty()) {
                template.header("X-Internal-Token", internalToken);
            }
        };
    }
}
