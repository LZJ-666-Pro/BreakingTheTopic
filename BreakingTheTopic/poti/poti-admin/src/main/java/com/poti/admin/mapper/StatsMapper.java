package com.poti.admin.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 数据统计专用 Mapper（只读聚合查询）。
 * <p>
 * 跨库方法逐个标注 {@code @DS}；同一 MySQL 实例下
 * 可在 practice 数据源连接中直接跨 schema JOIN poti_question 的表。
 */
@Mapper
public interface StatsMapper {

    // ==================== 用户数据（poti_user 库） ====================

    @DS("user")
    @Select("SELECT " +
            "COUNT(*) AS totalUsers, " +
            "SUM(create_time >= CURDATE()) AS todayNew, " +
            "SUM(create_time >= CURDATE() - INTERVAL 7 DAY) AS weekNew, " +
            "SUM(create_time >= CURDATE() - INTERVAL 30 DAY) AS monthNew, " +
            "SUM(last_login_time >= CURDATE()) AS dau, " +
            "SUM(last_login_time >= CURDATE() - INTERVAL 7 DAY) AS wau, " +
            "SUM(last_login_time >= CURDATE() - INTERVAL 30 DAY) AS mau " +
            "FROM user WHERE deleted = 0")
    Map<String, Object> selectUserOverview();

    @DS("user")
    @Select("SELECT COUNT(*) FROM user " +
            "WHERE deleted = 0 AND create_time < CURDATE() - INTERVAL 30 DAY " +
            "AND (last_login_time IS NULL OR last_login_time < CURDATE() - INTERVAL 30 DAY)")
    Long countLostUsers();

    /** 累计回访留存（近似口径）：注册后第 N 天及以后仍有过登录的用户占比 */
    @DS("user")
    @Select("SELECT " +
            "ROUND(SUM(DATEDIFF(last_login_time, create_time) >= 1) / COUNT(*) * 100, 1) AS d1, " +
            "ROUND(SUM(DATEDIFF(last_login_time, create_time) >= 7) / COUNT(*) * 100, 1) AS d7, " +
            "ROUND(SUM(DATEDIFF(last_login_time, create_time) >= 30) / COUNT(*) * 100, 1) AS d30 " +
            "FROM user WHERE deleted = 0")
    Map<String, Object> selectRetention();

    @DS("user")
    @Select("SELECT DATE_FORMAT(create_time, '%m-%d') AS day, COUNT(*) AS cnt " +
            "FROM user WHERE deleted = 0 AND create_time >= CURDATE() - INTERVAL 29 DAY " +
            "GROUP BY day ORDER BY MIN(create_time)")
    List<Map<String, Object>> selectUserTrend();

    /** 连续打卡天数分布（基于学习统计表的当前连续天数） */
    @DS("user")
    @Select("SELECT " +
            "SUM(consecutive_days BETWEEN 1 AND 3) AS s1to3, " +
            "SUM(consecutive_days BETWEEN 4 AND 7) AS s4to7, " +
            "SUM(consecutive_days BETWEEN 8 AND 14) AS s8to14, " +
            "SUM(consecutive_days >= 15) AS s15plus " +
            "FROM user_stats WHERE deleted = 0 AND user_id IN (SELECT id FROM user WHERE deleted = 0)")
    Map<String, Object> selectStreakDistribution();

    // ==================== 刷题行为数据（poti_practice 库，跨库 JOIN question） ====================

    @DS("practice")
    @Select("SELECT " +
            "COUNT(*) AS totalSubmits, " +
            "SUM(is_correct = 1) AS totalCorrect, " +
            "SUM(practice_time >= CURDATE()) AS todaySubmits, " +
            "SUM(practice_time >= CURDATE() - INTERVAL WEEKDAY(CURDATE()) DAY) AS weekSubmits, " +
            "ROUND(AVG(spend_seconds), 1) AS avgSpendSeconds, " +
            "ROUND(COUNT(*) / GREATEST((SELECT COUNT(*) FROM poti_user.user WHERE deleted = 0), 1), 1) AS avgPerUser " +
            "FROM practice_record WHERE deleted = 0")
    Map<String, Object> selectPracticeOverview();

    @DS("practice")
    @Select("SELECT DATE_FORMAT(practice_time, '%m-%d') AS day, COUNT(*) AS cnt " +
            "FROM practice_record WHERE deleted = 0 AND practice_time >= CURDATE() - INTERVAL 13 DAY " +
            "GROUP BY day ORDER BY MIN(practice_time)")
    List<Map<String, Object>> selectPracticeTrend();

    /** 各难度：提交量、通过量、通过率 */
    @DS("practice")
    @Select("SELECT q.difficulty, COUNT(*) AS submits, SUM(p.is_correct = 1) AS correct " +
            "FROM practice_record p JOIN poti_question.question q ON p.question_id = q.id " +
            "WHERE p.deleted = 0 GROUP BY q.difficulty")
    List<Map<String, Object>> selectDifficultyStats();

    /** 各分类刷题热度 TOP8 */
    @DS("practice")
    @Select("SELECT c.name, COUNT(*) AS cnt " +
            "FROM practice_record p " +
            "JOIN poti_question.question q ON p.question_id = q.id " +
            "JOIN poti_question.category c ON q.category_id = c.id " +
            "WHERE p.deleted = 0 GROUP BY q.category_id, c.name ORDER BY cnt DESC LIMIT 8")
    List<Map<String, Object>> selectCategoryHot();

    /** 热门题目 TOP10（题干截断 50 字） */
    @DS("practice")
    @Select("SELECT q.id, LEFT(REPLACE(q.content, CHAR(10), ' '), 50) AS content, q.difficulty, " +
            "COUNT(*) AS submits, SUM(p.is_correct = 1) AS correct " +
            "FROM practice_record p JOIN poti_question.question q ON p.question_id = q.id " +
            "WHERE p.deleted = 0 GROUP BY q.id, q.content, q.difficulty ORDER BY submits DESC LIMIT 10")
    List<Map<String, Object>> selectHotQuestions();

    // ==================== 题库数据（poti_question 库） ====================

    @DS("question")
    @Select("SELECT COUNT(*) AS totalQuestions, " +
            "SUM(status = 1) AS enabledQuestions, " +
            "SUM(view_count) AS totalViews " +
            "FROM question WHERE deleted = 0")
    Map<String, Object> selectQuestionOverview();

    // ==================== 特训营数据（poti_interview 库） ====================

    @DS("interview")
    @Select("SELECT " +
            "(SELECT COUNT(*) FROM camp_member WHERE deleted = 0) AS totalMembers, " +
            "(SELECT COUNT(*) FROM camp_member WHERE deleted = 0 AND status = 2) AS finishedMembers, " +
            "(SELECT COUNT(*) FROM camp_checkin) AS totalCheckins, " +
            "(SELECT COUNT(DISTINCT user_id) FROM camp_checkin WHERE checkin_date = CURDATE()) AS todayCheckinUsers")
    Map<String, Object> selectCampOverview();
}
