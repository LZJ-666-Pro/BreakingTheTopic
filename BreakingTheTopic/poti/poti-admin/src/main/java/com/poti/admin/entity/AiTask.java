package com.poti.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ai_task")
public class AiTask {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long categoryId;
    
    private String categoryName;
    
    private Integer totalCount;
    
    private Integer completedCount;
    
    private Integer status;
    
    private String difficulty;
    
    private String topic;
    
    private String additionalRequirements;
    
    private String errorMessage;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
