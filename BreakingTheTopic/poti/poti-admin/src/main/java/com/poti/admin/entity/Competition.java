package com.poti.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 竞赛实体（poti_practice.competition）。
 * status 仅作发布标记（0草稿 1已发布），报名中/进行中/已结束由时间窗动态推导，不落库。
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /** 结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /** 每题分值 */
    private Integer scorePerQuestion;

    /** 0草稿 1已发布 */
    private Integer status;

    /** 创建管理员ID */
    private Long createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
