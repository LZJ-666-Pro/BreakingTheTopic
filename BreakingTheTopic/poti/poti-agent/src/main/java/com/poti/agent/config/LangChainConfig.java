package com.poti.agent.config;

import com.poti.agent.service.Assistant;
import com.poti.agent.service.tools.AgentTools;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * LangChain4j 装配（客户指定 LangChain 技术路线）：
 * OpenAiChatModel 指向智谱 OpenAI 兼容端点（glm-4-flash）
 * + AiServices 工具循环 + JDBC 记忆。
 */
@Configuration
public class LangChainConfig {

    @Value("${agent.llm.api-key}")
    private String apiKey;

    @Value("${agent.llm.base-url:https://open.bigmodel.cn/api/paas/v4}")
    private String baseUrl;

    @Value("${agent.llm.model:glm-4-flash}")
    private String model;

    @Value("${agent.llm.temperature:0.4}")
    private Double temperature;

    @Value("${agent.llm.max-tokens:2000}")
    private Integer maxTokens;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(model)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .timeout(Duration.ofSeconds(60))
                .logRequests(false)
                .logResponses(false)
                .build();
    }

    /** 真流式模型：与同步模型同端点同参数，TokenStream 版 AiServices 必需 */
    @Bean
    public StreamingChatLanguageModel streamingChatLanguageModel() {
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(model)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .timeout(Duration.ofSeconds(60))
                .logRequests(false)
                .logResponses(false)
                .build();
    }

    @Bean
    public Assistant assistant(ChatLanguageModel chatLanguageModel,
                               StreamingChatLanguageModel streamingChatLanguageModel,
                               ChatMemoryStore chatMemoryStore,
                               AgentTools agentTools) {
        return AiServices.builder(Assistant.class)
                .chatLanguageModel(chatLanguageModel)
                .streamingChatLanguageModel(streamingChatLanguageModel)
                .chatMemoryProvider(sessionId -> dev.langchain4j.memory.chat.MessageWindowChatMemory.builder()
                        .id(sessionId)
                        .maxMessages(24)
                        .chatMemoryStore(chatMemoryStore)
                        .build())
                .tools(agentTools)
                .build();
    }
}
