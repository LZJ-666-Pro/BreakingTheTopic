package com.poti.question.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.poti.question.entity.Category;
import com.poti.question.entity.Question;
import com.poti.question.mapper.QuestionMapper;
import com.poti.question.service.CategoryService;
import com.poti.question.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    @Autowired
    private CategoryService categoryService;

    private static final Map<String, String> TYPE_NAME_MAP = new HashMap<>();
    static {
        TYPE_NAME_MAP.put("java", "Java");
        TYPE_NAME_MAP.put("python", "Python");
        TYPE_NAME_MAP.put("mysql", "MySQL");
        TYPE_NAME_MAP.put("redis", "Redis");
        TYPE_NAME_MAP.put("spring", "Spring");
        TYPE_NAME_MAP.put("mq", "MQ");
    }

    @Override
    public List<Question> getRandomQuestions(Long categoryId, int limit) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getCategoryId, categoryId)
               .eq(Question::getDeleted, 0)
               .eq(Question::getStatus, 1);
        
        List<Question> allQuestions = this.list(wrapper);
        
        Collections.shuffle(allQuestions);
        
        if (allQuestions.size() <= limit) {
            return allQuestions;
        }
        
        return allQuestions.subList(0, limit);
    }

    @Override
    public List<Question> getRandomQuestionsGlobal(int limit) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getDeleted, 0)
               .eq(Question::getStatus, 1);
        
        List<Question> allQuestions = this.list(wrapper);
        
        Collections.shuffle(allQuestions);
        
        if (allQuestions.size() <= limit) {
            return allQuestions;
        }
        
        return allQuestions.subList(0, limit);
    }

    @Override
    public List<Map<String, Object>> getRandomQuestionsByType(String type, int limit) {
        String categoryName = TYPE_NAME_MAP.get(type);
        if (categoryName == null) {
            categoryName = type;
        }

        LambdaQueryWrapper<Category> categoryWrapper = new LambdaQueryWrapper<>();
        categoryWrapper.eq(Category::getName, categoryName)
                      .eq(Category::getDeleted, 0);
        Category category = categoryService.getOne(categoryWrapper);

        if (category == null) {
            return Collections.emptyList();
        }

        List<Question> questions = getRandomQuestions(category.getId(), limit);

        return questions.stream().map(q -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", q.getId());
            map.put("content", q.getContent());
            map.put("analysis", q.getAnalysis());
            map.put("answer", q.getAnswer());
            map.put("type", q.getType());
            map.put("difficulty", q.getDifficulty());
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getRandomQuestionsByCategory(Long categoryId, int limit) {
        List<Long> categoryIds = new ArrayList<>();
        categoryIds.add(categoryId);
        
        LambdaQueryWrapper<Category> subCategoryWrapper = new LambdaQueryWrapper<>();
        subCategoryWrapper.eq(Category::getParentId, categoryId)
                         .eq(Category::getDeleted, 0);
        List<Category> subCategories = categoryService.list(subCategoryWrapper);
        for (Category sub : subCategories) {
            categoryIds.add(sub.getId());
        }
        
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Question::getCategoryId, categoryIds)
               .eq(Question::getDeleted, 0)
               .eq(Question::getStatus, 1);
        
        List<Question> allQuestions = this.list(wrapper);
        
        Collections.shuffle(allQuestions);
        
        List<Question> selectedQuestions = allQuestions.size() <= limit ? 
            allQuestions : allQuestions.subList(0, limit);

        return selectedQuestions.stream().map(q -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", q.getId());
            map.put("content", q.getContent());
            map.put("options", q.getOptions());
            map.put("analysis", q.getAnalysis());
            map.put("answer", q.getAnswer());
            map.put("type", q.getType());
            map.put("difficulty", q.getDifficulty());
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public void incrementViewCount(Long questionId) {
        Question question = this.getById(questionId);
        if (question != null) {
            int currentCount = question.getViewCount() != null ? question.getViewCount() : 0;
            question.setViewCount(currentCount + 1);
            this.updateById(question);
        }
    }
}
