package com.poti.practice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 竞赛报名实体（competition_id + user_id 唯一索引保证仅可报名一次）
 */
@Data
@TableName("competition_signup")
public class CompetitionSignup {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long competitionId;

    private Long userId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
