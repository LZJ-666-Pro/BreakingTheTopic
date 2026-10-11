package com.poti.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统公告实体（poti_admin.notice）。
 */
@Data
@TableName("notice")
public class Notice {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    /** 展示方式：0弹窗 1顶部横幅 2消息中心 */
    private Integer displayType;

    /** 目标用户：0全部用户 1会员用户 2指定用户 */
    private Integer targetType;

    /** 指定用户ID，逗号分隔（targetType=2 时生效） */
    private String targetUserIds;

    /** 1强制阅读（不可关闭） */
    private Integer forceRead;

    /** 1允许用户关闭 */
    private Integer closable;

    /** 生效时间（空=立即） */
    private LocalDateTime startTime;

    /** 失效时间（空=永久） */
    private LocalDateTime endTime;

    /** 1启用 0停用 */
    private Integer status;

    /** 阅读人数 */
    private Integer readCount;

    /** 点击次数 */
    private Integer clickCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
