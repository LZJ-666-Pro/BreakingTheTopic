package com.poti.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_statistics")
public class UserStatistics {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Integer totalQuestionCount;

    private Integer correctCount;

    private Integer wrongCount;

    private Integer favoriteCount;

    private LocalDateTime lastPracticeTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
