package com.poti.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.poti.admin.entity.Question;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface QuestionService extends IService<Question> {

    Page<Question> pageList(int page, int size, Long categoryId, Integer difficulty, Integer status, String keyword);

    boolean addQuestion(Question question);

    boolean updateQuestion(Question question);

    boolean deleteQuestion(Long id);

    boolean updateStatus(Long id, Integer status);

    Map<String, Object> importQuestionsFromExcel(MultipartFile file) throws Exception;
}
