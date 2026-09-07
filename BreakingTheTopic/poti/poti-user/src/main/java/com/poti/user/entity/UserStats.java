package com.poti.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("user_stats")
public class UserStats {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    private Integer totalQuestions;
    private Integer correctQuestions;
    private Integer wrongQuestions;
    private Integer totalTime;
    private Integer todayQuestions;
    private Integer todayCorrect;
    private Integer todayTime;
    private Integer consecutiveDays;
    private LocalDate lastPracticeDate;
    private Integer totalPoints;
    private Integer achievementPoints;
    
    @TableLogic
    private Integer deleted;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
