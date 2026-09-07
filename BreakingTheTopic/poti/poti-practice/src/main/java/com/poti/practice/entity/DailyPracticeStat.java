package com.poti.practice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("daily_practice_stat")
public class DailyPracticeStat {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private LocalDate practiceDate;

    private Integer count;

    private Integer correctCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
