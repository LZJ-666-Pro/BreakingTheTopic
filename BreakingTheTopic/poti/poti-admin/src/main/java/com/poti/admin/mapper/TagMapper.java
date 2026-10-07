package com.poti.admin.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poti.admin.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 题目标签 Mapper（poti_question 库）。
 */
@Mapper
@DS("question")
public interface TagMapper extends BaseMapper<Tag> {

    /**
     * 统计某标签关联的启用题目数。
     * <p>
     * question.tags 为逗号分隔字段（可能含全角逗号与空格），先规范化为
     * ",tag1,tag2," 形式再做精确词匹配，避免 "Java" 误匹配 "JavaScript"。
     * </p>
     */
    @Select("SELECT COUNT(*) FROM question WHERE deleted = 0 AND status = 1 " +
            "AND CONCAT(',', REPLACE(REPLACE(REPLACE(IFNULL(tags, ''), '， ', '，'), '，', ','), ' ', ''), ',') " +
            "LIKE CONCAT('%,', #{name}, ',%')")
    long countQuestionsByTag(@Param("name") String name);

    /** 查题目当前 tags 字段 */
    @Select("SELECT IFNULL(tags, '') FROM question WHERE id = #{id} AND deleted = 0")
    String getQuestionTags(@Param("id") Long id);

    /** 更新题目 tags 字段（批量打/移除标签用） */
    @Update("UPDATE question SET tags = #{tags} WHERE id = #{id} AND deleted = 0")
    int updateQuestionTags(@Param("id") Long id, @Param("tags") String tags);
}
