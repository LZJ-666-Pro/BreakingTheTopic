package com.poti.admin.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poti.admin.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
@DS("question")
public interface CategoryMapper extends BaseMapper<Category> {

    /** 分类下未删除题目数量 */
    @Select("SELECT COUNT(*) FROM question WHERE category_id = #{categoryId} AND deleted = 0")
    long countQuestionsByCategoryId(Long categoryId);
}
