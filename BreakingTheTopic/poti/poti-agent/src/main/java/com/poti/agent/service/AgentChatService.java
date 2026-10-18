package com.poti.agent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poti.agent.dto.AgentAnswer;
import com.poti.agent.entity.AgentMessage;
import com.poti.agent.entity.AgentSession;
import com.poti.agent.entity.AgentToolLog;
import com.poti.agent.feign.UserFeignClient;
import com.poti.agent.mapper.AgentMessageMapper;
import com.poti.agent.mapper.AgentSessionMapper;
import com.poti.agent.mapper.AgentToolLogMapper;
import com.poti.agent.service.tools.AgentTools;
import com.poti.common.R;
import dev.langchain4j.service.TokenStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 助手会话与编排入口：限额 → 持久化 → LLM 调用（框架托管工具循环）→ 结构化解析 → 落库
 * 双链路：chat() 同步整体返回（兼容/降级）；chatStream() TokenStream→SSE 真流式
 */
@Slf4j
@Service
public class AgentChatService {

    /** VIP 用户每日提问上限（会员权益：普通用户 20 次 → VIP 100 次） */
    private static final int VIP_DAILY_LIMIT = 100;

    /** SSE 推送超时：LLM 最长 60s + 工具循环余量 */
    private static final long SSE_TIMEOUT_MS = 120_000L;

    private final AgentSessionMapper sessionMapper;
    private final AgentMessageMapper messageMapper;
    private final AgentToolLogMapper toolLogMapper;
    private final Assistant assistant;
    private final AgentTools agentTools;
    private final UserFeignClient userFeignClient;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${agent.limits.daily-question-limit:20}")
    private int dailyQuestionLimit;

    public AgentChatService(AgentSessionMapper sessionMapper,
                            AgentMessageMapper messageMapper,
                            AgentToolLogMapper toolLogMapper,
                            Assistant assistant,
                            AgentTools agentTools,
                            UserFeignClient userFeignClient,
                            JdbcTemplate jdbcTemplate) {
        this.sessionMapper = sessionMapper;
        this.messageMapper = messageMapper;
        this.toolLogMapper = toolLogMapper;
        this.assistant = assistant;
        this.agentTools = agentTools;
        this.userFeignClient = userFeignClient;
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
     * 提问（同步整体返回，前端降级/兼容链路）。返回给前端的结构：
     * { sessionId, messageId, text, sources[], actions[] }
     */
    public Map<String, Object> chat(Long userId, Long sessionId, String message) {
        AgentSession session = prepareChat(userId, sessionId, message);

        // 调用 LLM（工具循环由 LangChain4j 托管；上下文按 sessionId 绑定，finally 清理）
        String raw;
        agentTools.bindContext(userId, session.getId());
        try {
            raw = assistant.chat(session.getId(), message);
        } finally {
            agentTools.clearContext(session.getId());
        }

        AgentAnswer answer = parseAnswer(raw);
        AgentMessage aiMsg = persistAssistantMessage(session, answer, raw);
        updateSessionTitleIfNeeded(session, message);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("sessionId", session.getId());
        result.put("messageId", aiMsg.getId());
        result.put("text", aiMsg.getContent());
        result.put("sources", safeList(aiMsg.getSources()));
        result.put("actions", answer.getActions() != null ? answer.getActions() : List.of());
        return result;
    }

    /**
     * 提问（真流式）：前置校验/限额/用户消息落库同步完成后返回 SseEmitter，
     * LLM 逐 token 经 TokenStream 回调推送；完成时持久化助手消息并下发 meta（sources/actions）。
     *
     * SSE 事件统一用 data 行包裹 JSON 信封，前端只解析 data 行：
     * {"t":"delta","d":"片段"} / {"t":"meta",sessionId,messageId,sources,actions} / {"t":"error","m":"原因"}
     */
    public SseEmitter chatStream(Long userId, Long sessionId, String message) {
        AgentSession session = prepareChat(userId, sessionId, message);
        Long sid = session.getId();

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        StringBuilder full = new StringBuilder();
        // 上下文清理幂等：onError/onComplete 里清 + emitter 生命周期回调兜底（超时/客户端断开）
        Runnable cleanup = () -> agentTools.clearContext(sid);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(t -> cleanup.run());

        agentTools.bindContext(userId, sid);
        try {
            TokenStream stream = assistant.chatStream(sid, message);
            stream.onPartialResponse(token -> {
                full.append(token);
                try {
                    emitter.send(sse(Map.of("t", "delta", "d", token)));
                } catch (Exception e) {
                    log.warn("[agent] SSE 推送失败（客户端可能已断开）: sessionId={}", sid);
                }
            }).onCompleteResponse(response -> {
                try {
                    String raw = response.aiMessage().text();
                    if (raw == null || raw.isBlank()) {
                        raw = full.toString();
                    }
                    AgentAnswer answer = parseAnswer(raw);
                    AgentMessage aiMsg = persistAssistantMessage(session, answer, raw);
                    updateSessionTitleIfNeeded(session, message);

                    Map<String, Object> meta = new LinkedHashMap<>();
                    meta.put("t", "meta");
                    meta.put("sessionId", sid);
                    meta.put("messageId", aiMsg.getId());
                    meta.put("sources", safeList(aiMsg.getSources()));
                    meta.put("actions", answer.getActions() != null ? answer.getActions() : List.of());
                    emitter.send(sse(meta));
                    emitter.complete();
                } catch (Exception e) {
                    log.error("[agent] 流式收尾失败: sessionId={}", sid, e);
                    emitter.completeWithError(e);
                }
            }).onError(error -> {
                log.error("[agent] 流式回答失败: sessionId={}", sid, error);
                try {
                    emitter.send(sse(Map.of("t", "error", "m", "助手开小差了，请稍后再试")));
                    emitter.complete();
                } catch (Exception ignored) {
                    // 连接已断，无需处理
                }
            }).start();
        } catch (Exception e) {
            agentTools.clearContext(sid);
            throw e;
        }
        return emitter;
    }

    // ==================== 内部方法 ====================

    /**
     * 提问前置：会话归属校验/创建 → 每日限额（VIP 100 次、普通 20 次）→ 用户消息落库。
     * 同步/流式两链路共用；异常直接抛出，由 Controller 转 R.error。
     */
    private AgentSession prepareChat(Long userId, Long sessionId, String message) {
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

        // 2. R-2 每日提问限额（VIP 每日 100 次 / 普通 20 次，会员状态经 Feign 查询）
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        Long todayCount = messageMapper.selectCount(new LambdaQueryWrapper<AgentMessage>()
                .eq(AgentMessage::getUserId, userId)
                .eq(AgentMessage::getRole, "user")
                .ge(AgentMessage::getCreateTime, todayStart));
        int dailyLimit = resolveDailyLimit(userId);
        if (todayCount >= dailyLimit) {
            throw new IllegalStateException("今日提问次数已用完（VIP 每日 " + VIP_DAILY_LIMIT
                    + " 次 / 普通 " + dailyQuestionLimit + " 次），明天再来吧");
        }

        // 3. 用户消息落库
        AgentMessage userMsg = new AgentMessage();
        userMsg.setSessionId(session.getId());
        userMsg.setUserId(userId);
        userMsg.setRole("user");
        userMsg.setContent(message);
        userMsg.setCreateTime(LocalDateTime.now());
        messageMapper.insert(userMsg);
        return session;
    }

    /** 助手消息落库（同步/流式共用）：文本取结构化 text，解析失败降级为原始回答 */
    private AgentMessage persistAssistantMessage(AgentSession session, AgentAnswer answer, String raw) {
        AgentMessage aiMsg = new AgentMessage();
        aiMsg.setSessionId(session.getId());
        aiMsg.setUserId(session.getUserId());
        aiMsg.setRole("assistant");
        aiMsg.setContent(answer.getText() != null ? answer.getText() : raw);
        aiMsg.setSources(toJson(answer.getSources()));
        aiMsg.setActions(toJson(answer.getActions()));
        aiMsg.setCreateTime(LocalDateTime.now());
        messageMapper.insert(aiMsg);
        return aiMsg;
    }

    /** 首条提问更新会话标题 */
    private void updateSessionTitleIfNeeded(AgentSession session, String message) {
        if ("新对话".equals(session.getTitle()) && message.length() > 0) {
            AgentSession update = new AgentSession();
            update.setId(session.getId());
            update.setTitle(message.length() > 30 ? message.substring(0, 30) : message);
            sessionMapper.updateById(update);
        }
    }

    /** SSE data 行事件：JSON 信封（前端只解析 data: 行） */
    private SseEmitter.SseEventBuilder sse(Object payload) throws Exception {
        return SseEmitter.event().data(objectMapper.writeValueAsString(payload));
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

    /**
     * 每日提问上限：VIP 100 次、普通用户按配置（默认 20 次）。
     * 会员状态经 Feign 查询 poti-user（internal 通道），调用异常时降级按普通用户处理。
     */
    private int resolveDailyLimit(Long userId) {
        try {
            R<Map<String, Object>> resp = userFeignClient.getVipStatus(userId);
            if (resp != null && resp.getCode() == 200 && resp.getData() != null
                    && Boolean.TRUE.equals(resp.getData().get("vip"))) {
                return VIP_DAILY_LIMIT;
            }
        } catch (Exception e) {
            log.warn("[agent] 查询会员状态失败，按普通用户限额处理: userId={}", userId, e);
        }
        return dailyQuestionLimit;
    }

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
