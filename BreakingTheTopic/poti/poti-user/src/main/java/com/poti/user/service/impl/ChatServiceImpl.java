package com.poti.user.service.impl;

import com.poti.common.utils.Result;
import com.poti.user.entity.Conversation;
import com.poti.user.entity.Message;
import com.poti.user.mapper.ConversationMapper;
import com.poti.user.mapper.MessageMapper;
import com.poti.user.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {
    
    @Autowired
    private ConversationMapper conversationMapper;
    
    @Autowired
    private MessageMapper messageMapper;
    
    @Value("${chat.upload.path:./uploads/chat/}")
    private String chatUploadPath;
    
    @Value("${chat.url.prefix:http://localhost:8200/chat/}")
    private String chatUrlPrefix;
    
    @Override
    public Result<List<Map<String, Object>>> getConversationList(Long userId) {
        try {
            List<Map<String, Object>> conversations = conversationMapper.getConversationList(userId);
            return Result.success(conversations);
        } catch (Exception e) {
            log.error("获取会话列表失败", e);
            return Result.error("获取会话列表失败：" + e.getMessage());
        }
    }
    
    @Override
    public Result<List<Map<String, Object>>> getMessages(Long userId, Long conversationId, Integer page, Integer size) {
        try {
            Conversation conversation = conversationMapper.selectById(conversationId);
            if (conversation == null) {
                return Result.error("会话不存在");
            }
            
            if (!conversation.getUser1Id().equals(userId) && !conversation.getUser2Id().equals(userId)) {
                return Result.error("无权访问此会话");
            }
            
            int offset = (page - 1) * size;
            List<Map<String, Object>> messages = messageMapper.getMessages(conversationId, offset, size);
            
            conversationMapper.clearUnread(conversationId, userId);
            messageMapper.markAsRead(conversationId, userId);
            
            return Result.success(messages);
        } catch (Exception e) {
            log.error("获取消息列表失败", e);
            return Result.error("获取消息列表失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public Result<Void> sendMessage(Long senderId, Long receiverId, String content, Integer type, String mediaUrl, Integer duration) {
        try {
            Conversation conversation = conversationMapper.findByUsers(senderId, receiverId);
            
            if (conversation == null) {
                conversation = new Conversation();
                if (senderId < receiverId) {
                    conversation.setUser1Id(senderId);
                    conversation.setUser2Id(receiverId);
                } else {
                    conversation.setUser1Id(receiverId);
                    conversation.setUser2Id(senderId);
                }
                conversation.setUser1Unread(0);
                conversation.setUser2Unread(0);
                conversationMapper.insert(conversation);
                log.info("创建新会话: id={}", conversation.getId());
            }
            
            Message message = new Message();
            message.setConversationId(conversation.getId());
            message.setSenderId(senderId);
            message.setReceiverId(receiverId);
            message.setContent(content != null ? content : "");
            message.setMediaUrl(mediaUrl);
            message.setDuration(duration);
            message.setType(type != null ? type : 1);
            message.setStatus(1);
            messageMapper.insert(message);
            log.info("发送消息: id={}", message.getId());
            
            String lastMessage = "";
            if (type != null && type == 2) {
                lastMessage = "[图片]";
            } else if (type != null && type == 3) {
                lastMessage = "[语音]";
            } else {
                lastMessage = content != null ? (content.length() > 50 ? content.substring(0, 50) + "..." : content) : "";
            }
            conversationMapper.updateLastMessage(conversation.getId(), lastMessage, senderId);
            
            return Result.success(null);
        } catch (Exception e) {
            log.error("发送消息失败", e);
            return Result.error("发送消息失败：" + e.getMessage());
        }
    }
    
    @Override
    public Result<Void> markAsRead(Long userId, Long conversationId) {
        try {
            conversationMapper.clearUnread(conversationId, userId);
            messageMapper.markAsRead(conversationId, userId);
            return Result.success(null);
        } catch (Exception e) {
            log.error("标记已读失败", e);
            return Result.error("标记已读失败：" + e.getMessage());
        }
    }
    
    @Override
    public Result<String> uploadFile(MultipartFile file, Integer type) {
        try {
            if (file.isEmpty()) {
                return Result.error("请选择要上传的文件");
            }
            
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            String fileName = UUID.randomUUID().toString() + extension;
            
            String subDir = type == 2 ? "images/" : "voices/";
            String absoluteUploadPath = new File(chatUploadPath + subDir).getAbsolutePath();
            File uploadDir = new File(absoluteUploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            
            File destFile = new File(absoluteUploadPath + File.separator + fileName);
            file.transferTo(destFile);
            
            String fileUrl = chatUrlPrefix + subDir + fileName;
            log.info("文件上传成功: {}", fileUrl);
            
            return Result.success(fileUrl);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }
    
    @Override
    public Result<Void> deleteMessage(Long messageId, Long userId) {
        try {
            Message message = messageMapper.selectById(messageId);
            if (message == null) {
                return Result.error("消息不存在");
            }
            
            if (!message.getSenderId().equals(userId)) {
                return Result.error("无权删除此消息");
            }
            
            messageMapper.deleteById(messageId);
            log.info("删除消息成功: messageId={}", messageId);
            
            return Result.success(null);
        } catch (Exception e) {
            log.error("删除消息失败", e);
            return Result.error("删除消息失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public Result<Void> deleteConversation(Long conversationId, Long userId) {
        try {
            Conversation conversation = conversationMapper.selectById(conversationId);
            if (conversation == null) {
                return Result.error("会话不存在");
            }
            
            if (!conversation.getUser1Id().equals(userId) && !conversation.getUser2Id().equals(userId)) {
                return Result.error("无权删除此会话");
            }
            
            messageMapper.deleteByConversationId(conversationId);
            log.info("删除会话的所有消息: conversationId={}", conversationId);
            
            conversationMapper.deleteById(conversationId);
            log.info("删除会话成功: conversationId={}", conversationId);
            
            return Result.success(null);
        } catch (Exception e) {
            log.error("删除会话失败", e);
            return Result.error("删除会话失败：" + e.getMessage());
        }
    }
}
