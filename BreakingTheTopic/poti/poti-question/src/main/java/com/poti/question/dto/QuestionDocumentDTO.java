package com.poti.question.dto;

import lombok.Data;

import java.util.List;

@Data
public class QuestionDocumentDTO {
    private Long id;
    private Long categoryId;
    private String type;
    private String title;
    private String content;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String answer;
    private String analysis;
    private String tags;
    private Integer difficulty;
    private String categoryName;
}
