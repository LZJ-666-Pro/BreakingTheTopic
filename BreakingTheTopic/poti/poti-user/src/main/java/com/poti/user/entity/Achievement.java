package com.poti.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("achievement")
public class Achievement {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String achievementCode;
    private String name;
    private String icon;
    private String description;
    private String category;
    private String type;
    private Integer conditionValue;
    private Integer rewardPoints;
    private Integer sortOrder;
    
    @TableLogic
    private Integer deleted;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
