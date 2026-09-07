package com.poti.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")  // 指定数据库表名
public class User {

    @TableId(type = IdType.AUTO)   // 自增主键
    private Long id;

    private String openid;          // 微信 openid
    private String unionid;         // 微信 unionid（可选）
    private String nickname;        // 昵称
    private String avatarUrl;       // 头像 URL
    private Integer gender;         // 性别：0未知，1男，2女
    private String phone;           // 手机号
    private String email;           // 邮箱
    private String password;        // 密码
    private String uniqueId;        // 唯一ID，用于搜索添加好友
    private Integer status;         // 状态：0禁用，1正常
    private LocalDateTime lastLoginTime;

    @TableField(fill = FieldFill.INSERT)   // 自动填充创建时间（需要配置元对象处理器）
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)  // 自动填充更新时间
    private LocalDateTime updateTime;

    @TableLogic    // 逻辑删除字段，MyBatis-Plus 会自动处理
    private Integer deleted;
}