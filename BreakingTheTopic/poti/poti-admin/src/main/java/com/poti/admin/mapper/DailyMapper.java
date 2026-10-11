package com.poti.admin.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poti.admin.entity.DailyQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/**
 * 每日一题 Mapper（poti_question 库）。
 */
@Mapper
@DS("question")
public interface DailyMapper extends BaseMapper<DailyQuestion> {

    @Select("SELECT content, difficulty FROM question WHERE id = #{questionId} AND deleted = 0")
    Map<String, Object> selectQuestionBrief(@Param("questionId") Long questionId);
}
