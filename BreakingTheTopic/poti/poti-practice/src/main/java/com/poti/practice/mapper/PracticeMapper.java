package com.poti.practice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poti.practice.entity.Practice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface PracticeMapper extends BaseMapper<Practice> {

    @Select("SELECT DATE(practice_time) as date, COUNT(*) as count FROM practice_record " +
            "WHERE user_id = #{userId} AND YEAR(practice_time) = #{year} AND MONTH(practice_time) = #{month} " +
            "AND deleted = 0 " +
            "GROUP BY DATE(practice_time) " +
            "ORDER BY date")
    List<Map<String, Object>> getCalendarByMonth(@Param("userId") Long userId, 
                                                @Param("year") Integer year, 
                                                @Param("month") Integer month);

    @Select("SELECT p.user_id, COUNT(*) as total_questions, " +
            "SUM(CASE WHEN p.is_correct = 1 THEN 1 ELSE 0 END) as correct_questions, " +
            "SUM(CASE WHEN DATE(p.practice_time) = CURDATE() THEN 1 ELSE 0 END) as today_questions " +
            "FROM practice_record p " +
            "WHERE p.deleted = 0 " +
            "GROUP BY p.user_id " +
            "ORDER BY total_questions DESC " +
            "LIMIT #{offset}, #{limit}")
    List<Map<String, Object>> getRankingByTotalQuestions(@Param("offset") Integer offset, @Param("limit") Integer limit);

    @Select("SELECT p.user_id, COUNT(*) as total_questions, " +
            "SUM(CASE WHEN p.is_correct = 1 THEN 1 ELSE 0 END) as correct_questions, " +
            "SUM(CASE WHEN DATE(p.practice_time) = CURDATE() THEN 1 ELSE 0 END) as today_questions " +
            "FROM practice_record p " +
            "WHERE p.deleted = 0 AND DATE(p.practice_time) = CURDATE() " +
            "GROUP BY p.user_id " +
            "ORDER BY today_questions DESC " +
            "LIMIT #{offset}, #{limit}")
    List<Map<String, Object>> getRankingByTodayQuestions(@Param("offset") Integer offset, @Param("limit") Integer limit);

    @Select("SELECT p.user_id, COUNT(*) as total_questions, " +
            "SUM(CASE WHEN p.is_correct = 1 THEN 1 ELSE 0 END) as correct_questions, " +
            "ROUND(SUM(CASE WHEN p.is_correct = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) as accuracy " +
            "FROM practice_record p " +
            "WHERE p.deleted = 0 " +
            "GROUP BY p.user_id " +
            "HAVING total_questions >= 10 " +
            "ORDER BY accuracy DESC " +
            "LIMIT #{offset}, #{limit}")
    List<Map<String, Object>> getRankingByAccuracy(@Param("offset") Integer offset, @Param("limit") Integer limit);

    @Select("SELECT COUNT(*) + 1 as `rank` FROM (" +
            "SELECT user_id, COUNT(*) as total FROM practice_record WHERE deleted = 0 GROUP BY user_id" +
            ") t WHERE t.total > (" +
            "SELECT COUNT(*) FROM practice_record WHERE user_id = #{userId} AND deleted = 0" +
            ")")
    Integer getUserRankByTotalQuestions(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) + 1 as `rank` FROM (" +
            "SELECT user_id, COUNT(*) as total FROM practice_record WHERE deleted = 0 AND DATE(practice_time) = CURDATE() GROUP BY user_id" +
            ") t WHERE t.total > (" +
            "SELECT COUNT(*) FROM practice_record WHERE user_id = #{userId} AND deleted = 0 AND DATE(practice_time) = CURDATE()" +
            ")")
    Integer getUserRankByTodayQuestions(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) + 1 as `rank` FROM (" +
            "SELECT user_id, COUNT(*) as total, SUM(CASE WHEN is_correct = 1 THEN 1 ELSE 0 END) as correct, " +
            "ROUND(SUM(CASE WHEN is_correct = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) as accuracy " +
            "FROM practice_record WHERE deleted = 0 GROUP BY user_id HAVING COUNT(*) >= 10" +
            ") t WHERE t.accuracy > (" +
            "SELECT ROUND(SUM(CASE WHEN is_correct = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) " +
            "FROM practice_record WHERE user_id = #{userId} AND deleted = 0" +
            ")")
    Integer getUserRankByAccuracy(@Param("userId") Long userId);
}
