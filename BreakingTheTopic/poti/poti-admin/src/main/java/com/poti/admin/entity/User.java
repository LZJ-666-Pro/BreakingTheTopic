package com.poti.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String openid;

    private String nickname;

    @TableField("avatar_url")
    private String avatarUrl;

    private String phone;

    private Integer gender;

    private Integer status;

    /** 会员等级 0普通 1VIP（权益按 vip_expire_time 动态判断，不依赖定时任务） */
    private Integer vipLevel;

    /** 会员到期时间（NULL=从未开通或已失效） */
    private LocalDateTime vipExpireTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
