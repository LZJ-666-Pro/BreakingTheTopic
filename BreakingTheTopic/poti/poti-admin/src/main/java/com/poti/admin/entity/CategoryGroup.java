package com.poti.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题库分组标签实体（poti_question.category_group）。
 * <p>
 * 对应小程序题库页顶部的筛选标签（热门/后端/前端/人工智能...），
 * 分类与分组标签通过 category_group_rel 多对多关联。
 * group_key = hidden 为特殊分组，挂在该分组下的分类不在题库宫格展示。
 * </p>
 */
@Data
@TableName("category_group")
public class CategoryGroup {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 分组名称（页面显示） */
    private String name;

    /** 分组标识（程序用，如 backend） */
    private String groupKey;

    /** 排序（越小越靠前） */
    private Integer sort;

    /** 状态：1启用 0停用 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
