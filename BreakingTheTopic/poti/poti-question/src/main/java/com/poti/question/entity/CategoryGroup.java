package com.poti.question.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题库分组标签实体（题库页顶部筛选标签：热门/后端/前端...）。
 */
@Data
@TableName("category_group")
public class CategoryGroup {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 分组名称 */
    private String name;

    /** 分组标识 */
    private String groupKey;

    /** 排序 */
    private Integer sort;

    /** 状态：1启用 0停用 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
