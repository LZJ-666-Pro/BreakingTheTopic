package com.poti.agent.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.poti.agent.entity.AgentMessage;
import com.poti.agent.entity.AgentSession;
import com.poti.agent.entity.AgentToolLog;
import com.poti.agent.mapper.AgentMessageMapper;
import com.poti.agent.mapper.AgentSessionMapper;
import com.poti.agent.mapper.AgentToolLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Agent 过期会话定时清理：
 * poti_agent 库的 session/message/tool_log/chat_memory 四表随对话无限增长，
 * 每日凌晨硬删「超过保留期未活跃」的会话及其全部关联数据（与手动删除会话的清理范围一致）。
 * 保留期默认 90 天，可经 agent.cleanup.retention-days 配置。
 */
@Slf4j
@Component
public class AgentCleanupTask {

    /** 单批清理的会话数（避免一次性大事务长锁表） */
    private static final int BATCH_SIZE = 500;
    /** 单次执行的最大批次数（防止极端积压时任务长时间占用连接） */
    private static final int MAX_BATCHES = 40;

    private final AgentSessionMapper sessionMapper;
    private final AgentMessageMapper messageMapper;
    private final AgentToolLogMapper toolLogMapper;
    private final JdbcTemplate jdbcTemplate;

    @Value("${agent.cleanup.retention-days:90}")
    private int retentionDays;

    public AgentCleanupTask(AgentSessionMapper sessionMapper,
                            AgentMessageMapper messageMapper,
                            AgentToolLogMapper toolLogMapper,
                            JdbcTemplate jdbcTemplate) {
        this.sessionMapper = sessionMapper;
        this.messageMapper = messageMapper;
        this.toolLogMapper = toolLogMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    /** 每日凌晨 3 点执行，可经 agent.cleanup.cron 覆盖 */
    @Scheduled(cron = "${agent.cleanup.cron:0 0 3 * * ?}")
    public void cleanupExpiredSessions() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        long start = System.currentTimeMillis();
        int totalSessions = 0;
        try {
            for (int batch = 0; batch < MAX_BATCHES; batch++) {
                // 仅取主键，避免大字段加载；按更新时间最旧优先
                List<AgentSession> expired = sessionMapper.selectList(new LambdaQueryWrapper<AgentSession>()
                        .select(AgentSession::getId)
                        .lt(AgentSession::getUpdateTime, cutoff)
                        .orderByAsc(AgentSession::getUpdateTime)
                        .last("LIMIT " + BATCH_SIZE));
                if (expired.isEmpty()) {
                    break;
                }
                List<Long> ids = expired.stream().map(AgentSession::getId).toList();
                // 清理顺序：子表在前，会话在后（与 deleteSession 手动删除保持一致）
                messageMapper.delete(new LambdaQueryWrapper<AgentMessage>()
                        .in(AgentMessage::getSessionId, ids));
                toolLogMapper.delete(new LambdaQueryWrapper<AgentToolLog>()
                        .in(AgentToolLog::getSessionId, ids));
                String placeholders = ids.stream()
                        .map(id -> "?")
                        .collect(java.util.stream.Collectors.joining(","));
                jdbcTemplate.update("DELETE FROM agent_chat_memory WHERE memory_id IN ("
                        + placeholders + ")", ids.stream().map(String::valueOf).toArray());
                sessionMapper.delete(new LambdaQueryWrapper<AgentSession>()
                        .in(AgentSession::getId, ids));
                totalSessions += ids.size();
            }
            if (totalSessions > 0) {
                log.info("[agent-cleanup] 过期会话清理完成: 保留期{}天, 清理会话{}个, 耗时{}ms",
                        retentionDays, totalSessions, System.currentTimeMillis() - start);
            }
        } catch (Exception e) {
            log.error("[agent-cleanup] 过期会话清理失败: 已清理{}个后中断", totalSessions, e);
        }
    }
}
