package com.poti.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poti.user.entity.DiscussionLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface DiscussionLikeMapper extends BaseMapper<DiscussionLike> {
    
    @Select("SELECT * FROM discussion_like WHERE discussion_id = #{discussionId} AND user_id = #{userId}")
    DiscussionLike selectByDiscussionAndUser(@Param("discussionId") Long discussionId, @Param("userId") Long userId);
    
    @Update("UPDATE discussion_like SET deleted = #{deleted} WHERE id = #{id}")
    int updateDeleted(@Param("id") Long id, @Param("deleted") Integer deleted);
}
