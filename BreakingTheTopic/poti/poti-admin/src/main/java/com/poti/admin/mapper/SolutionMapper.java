package com.poti.admin.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poti.admin.entity.Solution;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/**
 * 题解 Mapper（poti_question 库）。
 */
@Mapper
@DS("question")
public interface SolutionMapper extends BaseMapper<Solution> {

    @Select("SELECT q.id AS questionId, q.content AS questionContent, q.difficulty AS difficulty " +
            "FROM question q WHERE q.id = #{questionId} AND q.deleted = 0")
    Map<String, Object> selectQuestionBrief(@Param("questionId") Long questionId);
}
