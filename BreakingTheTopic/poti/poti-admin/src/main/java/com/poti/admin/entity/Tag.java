package com.poti.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题目标签实体（poti_question.tag）。
 * <p>
 * 题目通过 question.tags 逗号分隔字段与标签名（name）关联，标签与题目为多对多关系。
 * </p>
 */
@Data
@TableName("tag")
public class Tag {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标签名称（关联题目 tags 字段的关键词） */
    private String name;

    /** 所属分组（算法/数据结构/公司/场景等，可选） */
    private String groupName;

    /** 展示颜色 */
    private String color;

    /** 标签描述 */
    private String description;

    /** 排序权重，越大越靠前 */
    private Integer sort;

    /** 是否热门标签：1是 0否 */
    private Integer hot;

    /** 状态：1启用 0禁用 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
