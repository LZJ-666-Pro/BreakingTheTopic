package com.poti.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poti.user.entity.FriendRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface FriendRequestMapper extends BaseMapper<FriendRequest> {
    
    @Select("SELECT fr.*, u.nickname, u.avatar_url, us.total_points " +
            "FROM friend_request fr " +
            "LEFT JOIN user u ON fr.user_id = u.id " +
            "LEFT JOIN user_stats us ON fr.user_id = us.user_id " +
            "WHERE fr.friend_id = #{friendId} AND fr.status = 0 AND fr.deleted = 0 " +
            "ORDER BY fr.create_time DESC")
    List<Map<String, Object>> getPendingRequests(@Param("friendId") Long friendId);
    
    @Select("SELECT fr.*, u.nickname, u.avatar_url, us.total_points, " +
            "CASE fr.status WHEN 0 THEN '待处理' WHEN 1 THEN '已接受' WHEN 2 THEN '已拒绝' END as status_text " +
            "FROM friend_request fr " +
            "LEFT JOIN user u ON fr.friend_id = u.id " +
            "LEFT JOIN user_stats us ON fr.friend_id = us.user_id " +
            "WHERE fr.user_id = #{userId} AND fr.deleted = 0 " +
            "ORDER BY fr.create_time DESC " +
            "LIMIT 50")
    List<Map<String, Object>> getSentRequests(@Param("userId") Long userId);
}
