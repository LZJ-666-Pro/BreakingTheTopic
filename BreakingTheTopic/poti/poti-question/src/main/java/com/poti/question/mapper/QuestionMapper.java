package com.poti.question.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poti.question.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {

    /** 各分类未删除题目数量 */
    @Select("SELECT category_id AS categoryId, COUNT(*) AS cnt FROM question WHERE deleted = 0 GROUP BY category_id")
    List<Map<String, Object>> countByCategory();
}
