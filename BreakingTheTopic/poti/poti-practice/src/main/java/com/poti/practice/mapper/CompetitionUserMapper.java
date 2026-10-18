package com.poti.practice.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 跨库查询 poti_user.user 表（昵称/头像），通过 @DS("user") 切换到 user 数据源
 */
@DS("user")
@Mapper
public interface CompetitionUserMapper {

    /** 按用户ID批量查询昵称与头像（仅未删除且正常状态用户） */
    @Select("<script>" +
            "SELECT id, nickname, avatar_url AS avatarUrl FROM user " +
            "WHERE deleted = 0 AND status = 1 AND id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    List<Map<String, Object>> selectNicknameAndAvatar(@Param("ids") List<Long> ids);
}
