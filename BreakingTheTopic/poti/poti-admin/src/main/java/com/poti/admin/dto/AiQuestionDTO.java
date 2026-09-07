package com.poti.admin.dto;

import lombok.Data;

import java.util.List;

@Data
public class AiQuestionDTO {
    
    private String title;
    
    private String content;
    
    private Integer type = 1;
    
    private Integer difficulty = 1;
    
    private List<String> options;
    
    private String answer;
    
    private String analysis;
    
    private String tags;
}
