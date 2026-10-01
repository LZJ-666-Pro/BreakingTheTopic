package com.poti.admin.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DashboardMapper {

    @DS("question")
    @Select("SELECT COUNT(*) FROM question WHERE deleted = 0")
    Integer countQuestions();

    @DS("question")
    @Select("SELECT COUNT(*) FROM category WHERE deleted = 0")
    Integer countCategories();

    @DS("user")
    @Select("SELECT COUNT(*) FROM user WHERE deleted = 0")
    Integer countUsers();

    @DS("question")
    @Select("SELECT COUNT(*) FROM question WHERE deleted = 0 AND status = 1")
    Integer countEnabledQuestions();

    @DS("question")
    @Select("SELECT COUNT(*) FROM question WHERE deleted = 0 AND difficulty = 1")
    Integer countEasyQuestions();

    @DS("question")
    @Select("SELECT COUNT(*) FROM question WHERE deleted = 0 AND difficulty = 2")
    Integer countMediumQuestions();

    @DS("question")
    @Select("SELECT COUNT(*) FROM question WHERE deleted = 0 AND difficulty = 3")
    Integer countHardQuestions();

    @DS("user")
    @Select("SELECT COUNT(*) FROM user WHERE deleted = 0 AND status = 1")
    Integer countActiveUsers();

    @DS("user")
    @Select("SELECT COUNT(*) FROM user WHERE deleted = 0 AND status = 0")
    Integer countDisabledUsers();

    @DS("question")
    @Select("SELECT c.name as categoryName, COUNT(q.id) as questionCount " +
            "FROM category c " +
            "LEFT JOIN question q ON c.id = q.category_id AND q.deleted = 0 " +
            "WHERE c.deleted = 0 " +
            "GROUP BY c.id, c.name " +
            "ORDER BY questionCount DESC")
    List<Map<String, Object>> getCategoryStats();

    @DS("question")
    @Select("SELECT COUNT(*) FROM question WHERE deleted = 0 AND create_time >= CURDATE()")
    Integer countTodayQuestions();

    @DS("user")
    @Select("SELECT COUNT(*) FROM feedback WHERE status = 0")
    Integer countPendingFeedback();

    @DS("practice")
    @Select("SELECT COUNT(DISTINCT user_id) FROM practice_record " +
            "WHERE deleted = 0 AND practice_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)")
    Integer countWeekActiveUsers();

    @DS("practice")
    @Select("SELECT COUNT(DISTINCT user_id) FROM practice_record " +
            "WHERE deleted = 0 AND practice_time >= CURDATE()")
    Integer countTodayActiveUsers();

    @DS("practice")
    @Select("SELECT user_id as userId, MAX(practice_time) as lastPracticeTime, COUNT(*) as practiceCount " +
            "FROM practice_record " +
            "WHERE deleted = 0 " +
            "GROUP BY user_id " +
            "ORDER BY lastPracticeTime DESC " +
            "LIMIT 8")
    List<Map<String, Object>> getRecentPracticeUsers();
}
