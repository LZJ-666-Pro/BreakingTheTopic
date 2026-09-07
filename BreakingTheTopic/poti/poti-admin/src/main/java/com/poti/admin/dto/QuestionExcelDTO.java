package com.poti.admin.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class QuestionExcelDTO {
    
    @ExcelProperty(value = "分类ID", index = 0)
    private Long categoryId;
    
    @ExcelProperty(value = "题目内容", index = 1)
    private String content;
    
    @ExcelProperty(value = "题目类型", index = 2)
    private Integer type;
    
    @ExcelProperty(value = "难度", index = 3)
    private Integer difficulty;
    
    @ExcelProperty(value = "选项A", index = 4)
    private String optionA;
    
    @ExcelProperty(value = "选项B", index = 5)
    private String optionB;
    
    @ExcelProperty(value = "选项C", index = 6)
    private String optionC;
    
    @ExcelProperty(value = "选项D", index = 7)
    private String optionD;
    
    @ExcelProperty(value = "选项E", index = 8)
    private String optionE;
    
    @ExcelProperty(value = "选项F", index = 9)
    private String optionF;
    
    @ExcelProperty(value = "正确答案", index = 10)
    private String answer;
    
    @ExcelProperty(value = "解析", index = 11)
    private String analysis;
    
    @ExcelProperty(value = "标签", index = 12)
    private String tags;
}
