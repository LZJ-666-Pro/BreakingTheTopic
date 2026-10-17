package com.poti.agent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poti.agent.dto.AgentAnswer;
import com.poti.agent.entity.AgentMessage;
import com.poti.agent.entity.AgentSession;
import com.poti.agent.entity.AgentToolLog;
import com.poti.agent.mapper.AgentMessageMapper;
import com.poti.agent.mapper.AgentSessionMapper;
import com.poti.agent.mapper.AgentToolLogMapper;
import com.poti.agent.service.tools.AgentTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 助手会话与编排入口：限额 → 持久化 → LLM 调用（框架托管工具循环）→ 结构化解析 → 落库
 */
@Slf4j
@Service
public class AgentChatService {

    private final AgentSessionMapper sessionMapper;
    private final AgentMessageMapper messageMapper;
    private final AgentToolLogMapper toolLogMapper;
    private final Assistant assistant;
    private final AgentTools agentTools;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${agent.limits.daily-question-limit:20}")
    private int dailyQuestionLimit;

    public AgentChatService(AgentSessionMapper sessionMapper,
                            AgentMessageMapper messageMapper,
                            AgentToolLogMapper toolLogMapper,
                            Assistant assistant,
                            AgentTools agentTools,
                            JdbcTemplate jdbcTemplate) {
        this.sessionMapper = sessionMapper;
        this.messageMapper = messageMapper;
        this.toolLogMapper = toolLogMapper;
        this.assistant = assistant;
        this.agentTools = agentTools;
        this.jdbcTemplate = jdbcTemplate;
    }

    /** 新建会话 */
    public AgentSession createSession(Long userId) {
        AgentSession session = new AgentSession();
        session.setUserId(userId);
        session.setTitle("新对话");
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        sessionMapper.insert(session);
        return session;
    }

    /**
     * 提问。返回给前端的结构：
     * { sessionId, messageId, text, sources[], actions[], toolTrace[] }
     */
    public Map<String, Object> chat(Long userId, Long sessionId, String message) {
        // 1. 会话归属校验 / 创建
        AgentSession session;
        if (sessionId == null) {
            session = createSession(userId);
        } else {
            session = sessionMapper.selectById(sessionId);
            if (session == null || !session.getUserId().equals(userId)) {
                throw new IllegalArgumentException("会话不存在");
            }
        }

        // 2. R-2 每日提问限额
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        Long todayCount = messageMapper.selectCount(new LambdaQueryWrapper<AgentMessage>()
                .eq(AgentMessage::getUserId, userId)
                .eq(AgentMessage::getRole, "user")
                .ge(AgentMessage::getCreateTime, todayStart));
        if (todayCount >= dailyQuestionLimit) {
            throw new IllegalStateException("今日提问次数已用完（每日 " + dailyQuestionLimit + " 次），明天再来吧");
        }

        // 3. 用户消息落库
        AgentMessage userMsg = new AgentMessage();
        userMsg.setSessionId(session.getId());
        userMsg.setUserId(userId);
        userMsg.setRole("user");
        userMsg.setContent(message);
        userMsg.setCreateTime(LocalDateTime.now());
        messageMapper.insert(userMsg);

        // 4. 调用 LLM（工具循环由 LangChain4j 托管；上下文绑定当前用户，finally 清理）
        String raw;
        agentTools.bindContext(userId, session.getId());
        try {
            raw = assistant.chat(session.getId(), message);
        } finally {
            agentTools.clearContext();
        }

        // 5. 结构化解析（解析失败降级为纯文本回答，无卡片）
        AgentAnswer answer = parseAnswer(raw);

        // 6. 助手消息落库
        AgentMessage aiMsg = new AgentMessage();
        aiMsg.setSessionId(session.getId());
        aiMsg.setUserId(userId);
        aiMsg.setRole("assistant");
        aiMsg.setContent(answer.getText() != null ? answer.getText() : raw);
        aiMsg.setSources(toJson(answer.getSources()));
        aiMsg.setActions(toJson(answer.getActions()));
        aiMsg.setCreateTime(LocalDateTime.now());
        messageMapper.insert(aiMsg);

        // 7. 首条提问更新会话标题
        if ("新对话".equals(session.getTitle()) && message.length() > 0) {
            AgentSession update = new AgentSession();
            update.setId(session.getId());
            update.setTitle(message.length() > 30 ? message.substring(0, 30) : message);
            sessionMapper.updateById(update);
        }

        // 8. 组装响应
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("sessionId", session.getId());
        result.put("messageId", aiMsg.getId());
        result.put("text", aiMsg.getContent());
        result.put("sources", safeList(aiMsg.getSources()));
        result.put("actions", answer.getActions() != null ? answer.getActions() : List.of());
        return result;
    }

    /** 会话消息列表（用户侧历史，不含 LLM 内部工具消息） */
    public List<AgentMessage> getMessages(Long userId, Long sessionId) {
        AgentSession session = sessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            throw new IllegalArgumentException("会话不存在");
        }
        return messageMapper.selectList(new LambdaQueryWrapper<AgentMessage>()
                .eq(AgentMessage::getSessionId, sessionId)
                .orderByAsc(AgentMessage::getId));
    }

    /** 用户全部会话 */
    public List<AgentSession> getSessions(Long userId) {
        return sessionMapper.selectList(new LambdaQueryWrapper<AgentSession>()
                .eq(AgentSession::getUserId, userId)
                .orderByDesc(AgentSession::getUpdateTime));
    }

    /**
     * 删除会话：校验归属后硬删会话、消息、工具日志与 LLM 记忆（用户明确要求删除，不留软删残留）
     */
    public void deleteSession(Long userId, Long sessionId) {
        AgentSession session = sessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            throw new IllegalArgumentException("会话不存在");
        }
        messageMapper.delete(new LambdaQueryWrapper<AgentMessage>()
                .eq(AgentMessage::getSessionId, sessionId));
        toolLogMapper.delete(new LambdaQueryWrapper<AgentToolLog>()
                .eq(AgentToolLog::getSessionId, sessionId));
        jdbcTemplate.update("DELETE FROM agent_chat_memory WHERE memory_id = ?", String.valueOf(sessionId));
        sessionMapper.deleteById(sessionId);
    }

    // ==================== 内部方法 ====================

    private AgentAnswer parseAnswer(String raw) {
        AgentAnswer fallback = new AgentAnswer();
        fallback.setText(raw);
        fallback.setSources(List.of());
        fallback.setActions(List.of());
        if (raw == null || raw.isBlank()) {
            fallback.setText("抱歉，我暂时没有生成有效回答，请稍后重试。");
            return fallback;
        }
        try {
            String json = extractJson(raw);
            if (json == null) {
                return fallback;
            }
            AgentAnswer answer = objectMapper.readValue(json, AgentAnswer.class);
            if (answer.getText() == null || answer.getText().isBlank()) {
                return fallback;
            }
            if (answer.getSources() == null) {
                answer.setSources(List.of());
            }
            if (answer.getActions() == null) {
                answer.setActions(List.of());
            }
            return answer;
        } catch (Exception e) {
            log.warn("[agent] 回答 JSON 解析失败，降级为纯文本: {}", raw);
            return fallback;
        }
    }

    /** 提取文本中的 JSON 对象（容忍 ```json 包裹与前后缀文本） */
    private String extractJson(String raw) {
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start < 0 || end <= start) {
            return null;
        }
        return raw.substring(start, end + 1);
    }

    private String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return null;
        }
    }

    private List<Object> safeList(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            JsonNode node = objectMapper.readTree(json);
            List<Object> list = new ArrayList<>();
            node.forEach(list::add);
            return list;
        } catch (Exception e) {
            return List.of();
        }
    }
}
