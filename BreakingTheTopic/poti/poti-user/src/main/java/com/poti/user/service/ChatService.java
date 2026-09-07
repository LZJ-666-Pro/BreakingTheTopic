package com.poti.user.service;

import com.poti.common.utils.Result;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ChatService {
    
    Result<List<Map<String, Object>>> getConversationList(Long userId);
    
    Result<List<Map<String, Object>>> getMessages(Long userId, Long conversationId, Integer page, Integer size);
    
    Result<Void> sendMessage(Long senderId, Long receiverId, String content, Integer type, String mediaUrl, Integer duration);
    
    Result<Void> markAsRead(Long userId, Long conversationId);
    
    Result<String> uploadFile(MultipartFile file, Integer type);
    
    Result<Void> deleteMessage(Long messageId, Long userId);
    
    Result<Void> deleteConversation(Long conversationId, Long userId);
}
