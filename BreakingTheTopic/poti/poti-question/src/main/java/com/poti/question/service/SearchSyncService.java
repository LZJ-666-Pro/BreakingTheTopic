package com.poti.question.service;

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
            List<Question> questions = questionMapper.selectList(null);
            syncQuestions(questions);
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
