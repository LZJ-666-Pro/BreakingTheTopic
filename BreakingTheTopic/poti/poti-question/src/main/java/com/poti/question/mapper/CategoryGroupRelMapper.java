package com.poti.question.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poti.question.entity.CategoryGroupRel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface CategoryGroupRelMapper extends BaseMapper<CategoryGroupRel> {

    /** 全部分类-分组标识关联（联表取标识，排除已删除分组） */
    @Select("SELECT r.category_id AS categoryId, g.group_key AS groupKey " +
            "FROM category_group_rel r JOIN category_group g ON r.group_id = g.id AND g.deleted = 0")
    List<Map<String, Object>> selectAllCategoryGroupKeys();
}
