package com.poti.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日一题实体（poti_question.daily_question）。
 */
@Data
@TableName("daily_question")
public class DailyQuestion {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long questionId;

    private LocalDate date;

    private String reason;

    private String hint;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
