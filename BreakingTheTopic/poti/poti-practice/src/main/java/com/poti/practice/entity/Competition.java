package com.poti.practice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 答题竞赛实体
 * status 仅作发布标记（0草稿 1已发布），报名中/进行中/已结束由 start_time/end_time 动态推导
 */
@Data
@TableName("competition")
public class Competition {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 竞赛名称 */
    private String title;

    /** 竞赛说明 */
    private String description;

    /** 题库分类ID（抽题范围） */
    private Long categoryId;

    /** 抽题数量 */
    private Integer questionCount;

    /** 答题时长（分钟） */
    private Integer durationMinutes;

    /** 开赛时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 每题分值 */
    private Integer scorePerQuestion;

    /** 0草稿 1已发布 */
    private Integer status;

    /** 创建管理员ID */
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
