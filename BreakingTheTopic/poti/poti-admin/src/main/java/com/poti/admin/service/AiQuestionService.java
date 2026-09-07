package com.poti.admin.service;

import com.poti.admin.dto.AiGenerateRequest;
import com.poti.admin.dto.AiQuestionDTO;

import java.util.List;

public interface AiQuestionService {
    
    List<AiQuestionDTO> generateQuestions(AiGenerateRequest request);
    
    boolean saveGeneratedQuestions(Long categoryId, List<AiQuestionDTO> questions);
}
