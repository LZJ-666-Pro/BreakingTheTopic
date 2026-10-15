package com.poti.agent.memory;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * LangChain4j ChatMemoryStore 的 JDBC 实现：LLM 内部对话（含工具交互消息）
 * 序列化后落 agent_chat_memory 表，服务重启不丢上下文。
 * 用户侧历史展示读 agent_message，与此表职责分离。
 */
@Component
public class JdbcChatMemoryStore implements ChatMemoryStore {

    private final JdbcTemplate jdbcTemplate;

    public JdbcChatMemoryStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String memoryIdStr = String.valueOf(memoryId);
        // 多数调用按主键查即可；LambdaQueryWrapper 在此不可用（无实体映射），直接 JDBC
        List<String> json = jdbcTemplate.queryForList(
                "SELECT messages FROM agent_chat_memory WHERE memory_id = ?", String.class, memoryIdStr);
        if (json.isEmpty() || json.get(0) == null || json.get(0).isBlank()) {
            return List.of();
        }
        return ChatMessageDeserializer.messagesFromJson(json.get(0));
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String memoryIdStr = String.valueOf(memoryId);
        String payload = ChatMessageSerializer.messagesToJson(messages);
        jdbcTemplate.update(
                "INSERT INTO agent_chat_memory (memory_id, messages) VALUES (?, ?) " +
                        "ON DUPLICATE KEY UPDATE messages = VALUES(messages)",
                memoryIdStr, payload);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        jdbcTemplate.update("DELETE FROM agent_chat_memory WHERE memory_id = ?", String.valueOf(memoryId));
    }
}
