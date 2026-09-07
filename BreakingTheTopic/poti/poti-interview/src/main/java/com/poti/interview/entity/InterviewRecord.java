package com.poti.interview.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("interview_record")
public class InterviewRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private String type;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer totalQuestions;

    private Integer correctCount;

    private Integer spendSeconds;

    private BigDecimal score;

    private String aiFeedback;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
