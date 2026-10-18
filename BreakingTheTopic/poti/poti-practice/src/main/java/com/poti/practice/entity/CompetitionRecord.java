package com.poti.practice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 竞赛作答记录实体：开赛即生成（题目ID快照），交卷后补全判分字段
 */
@Data
@TableName("competition_record")
public class CompetitionRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long competitionId;

    private Long userId;

    /** 开赛时抽到的题目ID快照（JSON数组字符串） */
    private String questionIds;

    /** 用户提交答案 [{questionId,answer}]（JSON字符串） */
    private String answers;

    /** 得分 */
    private Integer score;

    /** 答对题数 */
    private Integer correctCount;

    /** 用时（秒） */
    private Integer costSeconds;

    /** 交卷时间（未交卷为 null） */
    private LocalDateTime submitTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
