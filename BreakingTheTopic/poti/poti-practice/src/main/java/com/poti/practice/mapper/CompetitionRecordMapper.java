package com.poti.practice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poti.practice.entity.CompetitionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface CompetitionRecordMapper extends BaseMapper<CompetitionRecord> {

    /**
     * 统计同竞赛中排名在我之前的记录数（rank = 该数 + 1）
     * 排序规则：分数更高者优先；同分用时短者优先
     */
    @Select("SELECT COUNT(*) FROM competition_record " +
            "WHERE competition_id = #{competitionId} AND score IS NOT NULL " +
            "AND (score > #{score} OR (score = #{score} AND cost_seconds < #{costSeconds}))")
    Integer countAhead(@Param("competitionId") Long competitionId,
                       @Param("score") Integer score,
                       @Param("costSeconds") Integer costSeconds);

    /**
     * 我的竞赛记录列表（已交卷），关联竞赛标题并带排名子查询
     */
    @Select("SELECT r.id, r.competition_id AS competitionId, c.title, r.score, " +
            "r.correct_count AS correctCount, r.cost_seconds AS costSeconds, r.submit_time AS submitTime, " +
            "(SELECT COUNT(*) + 1 FROM competition_record r2 " +
            " WHERE r2.competition_id = r.competition_id AND r2.score IS NOT NULL " +
            " AND (r2.score > r.score OR (r2.score = r.score AND r2.cost_seconds < r.cost_seconds))) AS `rank` " +
            "FROM competition_record r " +
            "JOIN competition c ON c.id = r.competition_id AND c.deleted = 0 " +
            "WHERE r.user_id = #{userId} AND r.submit_time IS NOT NULL " +
            "ORDER BY r.submit_time DESC " +
            "LIMIT #{offset}, #{size}")
    List<Map<String, Object>> selectMyList(@Param("userId") Long userId,
                                           @Param("offset") Integer offset,
                                           @Param("size") Integer size);

    /** 我的已交卷记录总数 */
    @Select("SELECT COUNT(*) FROM competition_record WHERE user_id = #{userId} AND submit_time IS NOT NULL")
    Long countMySubmitted(@Param("userId") Long userId);
}
