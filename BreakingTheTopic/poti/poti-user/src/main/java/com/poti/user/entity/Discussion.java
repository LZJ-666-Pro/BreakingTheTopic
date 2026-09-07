package com.poti.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("discussion")
public class Discussion {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long questionId;

    private Long userId;

    private String userName;

    private String avatar;

    private String content;

    private Integer likeCount;

    private Long replyTo;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
