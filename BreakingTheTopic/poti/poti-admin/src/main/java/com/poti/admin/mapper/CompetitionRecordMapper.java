package com.poti.admin.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poti.admin.entity.CompetitionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 竞赛作答记录 Mapper（跨库 poti_practice.competition_record）。
 */
@Mapper
@DS("practice")
public interface CompetitionRecordMapper extends BaseMapper<CompetitionRecord> {

    /** 批量统计各竞赛已交卷记录数（submit_time 非空才算交卷） */
    @Select("<script>SELECT competition_id AS competitionId, COUNT(*) AS cnt FROM competition_record " +
            "WHERE submit_time IS NOT NULL AND competition_id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach> " +
            "GROUP BY competition_id</script>")
    List<Map<String, Object>> selectSubmitCountBatch(@Param("ids") List<Long> ids);
}
