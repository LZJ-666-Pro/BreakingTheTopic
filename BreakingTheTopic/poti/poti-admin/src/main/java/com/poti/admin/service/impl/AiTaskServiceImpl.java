package com.poti.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.poti.admin.dto.AiGenerateRequest;
import com.poti.admin.dto.AiQuestionDTO;
import com.poti.admin.entity.AiTask;
import com.poti.admin.entity.Question;
import com.poti.admin.mapper.AiTaskMapper;
import com.poti.admin.mapper.CategoryMapper;
import com.poti.admin.mapper.QuestionMapper;
import com.poti.admin.service.AiTaskService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class AiTaskServiceImpl implements AiTaskService {

    @Autowired
    private AiTaskMapper aiTaskMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${ai.api-key}")
    private String apiKey;

    @Value("${ai.base-url}")
    private String baseUrl;

    @Value("${ai.model}")
    private String model;

    @Value("${ai.max-tokens}")
    private Integer maxTokens;

    @Value("${ai.temperature}")
    private Double temperature;

    @Override
    public AiTask createTask(AiGenerateRequest request) {
        String categoryName = request.getCategoryName();
        if (categoryName == null && request.getCategoryId() != null) {
            var category = categoryMapper.selectById(request.getCategoryId());
            if (category != null) {
                categoryName = category.getName();
            }
        }

        AiTask task = new AiTask();
        task.setCategoryId(request.getCategoryId());
        task.setCategoryName(categoryName);
        task.setTotalCount(request.getCount());
        task.setCompletedCount(0);
        task.setStatus(0);
        task.setDifficulty(request.getDifficulty() == 1 ? "简单" : request.getDifficulty() == 2 ? "中等" : "困难");
        task.setTopic(request.getTopic());
        task.setAdditionalRequirements(request.getAdditionalRequirements());
        
        aiTaskMapper.insert(task);
        log.info("创建AI生成任务: {}", task.getId());
        
        return task;
    }

    @Override
    public AiTask getTaskStatus(Long taskId) {
        return aiTaskMapper.selectById(taskId);
    }

    @Override
    public Page<AiTask> getTaskList(Integer page, Integer size) {
        Page<AiTask> pageParam = new Page<>(page, size);
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<AiTask> wrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        return aiTaskMapper.selectPage(pageParam, wrapper);
    }

    @Override
    @Async
    public void processTask(Long taskId) {
        AiTask task = aiTaskMapper.selectById(taskId);
        if (task == null) {
            log.error("任务不存在: {}", taskId);
            return;
        }

        try {
            updateTaskStatus(taskId, 1, null);
            log.info("开始处理AI任务: {}, 分类: {}, 数量: {}", taskId, task.getCategoryName(), task.getTotalCount());
            
            int batchSize = 5;
            int totalBatches = (int) Math.ceil((double) task.getTotalCount() / batchSize);
            int completedCount = 0;

            for (int i = 0; i < totalBatches; i++) {
                if (isTaskCancelled(taskId)) {
                    log.info("任务已取消: {}", taskId);
                    return;
                }

                int currentBatchSize = Math.min(batchSize, task.getTotalCount() - completedCount);
                
                AiGenerateRequest batchRequest = new AiGenerateRequest();
                batchRequest.setCategoryId(task.getCategoryId());
                batchRequest.setCategoryName(task.getCategoryName());
                batchRequest.setCount(currentBatchSize);
                batchRequest.setDifficulty(task.getDifficulty().equals("简单") ? 1 : task.getDifficulty().equals("中等") ? 2 : 3);
                batchRequest.setTopic(task.getTopic());
                batchRequest.setAdditionalRequirements(task.getAdditionalRequirements());

                log.info("任务 {} 第 {}/{} 批开始生成", taskId, i + 1, totalBatches);
                String prompt = buildPrompt(task.getCategoryName(), batchRequest);
                log.info("任务 {} Prompt: {}", taskId, prompt.substring(0, Math.min(200, prompt.length())));
                
                String response = callAiApi(prompt);
                log.info("任务 {} AI响应: {}", taskId, response.substring(0, Math.min(500, response.length())));
                
                List<AiQuestionDTO> questions = parseQuestions(response);
                log.info("任务 {} 解析出 {} 道题目", taskId, questions.size());

                if (questions.isEmpty()) {
                    log.warn("任务 {} 第 {} 批解析失败，跳过", taskId, i + 1);
                    continue;
                }

                int savedCount = saveQuestions(task.getCategoryId(), questions);
                completedCount += savedCount;
                
                updateTaskProgress(taskId, completedCount);
                log.info("任务 {} 进度: {}/{}", taskId, completedCount, task.getTotalCount());

                if (i < totalBatches - 1) {
                    Thread.sleep(2000);
                }
            }

            updateTaskStatus(taskId, 2, null);
            log.info("任务 {} 完成，共生成 {} 道题目", taskId, completedCount);

        } catch (Exception e) {
            log.error("任务 {} 执行失败", taskId, e);
            updateTaskStatus(taskId, 3, e.getMessage());
        }
    }

    @Override
    public void cancelTask(Long taskId) {
        updateTaskStatus(taskId, 4, "用户取消");
    }

    private boolean isTaskCancelled(Long taskId) {
        AiTask task = aiTaskMapper.selectById(taskId);
        return task != null && task.getStatus() == 4;
    }

    private void updateTaskStatus(Long taskId, Integer status, String errorMessage) {
        UpdateWrapper<AiTask> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", taskId)
               .set("status", status)
               .set("error_message", errorMessage);
        aiTaskMapper.update(null, wrapper);
    }

    private void updateTaskProgress(Long taskId, Integer completedCount) {
        UpdateWrapper<AiTask> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", taskId)
               .set("completed_count", completedCount);
        aiTaskMapper.update(null, wrapper);
    }

    private int saveQuestions(Long categoryId, List<AiQuestionDTO> questions) {
        int savedCount = 0;
        for (AiQuestionDTO dto : questions) {
            try {
                Question question = new Question();
                question.setCategoryId(categoryId);
                question.setTitle(dto.getTitle());
                question.setContent(dto.getContent());
                question.setType(dto.getType() != null ? dto.getType() : 1);
                question.setDifficulty(dto.getDifficulty() != null ? dto.getDifficulty() : 1);
                try {
                    question.setOptions(objectMapper.writeValueAsString(dto.getOptions()));
                } catch (Exception e) {
                    question.setOptions("[]");
                }
                question.setAnswer(dto.getAnswer());
                question.setAnalysis(dto.getAnalysis());
                question.setTags(dto.getTags());
                question.setStatus(1);
                question.setViewCount(0);
                question.setFavoriteCount(0);
                
                int result = questionMapper.insert(question);
                if (result > 0) {
                    savedCount++;
                    log.info("保存题目成功: id={}, title={}", question.getId(), question.getTitle());
                } else {
                    log.warn("保存题目失败: {}", dto.getTitle());
                }
            } catch (Exception e) {
                log.error("保存题目异常: {}", dto.getTitle(), e);
            }
        }
        log.info("本批共保存 {} 道题目", savedCount);
        return savedCount;
    }

    private String buildPrompt(String categoryName, AiGenerateRequest request) {
        String difficultyText = request.getDifficulty() == 1 ? "简单" : request.getDifficulty() == 2 ? "中等" : "困难";

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
            headers.setBearerAuth(apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", List.of(
                    Map.of("role", "system", "content", "你是一个专业的编程面试题出题专家，擅长生成高质量的技术面试题。"),
                    Map.of("role", "user", "content", prompt)
            ));
            requestBody.put("max_tokens", maxTokens);
            requestBody.put("temperature", temperature);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String url = baseUrl + "/chat/completions";
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode choices = root.path("choices");
                if (choices.isArray() && choices.size() > 0) {
                    return choices.get(0).path("message").path("content").asText();
                }
            }

            throw new RuntimeException("AI API响应格式错误");
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

            List<AiQuestionDTO> questions = objectMapper.readValue(jsonContent, new TypeReference<List<AiQuestionDTO>>() {});
            
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

            return questions;
        } catch (Exception e) {
            log.error("解析AI响应失败: {}", response, e);
            return new ArrayList<>();
        }
    }

    @Override
    public void deleteTask(Long taskId) {
        aiTaskMapper.deleteById(taskId);
        log.info("删除AI任务: {}", taskId);
    }
}
