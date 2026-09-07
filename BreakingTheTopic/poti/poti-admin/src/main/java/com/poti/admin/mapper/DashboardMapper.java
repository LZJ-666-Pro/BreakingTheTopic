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
}
