package com.poti.agent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 助手工具调度日志：LLM 每次执行业务工具记录一条
 */
@Data
@TableName("agent_tool_log")
public class AgentToolLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long sessionId;

    private String toolName;

    /** 工具入参 JSON */
    private String params;

    /** 执行耗时毫秒 */
    private Integer costMs;

    /** 第几轮工具调用 */
    private Integer roundNo;

    /** 是否成功：1是 0否 */
    private Integer success;

    private LocalDateTime createTime;
}
