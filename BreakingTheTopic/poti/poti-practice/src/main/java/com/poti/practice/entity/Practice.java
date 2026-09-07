package com.poti.practice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("practice_record")
public class Practice {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long questionId;

    private String userAnswer;

    private Boolean isCorrect;

    private Integer spendSeconds;

    @TableField(value = "practice_time", fill = FieldFill.INSERT)
    private LocalDateTime practiceTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
