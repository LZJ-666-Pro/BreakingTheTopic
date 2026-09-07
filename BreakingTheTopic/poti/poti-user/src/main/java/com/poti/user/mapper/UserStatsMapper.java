package com.poti.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poti.user.entity.UserStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserStatsMapper extends BaseMapper<UserStats> {
    
    @Select("SELECT * FROM user_stats WHERE user_id = #{userId} AND deleted = 0")
    UserStats selectByUserId(@Param("userId") Long userId);
    
    @Update("UPDATE user_stats SET consecutive_days = #{consecutiveDays}, update_time = NOW() WHERE user_id = #{userId} AND deleted = 0")
    int updateByUserId(UserStats userStats);
    
    @Select("SELECT us.*, u.nickname, u.avatar_url, " +
            "IFNULL(us.total_questions, 0) as total_questions, " +
            "IFNULL(us.today_questions, 0) as today_questions, " +
            "IFNULL(us.consecutive_days, 0) as consecutive_days " +
            "FROM user_stats us " +
            "LEFT JOIN user u ON us.user_id = u.id " +
            "WHERE us.deleted = 0 AND u.deleted = 0 " +
            "ORDER BY us.total_questions DESC " +
            "LIMIT #{offset}, #{limit}")
    List<Map<String, Object>> getRankingByTotalQuestions(@Param("offset") int offset, @Param("limit") int limit);
    
    @Select("SELECT us.*, u.nickname, u.avatar_url, " +
            "IFNULL(us.total_questions, 0) as total_questions, " +
            "IFNULL(us.today_questions, 0) as today_questions, " +
            "IFNULL(us.consecutive_days, 0) as consecutive_days " +
            "FROM user_stats us " +
            "LEFT JOIN user u ON us.user_id = u.id " +
            "WHERE us.deleted = 0 AND u.deleted = 0 AND us.last_practice_date = CURDATE() " +
            "ORDER BY us.today_questions DESC " +
            "LIMIT #{offset}, #{limit}")
    List<Map<String, Object>> getRankingByTodayQuestions(@Param("offset") int offset, @Param("limit") int limit);
    
    @Select("SELECT us.*, u.nickname, u.avatar_url, " +
            "IFNULL(us.total_questions, 0) as total_questions, " +
            "IFNULL(us.today_questions, 0) as today_questions, " +
            "IFNULL(us.consecutive_days, 0) as consecutive_days, " +
            "CASE WHEN us.total_questions > 0 THEN ROUND(us.correct_questions * 100.0 / us.total_questions, 2) ELSE 0 END as accuracy " +
            "FROM user_stats us " +
            "LEFT JOIN user u ON us.user_id = u.id " +
            "WHERE us.deleted = 0 AND u.deleted = 0 AND us.total_questions >= 10 " +
            "ORDER BY accuracy DESC " +
            "LIMIT #{offset}, #{limit}")
    List<Map<String, Object>> getRankingByAccuracy(@Param("offset") int offset, @Param("limit") int limit);
    
    @Select("SELECT us.*, u.nickname, u.avatar_url, " +
            "IFNULL(us.total_questions, 0) as total_questions, " +
            "IFNULL(us.today_questions, 0) as today_questions, " +
            "IFNULL(us.consecutive_days, 0) as consecutive_days " +
            "FROM user_stats us " +
            "LEFT JOIN user u ON us.user_id = u.id " +
            "WHERE us.deleted = 0 AND u.deleted = 0 AND us.consecutive_days > 0 " +
            "ORDER BY us.consecutive_days DESC " +
            "LIMIT #{offset}, #{limit}")
    List<Map<String, Object>> getRankingByConsecutiveDays(@Param("offset") int offset, @Param("limit") int limit);
    
    @Select("SELECT COUNT(*) + 1 as `rank` " +
            "FROM user_stats " +
            "WHERE deleted = 0 AND total_questions > (SELECT total_questions FROM user_stats WHERE user_id = #{userId} AND deleted = 0)")
    Integer getUserRankByTotalQuestions(@Param("userId") Long userId);
    
    @Select("SELECT COUNT(*) + 1 as `rank` " +
            "FROM user_stats " +
            "WHERE deleted = 0 AND last_practice_date = CURDATE() AND today_questions > (SELECT today_questions FROM user_stats WHERE user_id = #{userId} AND deleted = 0)")
    Integer getUserRankByTodayQuestions(@Param("userId") Long userId);
    
    @Select("SELECT COUNT(*) + 1 as `rank` " +
            "FROM user_stats " +
            "WHERE deleted = 0 AND total_questions >= 10 AND (correct_questions * 100.0 / total_questions) > " +
            "(SELECT CASE WHEN total_questions > 0 THEN correct_questions * 100.0 / total_questions ELSE 0 END FROM user_stats WHERE user_id = #{userId} AND deleted = 0)")
    Integer getUserRankByAccuracy(@Param("userId") Long userId);
    
    @Select("SELECT COUNT(*) + 1 as `rank` " +
            "FROM user_stats " +
            "WHERE deleted = 0 AND consecutive_days > 0 AND consecutive_days > (SELECT consecutive_days FROM user_stats WHERE user_id = #{userId} AND deleted = 0)")
    Integer getUserRankByConsecutiveDays(@Param("userId") Long userId);
}
