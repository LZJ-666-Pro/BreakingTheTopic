package com.poti.admin.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poti.admin.entity.CompetitionSignup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 竞赛报名 Mapper（跨库 poti_practice.competition_signup）。
 */
@Mapper
@DS("practice")
public interface CompetitionSignupMapper extends BaseMapper<CompetitionSignup> {

    /** 批量统计各竞赛报名人数（避免逐行查询） */
    @Select("<script>SELECT competition_id AS competitionId, COUNT(*) AS cnt FROM competition_signup " +
            "WHERE competition_id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach> " +
            "GROUP BY competition_id</script>")
    List<Map<String, Object>> selectSignupCountBatch(@Param("ids") List<Long> ids);
}
