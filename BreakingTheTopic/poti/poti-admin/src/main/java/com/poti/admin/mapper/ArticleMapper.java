package com.poti.admin.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poti.admin.entity.Article;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文章 Mapper（poti_admin 库，默认数据源）。
 */
@Mapper
@DS("admin")
public interface ArticleMapper extends BaseMapper<Article> {
}
