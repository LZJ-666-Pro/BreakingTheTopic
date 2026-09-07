package com.poti.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("search_record")
public class SearchRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String keyword;

    private Long userId;

    private Integer searchCount;

    private Integer userCount;

    private LocalDateTime lastSearchTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
