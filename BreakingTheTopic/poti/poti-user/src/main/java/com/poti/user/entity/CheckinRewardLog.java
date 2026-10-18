package com.poti.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 签到奖励发放记录：连签里程碑（3/7/15/30 天）达成时送会员时长。
 * 幂等：user_id + streak_days + cycle_start_date 唯一，断签归零后 cycle_start_date 变化可循环再得。
 */
@Data
@TableName("checkin_reward_log")
public class CheckinRewardLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 达成里程碑的连签天数（3/7/15/30） */
    private Integer streakDays;

    /** 发放的会员天数 */
    private Integer rewardDays;

    /** 本轮连签起始日期 */
    private LocalDate cycleStartDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
