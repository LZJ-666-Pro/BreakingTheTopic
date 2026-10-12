package com.poti.user.controller;

import com.poti.common.utils.Result;
import com.poti.user.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    // 用户身份统一取自鉴权过滤器注入的 X-User-Id（由 JWT 解析而来，无法伪造），
    // 不再信任前端传参，防止越权查看他人会话或冒充他人发消息

    @GetMapping("/conversations")
    public Result<List<Map<String, Object>>> getConversationList(@RequestHeader("X-User-Id") Long userId) {
        return chatService.getConversationList(userId);
    }

    @GetMapping("/messages")
    public Result<List<Map<String, Object>>> getMessages(@RequestHeader("X-User-Id") Long userId,
            @RequestParam Long conversationId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Long afterId) {
        return chatService.getMessages(userId, conversationId, page, size, afterId);
    }

    @PostMapping("/send")
    public Result<Void> sendMessage(@RequestHeader("X-User-Id") Long senderId,
            @RequestParam Long receiverId,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String mediaUrl,
            @RequestParam(required = false) Integer duration) {
        return chatService.sendMessage(senderId, receiverId, content, type, mediaUrl, duration);
    }

    @PostMapping("/read")
    public Result<Void> markAsRead(@RequestHeader("X-User-Id") Long userId,
            @RequestParam Long conversationId) {
        return chatService.markAsRead(userId, conversationId);
    }

    @PostMapping("/upload")
    public Result<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam Integer type) {
        return chatService.uploadFile(file, type);
    }

    @DeleteMapping("/message/{messageId}")
    public Result<Void> deleteMessage(
            @PathVariable Long messageId,
            @RequestHeader("X-User-Id") Long userId) {
        return chatService.deleteMessage(messageId, userId);
    }

    @DeleteMapping("/conversation/{conversationId}")
    public Result<Void> deleteConversation(
            @PathVariable Long conversationId,
            @RequestHeader("X-User-Id") Long userId) {
        return chatService.deleteConversation(conversationId, userId);
    }
}
