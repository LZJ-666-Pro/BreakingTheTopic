package com.poti.admin.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/**
 * 刷题统计 Mapper（poti_practice 库）——每日一题完成情况。
 */
@Mapper
@DS("practice")
public interface PracticeStatMapper {

    /** 某题在指定日期的完成人数与答对人数 */
    @Select("SELECT COUNT(*) AS total, IFNULL(SUM(is_correct = 1), 0) AS correct FROM practice_record " +
            "WHERE deleted = 0 AND question_id = #{questionId} AND DATE(practice_time) = #{date}")
    Map<String, Object> selectDailyStat(@Param("questionId") Long questionId, @Param("date") String date);
}
