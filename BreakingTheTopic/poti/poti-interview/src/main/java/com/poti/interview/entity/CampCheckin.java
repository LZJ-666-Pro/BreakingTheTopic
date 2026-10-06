package com.poti.interview.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("camp_checkin")
public class CampCheckin {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String campId;

    private Integer dayNum;

    private LocalDate checkinDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
