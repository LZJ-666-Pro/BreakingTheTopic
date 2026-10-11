package com.poti.admin.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poti.admin.entity.SpecialTopic;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 刷题专题 Mapper（poti_question 库）。
 */
@Mapper
@DS("question")
public interface SpecialMapper extends BaseMapper<SpecialTopic> {

    @Select("SELECT COUNT(*) FROM special_question WHERE topic_id = #{topicId}")
    long countQuestions(@Param("topicId") Long topicId);

    @Select("SELECT q.id, COALESCE(NULLIF(q.title, ''), q.content) AS content, q.difficulty, sq.sort FROM special_question sq " +
            "JOIN question q ON q.id = sq.question_id AND q.deleted = 0 " +
            "WHERE sq.topic_id = #{topicId} ORDER BY sq.sort")
    List<Map<String, Object>> selectTopicQuestions(@Param("topicId") Long topicId);

    @Delete("DELETE FROM special_question WHERE topic_id = #{topicId}")
    int deleteTopicQuestions(@Param("topicId") Long topicId);

    @Insert("INSERT INTO special_question (topic_id, question_id, sort) VALUES (#{topicId}, #{questionId}, #{sort})")
    int insertTopicQuestion(@Param("topicId") Long topicId, @Param("questionId") Long questionId, @Param("sort") int sort);
}
