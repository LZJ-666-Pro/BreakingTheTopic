package com.poti.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AiConfig {
    
    private String provider = "openai";
    
    private String apiKey;
    
    private String baseUrl = "https://api.openai.com/v1";
    
    private String model = "gpt-3.5-turbo";
    
    private Integer maxTokens = 2000;
    
    private Double temperature = 0.7;
}
