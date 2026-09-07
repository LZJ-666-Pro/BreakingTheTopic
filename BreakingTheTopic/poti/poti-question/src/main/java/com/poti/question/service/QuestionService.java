package com.poti.question.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.poti.question.entity.Question;

import java.util.List;
import java.util.Map;

public interface QuestionService extends IService<Question> {

    List<Question> getRandomQuestions(Long categoryId, int limit);
    
    List<Question> getRandomQuestionsGlobal(int limit);

    List<Map<String, Object>> getRandomQuestionsByType(String type, int limit);
    
    List<Map<String, Object>> getRandomQuestionsByCategory(Long categoryId, int limit);
    
    void incrementViewCount(Long questionId);
}
