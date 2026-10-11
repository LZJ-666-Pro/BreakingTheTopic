package com.poti.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_log")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long adminId;

    /** 操作人昵称 */
    private String adminName;

    /** 操作模块（题目管理/用户管理等） */
    private String module;

    /** 动作（新增/更新/删除） */
    private String action;

    /** 操作对象（接口路径） */
    private String target;

    /** 请求参数摘要 */
    private String detail;

    /** 1成功 0失败 */
    private Integer result;

    private String ip;

    /** 耗时(毫秒) */
    private Integer costMs;

    private LocalDateTime createTime;
}
