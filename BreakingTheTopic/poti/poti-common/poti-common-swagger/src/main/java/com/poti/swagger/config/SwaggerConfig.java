package com.poti.swagger.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("破题小程序API文档")
                        .description("破题刷题小程序后端API接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("破题团队")
                                .email("")
                                .url("")));
    }
}
