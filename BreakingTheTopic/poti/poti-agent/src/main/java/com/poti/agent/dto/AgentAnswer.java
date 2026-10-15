package com.poti.agent.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * 助手最终回答的结构化协议（02 规范 3.3）。
 * LLM 被要求以 JSON 输出；解析失败时降级为纯文本回答（无卡片）。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AgentAnswer {

    private String text;

    /** 引用标注：结论来自哪些业务数据 */
    private List<String> sources;

    /** 动作卡片 */
    private List<AgentAction> actions;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AgentAction {
        /** PRACTICE_CATEGORY / VIEW_QUESTION / VIEW_WRONGBOOK / VIEW_STATISTICS */
        private String type;
        private Long categoryId;
        private Long questionId;
        private String label;
    }
}
