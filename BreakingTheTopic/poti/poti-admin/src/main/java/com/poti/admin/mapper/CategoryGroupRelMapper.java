package com.poti.admin.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poti.admin.entity.CategoryGroupRel;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@DS("question")
public interface CategoryGroupRelMapper extends BaseMapper<CategoryGroupRel> {
}
