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

    /** 自动发布随机抽题：启用状态、未删除，且排除 sinceDate（含）以来已作为每日一题的题目 */
    @Select("SELECT id, title, difficulty FROM question " +
            "WHERE deleted = 0 AND status = 1 " +
            "AND NOT EXISTS (SELECT 1 FROM daily_question d WHERE d.question_id = question.id AND d.date >= #{sinceDate}) " +
            "ORDER BY RAND() LIMIT 1")
    Map<String, Object> selectRandomQuestion(@Param("sinceDate") java.time.LocalDate sinceDate);

    /** 兜底随机抽题：近 30 天题目全部用尽时，不排除重复再抽 */
    @Select("SELECT id, title, difficulty FROM question " +
            "WHERE deleted = 0 AND status = 1 ORDER BY RAND() LIMIT 1")
    Map<String, Object> selectRandomQuestionAny();
}
