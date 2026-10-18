package com.poti.practice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poti.practice.entity.CompetitionSignup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface CompetitionSignupMapper extends BaseMapper<CompetitionSignup> {

    /** 按竞赛ID批量统计报名人数（一次聚合查询，避免列表页 N+1） */
    @Select("<script>" +
            "SELECT competition_id AS competitionId, COUNT(*) AS cnt FROM competition_signup " +
            "WHERE competition_id IN " +
            "<foreach collection='competitionIds' item='cid' open='(' separator=',' close=')'>#{cid}</foreach> " +
            "GROUP BY competition_id" +
            "</script>")
    List<Map<String, Object>> countByCompetitionIds(@Param("competitionIds") List<Long> competitionIds);
}
