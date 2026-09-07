package com.poti.admin.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poti.admin.entity.Category;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@DS("question")
public interface CategoryMapper extends BaseMapper<Category> {
}
