package com.poti.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poti.user.entity.UserAchievement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserAchievementMapper extends BaseMapper<UserAchievement> {
    
    @Select("SELECT ua.*, a.achievement_code, a.name, a.icon, a.description, a.category, a.type, " +
            "a.condition_value, a.reward_points " +
            "FROM user_achievement ua " +
            "LEFT JOIN achievement a ON ua.achievement_id = a.id " +
            "WHERE ua.user_id = #{userId} AND ua.deleted = 0 AND a.deleted = 0 " +
            "ORDER BY a.sort_order")
    List<Map<String, Object>> getUserAchievements(@Param("userId") Long userId);
    
    @Select("SELECT COUNT(*) as total, " +
            "SUM(CASE WHEN ua.is_unlocked = 1 THEN 1 ELSE 0 END) as achieved, " +
            "SUM(CASE WHEN ua.is_unlocked = 1 THEN a.reward_points ELSE 0 END) as points " +
            "FROM user_achievement ua " +
            "LEFT JOIN achievement a ON ua.achievement_id = a.id " +
            "WHERE ua.user_id = #{userId} AND ua.deleted = 0 AND a.deleted = 0")
    Map<String, Object> getAchievementSummary(@Param("userId") Long userId);
}
