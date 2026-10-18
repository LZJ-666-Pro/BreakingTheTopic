package com.poti.question.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 每日一题答题记录：用户维度一天一条，用于「答完不再展示」判定 */
@Data
@TableName("daily_question_answer")
public class DailyAnswer {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long questionId;

    private LocalDate answerDate;

    private Integer isCorrect;

    private LocalDateTime answerTime;
}
