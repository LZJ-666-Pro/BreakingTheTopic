package com.poti.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 刷题专题实体（poti_question.special_topic）。
 */
@Data
@TableName("special_topic")
public class SpecialTopic {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String cover;

    private String description;

    /** 难度：1简单 2中等 3困难 */
    private Integer difficulty;

    /** 预计完成天数 */
    private Integer estimatedDays;

    /** 解锁规则：0自由刷 1按顺序解锁 */
    private Integer unlockType;

    /** 推荐权重，越大越靠前 */
    private Integer sort;

    /** 1上架 0下架 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
