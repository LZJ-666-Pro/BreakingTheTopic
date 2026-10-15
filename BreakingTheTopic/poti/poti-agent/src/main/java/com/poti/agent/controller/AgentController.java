package com.poti.agent.controller;

import com.poti.agent.entity.AgentSession;
import com.poti.agent.service.AgentChatService;
import com.poti.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * AI 学习助手接口。
 * 全部接口要求登录（JwtAuthenticationFilter 校验 JWT 并注入 X-User-Id），
 * 身份一律取请求头，不信任任何 body 参数。
 */
@Slf4j
@RestController
@RequestMapping("/agent")
public class AgentController {

    private final AgentChatService chatService;

    public AgentController(AgentChatService chatService) {
        this.chatService = chatService;
    }

    /** 新建会话 */
    @PostMapping("/session")
    public R<AgentSession> createSession(@RequestHeader("X-User-Id") Long userId) {
        try {
            return R.success(chatService.createSession(userId));
        } catch (Exception e) {
            log.error("创建会话失败", e);
            return R.error("创建会话失败");
        }
    }

    /** 提问（多轮：携带 sessionId 续问；不携带则新建） */
    @PostMapping("/chat")
    public R<Map<String, Object>> chat(@RequestHeader("X-User-Id") Long userId,
                                       @RequestBody ChatRequest request) {
        try {
            if (request == null || request.getMessage() == null || request.getMessage().isBlank()) {
                return R.error("提问内容不能为空");
            }
            return R.success(chatService.chat(userId, request.getSessionId(), request.getMessage().trim()));
        } catch (IllegalArgumentException e) {
            return R.error(e.getMessage());
        } catch (IllegalStateException e) {
            // 限额等业务规则拒绝：HTTP 200 + 业务失败，前端 toast 展示 message
            return R.error(e.getMessage());
        } catch (Exception e) {
            log.error("助手回答失败", e);
            return R.error("助手开小差了，请稍后再试");
        }
    }

    /** 用户全部会话 */
    @GetMapping("/sessions")
    public R<List<AgentSession>> listSessions(@RequestHeader("X-User-Id") Long userId) {
        try {
            return R.success(chatService.getSessions(userId));
        } catch (Exception e) {
            log.error("查询会话列表失败", e);
            return R.error("查询会话列表失败");
        }
    }

    /** 会话消息历史 */
    @GetMapping("/messages")
    public R<?> listMessages(@RequestHeader("X-User-Id") Long userId,
                             @RequestParam("sessionId") Long sessionId) {
        try {
            return R.success(chatService.getMessages(userId, sessionId));
        } catch (IllegalArgumentException e) {
            return R.error(e.getMessage());
        } catch (Exception e) {
            log.error("查询消息历史失败", e);
            return R.error("查询消息历史失败");
        }
    }

    public static class ChatRequest {
        private Long sessionId;
        private String message;

        public Long getSessionId() {
            return sessionId;
        }

        public void setSessionId(Long sessionId) {
            this.sessionId = sessionId;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
