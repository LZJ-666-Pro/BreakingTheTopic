package com.poti.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("discussion_like")
public class DiscussionLike {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long discussionId;

    private Long userId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private Integer deleted;
}
