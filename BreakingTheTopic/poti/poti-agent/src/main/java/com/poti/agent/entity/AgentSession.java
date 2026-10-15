package com.poti.agent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 助手会话
 */
@Data
@TableName("agent_session")
public class AgentSession {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户ID */
    private Long userId;

    /** 会话标题（首条提问截断） */
    private String title;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
