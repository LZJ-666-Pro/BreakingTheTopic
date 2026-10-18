package com.poti.practice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 竞赛解答题 AI 判分服务
 * 复用项目统一的智谱 OpenAI 兼容端点（glm-4-flash），对比用户答案与标准答案/解析给出 0-100 分
 */
@Slf4j
@Service
public class ZhipuJudgeService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${ai.api-key:}")
    private String apiKey;

    @Value("${ai.base-url:https://open.bigmodel.cn/api/paas/v4}")
    private String baseUrl;

    @Value("${ai.model:glm-4-flash}")
    private String model;

    /**
     * AI 判分
     *
     * @param title     题干
     * @param answer    标准答案（要点）
     * @param analysis  解析
     * @param userAnswer 用户作答（文本/代码）
     * @return 0-100 分；调用失败返回 -1（由调用方走降级逻辑）
     */
    public int judge(String title, String answer, String analysis, String userAnswer) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.warn("未配置 ai.api-key，解答题走降级判分");
            return -1;
        }
        try {
            String prompt = buildPrompt(title, answer, analysis, userAnswer);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey.trim());

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", List.of(
                    Map.of("role", "system", "content",
                            "你是严格的编程竞赛阅卷老师。根据题目、标准答案和解析评判用户作答，"
                                    + "只返回JSON对象：{\"score\":0到100的整数}，不要返回其他任何文字。"),
                    Map.of("role", "user", "content", prompt)
            ));
            requestBody.put("max_tokens", 128);
            requestBody.put("temperature", 0.1);

            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "/chat/completions", HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers), String.class);

            if (response.getStatusCode().value() == 200 && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                String content = root.path("choices").path(0).path("message").path("content").asText("");
                return parseScore(content);
            }
        } catch (Exception e) {
            log.error("解答题 AI 判分失败，走降级判分: {}", e.getMessage());
        }
        return -1;
    }

    private String buildPrompt(String title, String answer, String analysis, String userAnswer) {
        StringBuilder p = new StringBuilder();
        p.append("【题目】").append(title == null ? "" : title).append("\n");
        if (analysis != null && !analysis.trim().isEmpty()) {
            p.append("【解析】").append(analysis).append("\n");
        }
        p.append("【标准答案要点】").append(answer == null ? "" : answer).append("\n");
        p.append("【用户作答】\n").append(userAnswer == null ? "" : userAnswer).append("\n\n");
        p.append("评分标准：核心要点/代码逻辑正确即给高分；答非所问、完全错误给低分。"
                + "只返回JSON：{\"score\":整数}");
        return p.toString();
    }

    /** 从 AI 返回内容中解析 score（容忍 ```json 围栏与多余文字） */
    private int parseScore(String content) {
        if (content == null || content.isEmpty()) {
            return -1;
        }
        try {
            String json = content.trim();
            int start = json.indexOf("{");
            int end = json.lastIndexOf("}");
            if (start == -1 || end <= start) {
                return -1;
            }
            json = json.substring(start, end + 1);
            int score = objectMapper.readTree(json).path("score").asInt(-1);
            if (score < 0 || score > 100) {
                return -1;
            }
            return score;
        } catch (Exception e) {
            return -1;
        }
    }
}
