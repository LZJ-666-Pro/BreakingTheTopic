package com.poti.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poti.user.entity.Friendship;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface FriendshipMapper extends BaseMapper<Friendship> {
    
    @Select("SELECT f.friend_id " +
            "FROM friendship f " +
            "WHERE f.user_id = #{userId} AND f.status = 1 AND f.deleted = 0")
    List<Long> getFriendIds(@Param("userId") Long userId);
    
    @Select("<script>" +
            "SELECT u.id as user_id, u.nickname, u.avatar_url, " +
            "IFNULL(us.total_questions, 0) as total_questions, " +
            "IFNULL(us.today_questions, 0) as today_questions, " +
            "CASE WHEN us.total_questions > 0 " +
            "THEN ROUND(us.correct_questions * 100.0 / us.total_questions, 2) " +
            "ELSE 0 END as accuracy, " +
            "IFNULL(us.consecutive_days, 0) as consecutive_days " +
            "FROM user u " +
            "LEFT JOIN user_stats us ON u.id = us.user_id " +
            "WHERE u.id IN " +
            "<foreach item='id' collection='friendIds' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " AND u.deleted = 0 " +
            "ORDER BY ${orderField} DESC " +
            "LIMIT #{offset}, #{limit}" +
            "</script>")
    List<Map<String, Object>> getFriendRankingByField(
            @Param("friendIds") List<Long> friendIds,
            @Param("orderField") String orderField,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);
    
    @Select("SELECT * FROM friendship WHERE user_id = #{userId} AND friend_id = #{friendId} LIMIT 1")
    Friendship selectByUserIdAndFriendId(@Param("userId") Long userId, @Param("friendId") Long friendId);
    
    @Update("UPDATE friendship SET deleted = 0, status = 1, update_time = NOW() WHERE id = #{id}")
    int restoreFriendship(@Param("id") Long id);
    
    @Select("SELECT u.id as user_id, u.nickname, u.avatar_url, " +
            "us.total_questions, us.today_questions, " +
            "CASE WHEN us.total_questions > 0 " +
            "THEN ROUND(us.correct_questions * 100.0 / us.total_questions, 2) " +
            "ELSE 0 END as accuracy, " +
            "us.consecutive_days, " +
            "COUNT(f2.friend_id) as mutual_friends_count " +
            "FROM user u " +
            "LEFT JOIN user_stats us ON u.id = us.user_id " +
            "LEFT JOIN friendship f1 ON f1.user_id = #{userId} AND f1.status = 1 AND f1.deleted = 0 " +
            "LEFT JOIN friendship f2 ON f2.user_id = u.id AND f2.friend_id = f1.friend_id AND f2.status = 1 AND f2.deleted = 0 " +
            "WHERE u.id != #{userId} " +
            "AND u.deleted = 0 " +
            "AND u.id NOT IN (SELECT friend_id FROM friendship WHERE user_id = #{userId} AND deleted = 0) " +
            "AND u.id NOT IN (SELECT friend_id FROM friend_request WHERE user_id = #{userId} AND status = 0) " +
            "GROUP BY u.id " +
            "ORDER BY mutual_friends_count DESC, us.total_questions DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getFriendRecommendations(@Param("userId") Long userId, @Param("limit") Integer limit);
}
