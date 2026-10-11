package com.poti.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题目题解实体（poti_question.solution）。
 */
@Data
@TableName("solution")
public class Solution {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联题目ID */
    private Long questionId;

    /** 提交用户ID，0=官方 */
    private Long userId;

    /** 作者昵称（冗余） */
    private String authorName;

    private String title;

    /** 题解内容（Markdown） */
    private String content;

    /** 1官方题解 0用户提交 */
    private Integer isOfficial;

    /** 审核状态：0待审核 1已通过 2已拒绝 */
    private Integer status;

    /** 拒绝原因 */
    private String rejectReason;

    /** 1优质题解 */
    private Integer quality;

    /** 1置顶题解 */
    private Integer pinned;

    private Integer viewCount;

    private Integer likeCount;

    private Integer starCount;

    private Integer reportCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
