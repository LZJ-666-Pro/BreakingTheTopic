package com.poti.question.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 刷题专题（poti_question.special_topic）。
 */
@Data
@TableName("special_topic")
public class SpecialTopic {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String cover;

    private String description;

    private Integer difficulty;

    private Integer estimatedDays;

    private Integer unlockType;

    private Integer sort;

    /** 1上架 0下架 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
