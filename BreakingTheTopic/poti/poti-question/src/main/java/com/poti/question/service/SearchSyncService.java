package com.poti.question.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poti.question.client.SearchClient;
import com.poti.question.dto.QuestionDocumentDTO;
import com.poti.question.entity.Category;
import com.poti.question.entity.Question;
import com.poti.question.mapper.CategoryMapper;
import com.poti.question.mapper.QuestionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SearchSyncService {

    @Autowired(required = false)
    private SearchClient searchClient;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${search.sync.enabled:false}")
    private boolean syncEnabled;

    @Async
    public void syncQuestion(Question question) {
        if (!syncEnabled || searchClient == null) {
            return;
        }
        try {
            QuestionDocumentDTO dto = convertToDTO(question);
            searchClient.syncQuestion(dto);
            log.info("同步题目到ES: {}", question.getId());
        } catch (Exception e) {
            log.error("同步题目到ES失败: {}", question.getId(), e);
        }
    }

    @Async
    public void syncQuestions(List<Question> questions) {
        if (!syncEnabled || searchClient == null) {
            return;
        }
        try {
            List<QuestionDocumentDTO> dtos = new ArrayList<>();
            for (Question question : questions) {
                dtos.add(convertToDTO(question));
            }
            searchClient.syncQuestions(dtos);
            log.info("批量同步题目到ES: {} 条", questions.size());
        } catch (Exception e) {
            log.error("批量同步题目到ES失败", e);
        }
    }

    public void syncAllQuestions() {
        if (!syncEnabled || searchClient == null) {
            log.warn("ES同步未启用");
            return;
        }
        try {
            // 只同步未删除且已上架的题，避免脏数据进入 ES
            LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Question::getDeleted, 0).eq(Question::getStatus, 1);
            List<Question> questions = questionMapper.selectList(wrapper);
            // 分批同步，避免一次性提交过大 payload 导致 Feign 超时
            int batchSize = 500;
            for (int i = 0; i < questions.size(); i += batchSize) {
                List<Question> batch = questions.subList(i, Math.min(i + batchSize, questions.size()));
                List<QuestionDocumentDTO> dtos = new ArrayList<>();
                for (Question q : batch) {
                    dtos.add(convertToDTO(q));
                }
                searchClient.syncQuestions(dtos);
                log.info("批量同步题目到ES: {} 条（进度 {}/{})", batch.size(), Math.min(i + batchSize, questions.size()), questions.size());
            }
            log.info("全量同步题目到ES完成: {} 条", questions.size());
        } catch (Exception e) {
            log.error("全量同步题目到ES失败", e);
        }
    }

    private QuestionDocumentDTO convertToDTO(Question question) {
        QuestionDocumentDTO dto = new QuestionDocumentDTO();
        dto.setId(question.getId());
        dto.setCategoryId(question.getCategoryId());
        dto.setType(question.getType() != null ? question.getType().toString() : null);
        dto.setTitle(question.getTitle());
        dto.setContent(question.getContent());
        dto.setAnswer(question.getAnswer());
        dto.setAnalysis(question.getAnalysis());
        dto.setTags(question.getTags());
        dto.setDifficulty(question.getDifficulty());

        if (question.getCategoryId() != null) {
            Category category = categoryMapper.selectById(question.getCategoryId());
            if (category != null) {
                dto.setCategoryName(category.getName());
            }
        }

        if (question.getOptions() != null && !question.getOptions().isEmpty()) {
            try {
                List<String> options = objectMapper.readValue(question.getOptions(), new TypeReference<List<String>>() {});
                if (options.size() > 0) dto.setOptionA(options.get(0));
                if (options.size() > 1) dto.setOptionB(options.get(1));
                if (options.size() > 2) dto.setOptionC(options.get(2));
                if (options.size() > 3) dto.setOptionD(options.get(3));
            } catch (Exception e) {
                log.error("解析选项失败", e);
            }
        }

        return dto;
    }
}
