package com.poti.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poti.user.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {
    
    @Select("SELECT c.*, " +
            "CASE WHEN c.user1_id = #{userId} THEN c.user2_id ELSE c.user1_id END as other_user_id, " +
            "u.nickname, u.avatar_url " +
            "FROM conversation c " +
            "LEFT JOIN user u ON (CASE WHEN c.user1_id = #{userId} THEN c.user2_id ELSE c.user1_id END) = u.id " +
            "WHERE (c.user1_id = #{userId} OR c.user2_id = #{userId}) AND c.deleted = 0 " +
            "ORDER BY c.last_message_time DESC")
    List<Map<String, Object>> getConversationList(@Param("userId") Long userId);
    
    @Select("SELECT * FROM conversation " +
            "WHERE ((user1_id = #{userId1} AND user2_id = #{userId2}) " +
            "OR (user1_id = #{userId2} AND user2_id = #{userId1})) " +
            "AND deleted = 0 LIMIT 1")
    Conversation findByUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
    
    @Update("UPDATE conversation SET " +
            "last_message = #{lastMessage}, " +
            "last_message_time = NOW(), " +
            "user1_unread = user1_unread + IF(user1_id = #{senderId}, 0, 1), " +
            "user2_unread = user2_unread + IF(user2_id = #{senderId}, 0, 1) " +
            "WHERE id = #{conversationId}")
    int updateLastMessage(@Param("conversationId") Long conversationId, 
                          @Param("lastMessage") String lastMessage,
                          @Param("senderId") Long senderId);
    
    @Update("UPDATE conversation SET " +
            "user1_unread = IF(user1_id = #{userId}, 0, user1_unread), " +
            "user2_unread = IF(user2_id = #{userId}, 0, user2_unread) " +
            "WHERE id = #{conversationId}")
    int clearUnread(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
}
