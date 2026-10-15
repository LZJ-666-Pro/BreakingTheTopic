package com.poti.agent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 助手消息
 */
@Data
@TableName("agent_message")
public class AgentMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sessionId;

    /** 冗余用户ID，供每日限额统计 */
    private Long userId;

    /** 角色：user / assistant */
    private String role;

    private String content;

    /** 引用标注 JSON 数组字符串 */
    private String sources;

    /** 动作卡片 JSON 数组字符串 */
    private String actions;

    /** 工具调用轨迹 JSON 数组字符串 */
    private String toolTrace;

    private LocalDateTime createTime;
}
