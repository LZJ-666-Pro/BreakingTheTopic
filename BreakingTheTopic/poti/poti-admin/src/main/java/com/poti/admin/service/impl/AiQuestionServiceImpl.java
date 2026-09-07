package com.poti.admin.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poti.admin.config.AiConfig;
import com.poti.admin.dto.AiGenerateRequest;
import com.poti.admin.dto.AiQuestionDTO;
import com.poti.admin.entity.Question;
import com.poti.admin.mapper.CategoryMapper;
import com.poti.admin.mapper.QuestionMapper;
import com.poti.admin.service.AiQuestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class AiQuestionServiceImpl implements AiQuestionService {

    @Autowired
    private AiConfig aiConfig;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public List<AiQuestionDTO> generateQuestions(AiGenerateRequest request) {
        try {
            String categoryName = request.getCategoryName();
            if (categoryName == null && request.getCategoryId() != null) {
                var category = categoryMapper.selectById(request.getCategoryId());
                if (category != null) {
                    categoryName = category.getName();
                }
            }

            int totalCount = request.getCount();
            int batchSize = 5;
            List<AiQuestionDTO> allQuestions = new ArrayList<>();
            
            int batches = (int) Math.ceil((double) totalCount / batchSize);
            log.info("开始分批生成题目，总数: {}, 批次数: {}", totalCount, batches);
            
            for (int i = 0; i < batches; i++) {
                int currentBatchSize = Math.min(batchSize, totalCount - allQuestions.size());
                AiGenerateRequest batchRequest = new AiGenerateRequest();
                batchRequest.setCategoryId(request.getCategoryId());
                batchRequest.setCategoryName(categoryName);
                batchRequest.setCount(currentBatchSize);
                batchRequest.setDifficulty(request.getDifficulty());
                batchRequest.setTopic(request.getTopic());
                batchRequest.setAdditionalRequirements(request.getAdditionalRequirements());
                
                String prompt = buildPrompt(categoryName, batchRequest);
                log.info("第 {}/{} 批生成题目", i + 1, batches);
                
                String response = callAiApi(prompt);
                List<AiQuestionDTO> batchQuestions = parseQuestions(response);
                
                allQuestions.addAll(batchQuestions);
                log.info("第 {} 批完成，累计生成 {} 道题目", i + 1, allQuestions.size());
                
                if (i < batches - 1) {
                    Thread.sleep(1000);
                }
            }
            
            log.info("AI生成完成，共 {} 道题目", allQuestions.size());
            return allQuestions;
        } catch (Exception e) {
            log.error("AI生成题目失败", e);
            throw new RuntimeException("AI生成题目失败: " + e.getMessage());
        }
    }

    @Override
    public boolean saveGeneratedQuestions(Long categoryId, List<AiQuestionDTO> questions) {
        try {
            for (AiQuestionDTO dto : questions) {
                Question question = new Question();
                question.setCategoryId(categoryId);
                question.setTitle(dto.getTitle());
                question.setContent(dto.getContent());
                question.setType(dto.getType());
                question.setDifficulty(dto.getDifficulty());
                question.setOptions(objectMapper.writeValueAsString(dto.getOptions()));
                question.setAnswer(dto.getAnswer());
                question.setAnalysis(dto.getAnalysis());
                question.setTags(dto.getTags());
                question.setStatus(1);
                questionMapper.insert(question);
            }
            return true;
        } catch (Exception e) {
            log.error("保存AI生成的题目失败", e);
            return false;
        }
    }

    private String buildPrompt(String categoryName, AiGenerateRequest request) {
        String difficultyText = switch (request.getDifficulty()) {
            case 1 -> "简单";
            case 2 -> "中等";
            case 3 -> "困难";
            default -> "中等";
        };

        StringBuilder prompt = new StringBuilder();
        prompt.append("请生成").append(request.getCount()).append("道").append(categoryName).append("相关的选择题。\n");
        prompt.append("难度：").append(difficultyText).append("\n");
        
        if (request.getTopic() != null && !request.getTopic().isEmpty()) {
            prompt.append("主题：").append(request.getTopic()).append("\n");
        }
        
        if (request.getAdditionalRequirements() != null && !request.getAdditionalRequirements().isEmpty()) {
            prompt.append("额外要求：").append(request.getAdditionalRequirements()).append("\n");
        }

        prompt.append("\n请严格按照以下JSON格式返回，不要包含其他内容：\n");
        prompt.append("[\n");
        prompt.append("  {\n");
        prompt.append("    \"title\": \"题目标题\",\n");
        prompt.append("    \"content\": \"题目内容描述\",\n");
        prompt.append("    \"type\": 1,\n");
        prompt.append("    \"difficulty\": ").append(request.getDifficulty()).append(",\n");
        prompt.append("    \"options\": [\"A. 选项A内容\", \"B. 选项B内容\", \"C. 选项C内容\", \"D. 选项D内容\"],\n");
        prompt.append("    \"answer\": \"正确答案字母，如A\",\n");
        prompt.append("    \"analysis\": \"答案解析\",\n");
        prompt.append("    \"tags\": \"标签1,标签2\"\n");
        prompt.append("  }\n");
        prompt.append("]\n");
        prompt.append("\n注意：\n");
        prompt.append("1. 每道题必须有4个选项\n");
        prompt.append("2. 答案只能是A、B、C、D中的一个\n");
        prompt.append("3. 题目内容要准确、专业\n");
        prompt.append("4. 解析要详细说明为什么选这个答案\n");
        prompt.append("5. 只返回JSON数组，不要有其他文字说明");

        return prompt.toString();
    }

    private String callAiApi(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(aiConfig.getApiKey());

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", aiConfig.getModel());
            requestBody.put("messages", List.of(
                    Map.of("role", "system", "content", "你是一个专业的编程面试题出题专家，擅长生成高质量的技术面试题。"),
                    Map.of("role", "user", "content", prompt)
            ));
            requestBody.put("max_tokens", aiConfig.getMaxTokens());
            requestBody.put("temperature", aiConfig.getTemperature());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String url = aiConfig.getBaseUrl() + "/chat/completions";
            log.info("调用AI API: {}", url);
            log.info("请求模型: {}", aiConfig.getModel());
            log.info("API Key前缀: {}", aiConfig.getApiKey().substring(0, Math.min(10, aiConfig.getApiKey().length())) + "...");

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            log.info("AI API响应状态: {}", response.getStatusCode());
            log.info("AI API响应体: {}", response.getBody());

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode choices = root.path("choices");
                if (choices.isArray() && choices.size() > 0) {
                    return choices.get(0).path("message").path("content").asText();
                }
            }

            throw new RuntimeException("AI API响应格式错误: " + response.getBody());
        } catch (RestClientException e) {
            log.error("调用AI API网络错误", e);
            throw new RuntimeException("网络连接失败，请检查网络或API地址: " + e.getMessage());
        } catch (Exception e) {
            log.error("调用AI API失败", e);
            throw new RuntimeException("调用AI API失败: " + e.getMessage());
        }
    }

    private List<AiQuestionDTO> parseQuestions(String response) {
        try {
            String jsonContent = response.trim();
            
            if (jsonContent.startsWith("```json")) {
                jsonContent = jsonContent.substring(7);
            }
            if (jsonContent.startsWith("```")) {
                jsonContent = jsonContent.substring(3);
            }
            if (jsonContent.endsWith("```")) {
                jsonContent = jsonContent.substring(0, jsonContent.length() - 3);
            }
            jsonContent = jsonContent.trim();

            if (!jsonContent.startsWith("[")) {
                int start = jsonContent.indexOf("[");
                int end = jsonContent.lastIndexOf("]");
                if (start != -1 && end != -1) {
                    jsonContent = jsonContent.substring(start, end + 1);
                }
            }

            log.info("解析JSON长度: {}", jsonContent.length());

            List<AiQuestionDTO> questions;
            try {
                questions = objectMapper.readValue(jsonContent, new TypeReference<List<AiQuestionDTO>>() {});
            } catch (Exception e) {
                log.warn("JSON解析失败，尝试修复截断的JSON: {}", e.getMessage());
                questions = tryParsePartialJson(jsonContent);
            }
            
            if (questions == null || questions.isEmpty()) {
                throw new RuntimeException("未能解析出有效题目");
            }
            
            for (AiQuestionDTO q : questions) {
                if (q.getType() == null) q.setType(1);
                if (q.getDifficulty() == null) q.setDifficulty(1);
                if (q.getOptions() != null) {
                    List<String> cleanedOptions = new ArrayList<>();
                    for (String opt : q.getOptions()) {
                        if (!opt.matches("^[A-D]\\..*")) {
                            cleanedOptions.add((char)('A' + cleanedOptions.size()) + ". " + opt);
                        } else {
                            cleanedOptions.add(opt);
                        }
                    }
                    q.setOptions(cleanedOptions);
                }
                if (q.getAnswer() != null) {
                    q.setAnswer(q.getAnswer().toUpperCase());
                }
            }

            log.info("成功解析 {} 道题目", questions.size());
            return questions;
        } catch (Exception e) {
            log.error("解析AI响应失败: {}", response, e);
            throw new RuntimeException("解析AI响应失败: " + e.getMessage());
        }
    }
    
    private List<AiQuestionDTO> tryParsePartialJson(String jsonContent) {
        List<AiQuestionDTO> questions = new ArrayList<>();
        
        try {
            int lastValidEnd = jsonContent.lastIndexOf("}");
            if (lastValidEnd == -1) {
                return questions;
            }
            
            String partialJson = jsonContent.substring(0, lastValidEnd + 1) + "]";
            
            int firstBracket = partialJson.indexOf("[");
            if (firstBracket != -1) {
                partialJson = partialJson.substring(firstBracket);
            }
            
            log.info("尝试解析部分JSON: {}", partialJson.substring(0, Math.min(200, partialJson.length())) + "...");
            
            questions = objectMapper.readValue(partialJson, new TypeReference<List<AiQuestionDTO>>() {});
        } catch (Exception e) {
            log.error("部分JSON解析也失败: {}", e.getMessage());
        }
        
        return questions;
    }
}
