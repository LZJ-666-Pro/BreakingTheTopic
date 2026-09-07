package com.poti.admin.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poti.admin.dto.QuestionExcelDTO;
import com.poti.admin.entity.Question;
import com.poti.admin.mapper.QuestionMapper;
import com.poti.admin.service.QuestionService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@DS("question")
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Page<Question> pageList(int page, int size, Long categoryId, Integer difficulty, Integer status, String keyword) {
        Page<Question> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        
        if (categoryId != null) {
            wrapper.eq(Question::getCategoryId, categoryId);
        }
        if (difficulty != null) {
            wrapper.eq(Question::getDifficulty, difficulty);
        }
        if (status != null) {
            wrapper.eq(Question::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Question::getTitle, keyword)
                    .or()
                    .like(Question::getContent, keyword));
        }
        wrapper.orderByDesc(Question::getCreateTime);
        return this.page(pageParam, wrapper);
    }

    @Override
    public boolean addQuestion(Question question) {
        question.setViewCount(0);
        question.setFavoriteCount(0);
        return this.save(question);
    }

    @Override
    public boolean updateQuestion(Question question) {
        return this.updateById(question);
    }

    @Override
    public boolean deleteQuestion(Long id) {
        return this.removeById(id);
    }

    @Override
    public boolean updateStatus(Long id, Integer status) {
        Question question = new Question();
        question.setId(id);
        question.setStatus(status);
        return this.updateById(question);
    }

    @Override
    public Map<String, Object> importQuestionsFromExcel(MultipartFile file) throws Exception {
        long startTime = System.currentTimeMillis();
        List<QuestionExcelDTO> excelList = EasyExcel.read(file.getInputStream())
                .head(QuestionExcelDTO.class)
                .sheet()
                .doReadSync();

        List<Question> questionList = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;
        List<String> errorMessages = new ArrayList<>();
        int batchSize = 500;

        for (int i = 0; i < excelList.size(); i++) {
            QuestionExcelDTO dto = excelList.get(i);
            int rowNum = i + 2;

            try {
                if (dto.getCategoryId() == null) {
                    errorMessages.add("第" + rowNum + "行: 分类ID不能为空");
                    failCount++;
                    continue;
                }
                if (!StringUtils.hasText(dto.getContent())) {
                    errorMessages.add("第" + rowNum + "行: 题目内容不能为空");
                    failCount++;
                    continue;
                }
                if (!StringUtils.hasText(dto.getAnswer())) {
                    errorMessages.add("第" + rowNum + "行: 正确答案不能为空");
                    failCount++;
                    continue;
                }

                LambdaQueryWrapper<Question> existWrapper = new LambdaQueryWrapper<>();
                existWrapper.eq(Question::getCategoryId, dto.getCategoryId())
                           .eq(Question::getContent, dto.getContent());
                long existCount = this.count(existWrapper);
                if (existCount > 0) {
                    errorMessages.add("第" + rowNum + "行: 题目已存在，跳过导入");
                    failCount++;
                    continue;
                }

                Question question = new Question();
                question.setCategoryId(dto.getCategoryId());
                question.setTitle(dto.getContent());
                question.setContent(dto.getContent());
                question.setType(dto.getType() != null ? dto.getType() : 1);
                question.setDifficulty(dto.getDifficulty() != null ? dto.getDifficulty() : 2);

                List<String> options = new ArrayList<>();
                if (StringUtils.hasText(dto.getOptionA())) options.add("A. " + dto.getOptionA());
                if (StringUtils.hasText(dto.getOptionB())) options.add("B. " + dto.getOptionB());
                if (StringUtils.hasText(dto.getOptionC())) options.add("C. " + dto.getOptionC());
                if (StringUtils.hasText(dto.getOptionD())) options.add("D. " + dto.getOptionD());
                if (StringUtils.hasText(dto.getOptionE())) options.add("E. " + dto.getOptionE());
                if (StringUtils.hasText(dto.getOptionF())) options.add("F. " + dto.getOptionF());
                
                try {
                    question.setOptions(options.isEmpty() ? "[]" : objectMapper.writeValueAsString(options));
                } catch (Exception e) {
                    question.setOptions("[]");
                }

                String answer = dto.getAnswer().toUpperCase();
                if (!answer.matches("[A-F]")) {
                    errorMessages.add("第" + rowNum + "行: 正确答案必须是A-F之间的字母");
                    failCount++;
                    continue;
                }
                question.setAnswer(answer);
                question.setAnalysis(dto.getAnalysis());
                question.setTags(dto.getTags());
                question.setViewCount(0);
                question.setFavoriteCount(0);
                question.setStatus(1);

                questionList.add(question);
                successCount++;

                if (questionList.size() >= batchSize) {
                    this.saveBatch(questionList);
                    questionList.clear();
                }
            } catch (Exception e) {
                errorMessages.add("第" + rowNum + "行: " + e.getMessage());
                failCount++;
            }
        }

        if (!questionList.isEmpty()) {
            this.saveBatch(questionList);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        Map<String, Object> result = new HashMap<>();
        result.put("total", excelList.size());
        result.put("success", successCount);
        result.put("fail", failCount);
        result.put("errors", errorMessages.size() > 20 ? errorMessages.subList(0, 20) : errorMessages);
        result.put("duration", duration + "ms");
        result.put("message", String.format("导入完成！共%d条，成功%d条，失败%d条，耗时%dms", 
            excelList.size(), successCount, failCount, duration));

        return result;
    }
}
