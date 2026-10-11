package com.poti.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分类-分组标签关联实体（poti_question.category_group_rel）。
 */
@Data
@TableName("category_group_rel")
public class CategoryGroupRel {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 分类ID */
    private Long categoryId;

    /** 分组标签ID */
    private Long groupId;

    private LocalDateTime createTime;
}
