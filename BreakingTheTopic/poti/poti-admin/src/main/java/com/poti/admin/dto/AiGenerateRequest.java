package com.poti.admin.dto;

import lombok.Data;

@Data
public class AiGenerateRequest {
    
    private Long categoryId;
    
    private String categoryName;
    
    private Integer count = 5;
    
    private Integer difficulty = 1;
    
    private String topic;
    
    private String additionalRequirements;
}
