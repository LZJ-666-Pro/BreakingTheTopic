package com.poti.admin.dto;

import lombok.Data;

/**
 * 用户数据重置请求：勾选要重置的数据维度。
 */
@Data
public class UserResetDataRequest {

    /** 刷题记录（练习明细 + 学习统计归零） */
    private Boolean practice;

    /** 错题本 */
    private Boolean wrongbook;

    /** 收藏 */
    private Boolean favorite;

    /** 签到记录（含连续天数） */
    private Boolean checkin;

    /** 讨论记录（含发出的评论与点赞） */
    private Boolean discussion;

    public boolean hasAny() {
        return Boolean.TRUE.equals(practice) || Boolean.TRUE.equals(wrongbook)
                || Boolean.TRUE.equals(favorite) || Boolean.TRUE.equals(checkin)
                || Boolean.TRUE.equals(discussion);
    }
}
