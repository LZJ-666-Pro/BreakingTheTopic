package com.poti.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("checkin_record")
public class CheckinRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    private LocalDate checkinDate;
    private LocalDateTime checkinTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableLogic
    private Integer deleted;
}
