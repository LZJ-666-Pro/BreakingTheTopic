package com.poti.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("checkin_record")
public class CheckIn {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    @TableField("checkin_date")
    private LocalDate checkinDate;

    @TableField("checkin_time")
    private LocalDateTime checkinTime;

    private Integer streakDays;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
