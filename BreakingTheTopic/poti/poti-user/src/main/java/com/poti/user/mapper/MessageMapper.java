package com.poti.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poti.user.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;

import java.util.List;
import java.util.Map;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
    
    @Select("SELECT m.*, u.nickname as sender_name, u.avatar_url as sender_avatar " +
            "FROM message m " +
            "LEFT JOIN user u ON m.sender_id = u.id " +
            "WHERE m.conversation_id = #{conversationId} AND m.deleted = 0 " +
            "ORDER BY m.create_time ASC " +
            "LIMIT #{offset}, #{limit}")
    List<Map<String, Object>> getMessages(@Param("conversationId") Long conversationId,
                                          @Param("offset") Integer offset,
                                          @Param("limit") Integer limit);
    
    @Update("UPDATE message SET status = 2 WHERE conversation_id = #{conversationId} AND receiver_id = #{userId} AND status = 1")
    int markAsRead(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
    
    @Delete("DELETE FROM message WHERE conversation_id = #{conversationId}")
    int deleteByConversationId(@Param("conversationId") Long conversationId);
}
