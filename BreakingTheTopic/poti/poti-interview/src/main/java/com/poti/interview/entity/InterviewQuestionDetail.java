package com.poti.interview.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("interview_question_detail")
public class InterviewQuestionDetail {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long interviewId;

    private Long questionId;

    private String questionContent;

    private String options;

    private String referenceAnswer;

    private String correctAnswer;

    private String userAnswer;

    private String audioUrl;

    private Integer isCorrect;

    private BigDecimal score;

    private String aiComment;

    private Integer answerTimeSeconds;

    private Integer orderNum;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
