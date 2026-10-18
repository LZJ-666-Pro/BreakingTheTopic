package com.poti.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 竞赛作答记录实体（poti_practice.competition_record）。
 * question_ids / answers 为 JSON 列，后台管理端不读表明细，用 String 承接。
 */
@Data
@TableName("competition_record")
public class CompetitionRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long competitionId;

    private Long userId;

    /** 开赛时抽到的题目ID快照（JSON） */
    private String questionIds;

    /** 用户提交答案（JSON） */
    private String answers;

    /** 得分（未交卷为 NULL） */
    private Integer score;

    /** 答对题数 */
    private Integer correctCount;

    /** 用时（秒） */
    private Integer costSeconds;

    /** 交卷时间（未交卷为 NULL） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
