package com.poti.question.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poti.common.R;
import com.poti.question.entity.Category;
import com.poti.question.entity.Question;
import com.poti.question.service.CategoryService;
import com.poti.question.service.QuestionService;
import com.poti.question.service.SearchSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/question")
@Tag(name = "题目管理", description = "题目和分类相关接口")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired(required = false)
    private SearchSyncService searchSyncService;

    @GetMapping("/categories")
    @Operation(summary = "获取题目分类列表", description = "获取所有题目分类信息，包含题目数量统计")
    public R<List<Map<String, Object>>> getCategories() {
        try {
            LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Category::getDeleted, 0)
                   .orderByAsc(Category::getSort);

            List<Category> categories = categoryService.list(wrapper);
            
            List<Map<String, Object>> result = categories.stream().map(cat -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", cat.getId());
                map.put("name", cat.getName());
                map.put("parentId", cat.getParentId());
                map.put("sort", cat.getSort());
                map.put("icon", cat.getIcon());
                String iconUrl = cat.getIconUrl();
                map.put("iconUrl", (iconUrl != null && !iconUrl.trim().isEmpty()) ? iconUrl : null);
                map.put("description", cat.getDescription());
                
                LambdaQueryWrapper<Question> qWrapper = new LambdaQueryWrapper<>();
                qWrapper.eq(Question::getCategoryId, cat.getId())
                        .eq(Question::getDeleted, 0)
                        .eq(Question::getStatus, 1);
                long count = questionService.count(qWrapper);
                map.put("questionCount", count);
                
                return map;
            }).collect(java.util.stream.Collectors.toList());
            
            return R.success(result);
        } catch (Exception e) {
            log.error("获取分类列表失败", e);
            return R.error("获取分类列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    @Operation(summary = "获取题目列表", description = "根据分类ID获取题目列表，不传分类ID则获取所有题目")
    public R<List<Map<String, Object>>> getQuestionList(@RequestParam(required = false) Long categoryId) {
        try {
            LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
            if (categoryId != null) {
                wrapper.eq(Question::getCategoryId, categoryId);
            }
            wrapper.eq(Question::getDeleted, 0);
            wrapper.eq(Question::getStatus, 1);
            
            List<Question> questions = questionService.list(wrapper);
            
            List<Map<String, Object>> result = questions.stream().map(q -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", q.getId());
                map.put("categoryId", q.getCategoryId());
                map.put("title", q.getTitle());
                map.put("content", q.getContent());
                map.put("type", q.getType());
                map.put("difficulty", q.getDifficulty());
                map.put("answer", q.getAnswer());
                map.put("analysis", q.getAnalysis());
                map.put("tags", q.getTags());
                map.put("viewCount", q.getViewCount());
                map.put("favoriteCount", q.getFavoriteCount());
                
                if (q.getOptions() != null && !q.getOptions().isEmpty()) {
                    try {
                        List<String> options = objectMapper.readValue(q.getOptions(), new TypeReference<List<String>>() {});
                        map.put("options", options);
                    } catch (Exception e) {
                        map.put("options", q.getOptions());
                    }
                }
                
                return map;
            }).collect(java.util.stream.Collectors.toList());
            
            return R.success(result);
        } catch (Exception e) {
            return R.error("获取题目列表失败");
        }
    }

    @GetMapping("/random")
    public R<List<Map<String, Object>>> getRandomQuestions(@RequestParam Long categoryId,
                                                 @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Question> questions = questionService.getRandomQuestions(categoryId, limit);
            
            List<Map<String, Object>> result = questions.stream().map(q -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", q.getId());
                map.put("categoryId", q.getCategoryId());
                map.put("title", q.getTitle());
                map.put("content", q.getContent());
                map.put("type", q.getType());
                map.put("difficulty", q.getDifficulty());
                map.put("answer", q.getAnswer());
                map.put("analysis", q.getAnalysis());
                
                if (q.getOptions() != null && !q.getOptions().isEmpty()) {
                    try {
                        List<String> options = objectMapper.readValue(q.getOptions(), new TypeReference<List<String>>() {});
                        map.put("options", options);
                    } catch (Exception e) {
                        map.put("options", q.getOptions());
                    }
                }
                
                return map;
            }).collect(java.util.stream.Collectors.toList());
            
            return R.success(result);
        } catch (Exception e) {
            return R.error("获取随机题目失败");
        }
    }

    @GetMapping("/random/type")
    public R<List<Map<String, Object>>> getRandomQuestionsByType(@RequestParam String type,
                                                                  @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Map<String, Object>> questions = questionService.getRandomQuestionsByType(type, limit);
            return R.success(questions);
        } catch (Exception e) {
            return R.error("获取随机题目失败");
        }
    }

    @GetMapping("/random/category")
    public R<List<Map<String, Object>>> getRandomQuestionsByCategory(@RequestParam Long categoryId,
                                                                      @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Map<String, Object>> questions = questionService.getRandomQuestionsByCategory(categoryId, limit);
            return R.success(questions);
        } catch (Exception e) {
            return R.error("获取随机题目失败");
        }
    }

    @GetMapping("/random/global")
    public R<List<Map<String, Object>>> getRandomQuestionsGlobal(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<Question> questions = questionService.getRandomQuestionsGlobal(limit);
            
            List<Map<String, Object>> result = questions.stream().map(q -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", q.getId());
                map.put("categoryId", q.getCategoryId());
                map.put("title", q.getTitle());
                map.put("content", q.getContent());
                map.put("type", q.getType());
                map.put("difficulty", q.getDifficulty());
                map.put("answer", q.getAnswer());
                map.put("analysis", q.getAnalysis());
                map.put("viewCount", q.getViewCount());
                
                if (q.getOptions() != null && !q.getOptions().isEmpty()) {
                    try {
                        List<String> options = objectMapper.readValue(q.getOptions(), new TypeReference<List<String>>() {});
                        map.put("options", options);
                    } catch (Exception e) {
                        map.put("options", q.getOptions());
                    }
                }
                
                return map;
            }).collect(java.util.stream.Collectors.toList());
            
            return R.success(result);
        } catch (Exception e) {
            return R.error("获取随机题目失败");
        }
    }

    @GetMapping("/{id}")
    public R<Question> getQuestionById(@PathVariable Long id) {
        try {
            Question question = questionService.getById(id);
            if (question == null) {
                return R.error("题目不存在");
            }
            return R.success(question);
        } catch (Exception e) {
            return R.error("获取题目失败");
        }
    }

    @GetMapping("/detail/{questionId}")
    public R<Map<String, Object>> getQuestionDetail(@PathVariable Long questionId) {
        try {
            Question question = questionService.getById(questionId);
            if (question == null) {
                return R.error("题目不存在");
            }
            
            questionService.incrementViewCount(questionId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("id", question.getId());
            result.put("categoryId", question.getCategoryId());
            result.put("title", question.getTitle());
            result.put("content", question.getContent());
            result.put("type", question.getType());
            result.put("difficulty", question.getDifficulty());
            result.put("answer", question.getAnswer());
            result.put("analysis", question.getAnalysis());
            result.put("tags", question.getTags());
            result.put("viewCount", question.getViewCount() + 1);
            
            if (question.getOptions() != null && !question.getOptions().isEmpty()) {
                List<String> options = objectMapper.readValue(question.getOptions(), new TypeReference<List<String>>() {});
                result.put("options", options);
            }
            
            return R.success(result);
        } catch (Exception e) {
            return R.error("获取题目详情失败");
        }
    }

    @PostMapping("/view/{questionId}")
    public R<String> incrementViewCount(@PathVariable Long questionId) {
        try {
            Question question = questionService.getById(questionId);
            if (question == null) {
                return R.error("题目不存在");
            }
            questionService.incrementViewCount(questionId);
            return R.success("浏览量已更新");
        } catch (Exception e) {
            return R.error("更新浏览量失败");
        }
    }

    @GetMapping("/internal/{id}")
    public R<Map<String, Object>> getQuestionInternal(@PathVariable Long id) {
        try {
            Question question = questionService.getById(id);
            if (question == null) {
                return R.error("题目不存在");
            }
            Map<String, Object> result = new HashMap<>();
            result.put("id", question.getId());
            result.put("title", question.getTitle());
            result.put("type", question.getType());
            result.put("answer", question.getAnswer());
            result.put("analysis", question.getAnalysis());
            result.put("categoryId", question.getCategoryId());
            
            if (question.getCategoryId() != null) {
                Category category = categoryService.getById(question.getCategoryId());
                if (category != null) {
                    result.put("categoryName", category.getName());
                }
            }
            
            if (question.getOptions() != null && !question.getOptions().isEmpty()) {
                List<String> options = objectMapper.readValue(question.getOptions(), new TypeReference<List<String>>() {});
                if (options.size() > 0) result.put("optionA", options.get(0));
                if (options.size() > 1) result.put("optionB", options.get(1));
                if (options.size() > 2) result.put("optionC", options.get(2));
                if (options.size() > 3) result.put("optionD", options.get(3));
            }
            
            return R.success(result);
        } catch (Exception e) {
            return R.error("获取题目失败");
        }
    }

    @PostMapping("/save")
    public R<Question> saveQuestion(@RequestBody Map<String, Object> request) {
        try {
            Question question = new Question();
            question.setCategoryId(Long.valueOf(request.get("categoryId").toString()));
            question.setTitle((String) request.get("title"));
            question.setContent((String) request.get("content"));
            question.setType(request.get("type") != null ? Integer.valueOf(request.get("type").toString()) : 1);
            question.setDifficulty(request.get("difficulty") != null ? Integer.valueOf(request.get("difficulty").toString()) : 2);
            question.setAnswer((String) request.get("answer"));
            question.setAnalysis((String) request.get("analysis"));
            question.setTags((String) request.get("tags"));
            
            if (request.get("options") != null) {
                String optionsJson = objectMapper.writeValueAsString(request.get("options"));
                question.setOptions(optionsJson);
            }
            
            question.setStatus(1);
            question.setViewCount(0);
            question.setFavoriteCount(0);
            questionService.save(question);
            
            if (searchSyncService != null) {
                searchSyncService.syncQuestion(question);
            }
            
            return R.success(question);
        } catch (Exception e) {
            return R.error("创建题目失败");
        }
    }

    @PutMapping("/update")
    public R<Question> updateQuestion(@RequestBody Map<String, Object> request) {
        try {
            Long id = Long.valueOf(request.get("id").toString());
            Question question = questionService.getById(id);
            if (question == null) {
                return R.error("题目不存在");
            }
            
            if (request.get("categoryId") != null) {
                question.setCategoryId(Long.valueOf(request.get("categoryId").toString()));
            }
            if (request.get("title") != null) {
                question.setTitle((String) request.get("title"));
            }
            if (request.get("content") != null) {
                question.setContent((String) request.get("content"));
            }
            if (request.get("type") != null) {
                question.setType(Integer.valueOf(request.get("type").toString()));
            }
            if (request.get("difficulty") != null) {
                question.setDifficulty(Integer.valueOf(request.get("difficulty").toString()));
            }
            if (request.get("answer") != null) {
                question.setAnswer((String) request.get("answer"));
            }
            if (request.get("analysis") != null) {
                question.setAnalysis((String) request.get("analysis"));
            }
            if (request.get("tags") != null) {
                question.setTags((String) request.get("tags"));
            }
            if (request.get("options") != null) {
                String optionsJson = objectMapper.writeValueAsString(request.get("options"));
                question.setOptions(optionsJson);
            }
            
            questionService.updateById(question);
            
            if (searchSyncService != null) {
                searchSyncService.syncQuestion(question);
            }
            
            return R.success(question);
        } catch (Exception e) {
            return R.error("更新题目失败");
        }
    }

    @DeleteMapping("/{id}")
    public R<String> deleteQuestion(@PathVariable Long id) {
        try {
            Question question = questionService.getById(id);
            if (question == null) {
                return R.error("题目不存在");
            }
            question.setDeleted(1);
            questionService.updateById(question);
            return R.success("删除成功");
        } catch (Exception e) {
            return R.error("删除题目失败");
        }
    }

    @GetMapping("/stats")
    public R<Map<String, Object>> getStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            LambdaQueryWrapper<Question> questionWrapper = new LambdaQueryWrapper<>();
            questionWrapper.eq(Question::getDeleted, 0);
            long questionCount = questionService.count(questionWrapper);
            stats.put("questionCount", questionCount);
            
            LambdaQueryWrapper<Category> categoryWrapper = new LambdaQueryWrapper<>();
            categoryWrapper.eq(Category::getDeleted, 0);
            long categoryCount = categoryService.count(categoryWrapper);
            stats.put("categoryCount", categoryCount);
            
            stats.put("userCount", 0);
            stats.put("practiceCount", 0);
            
            return R.success(stats);
        } catch (Exception e) {
            return R.error("获取统计信息失败");
        }
    }

    @GetMapping("/search")
    public R<Map<String, Object>> searchQuestions(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize) {
        try {
            LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Question::getDeleted, 0);
            wrapper.eq(Question::getStatus, 1);
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                wrapper.and(w -> w
                    .like(Question::getTitle, keyword)
                    .or()
                    .like(Question::getContent, keyword)
                    .or()
                    .like(Question::getAnalysis, keyword)
                    .or()
                    .like(Question::getTags, keyword)
                );
            }
            
            if (categoryId != null) {
                wrapper.eq(Question::getCategoryId, categoryId);
            }
            
            wrapper.orderByDesc(Question::getId);
            
            long total = questionService.count(wrapper);
            
            int offset = (pageNum - 1) * pageSize;
            wrapper.last("LIMIT " + offset + ", " + pageSize);
            
            List<Question> questions = questionService.list(wrapper);
            
            List<Map<String, Object>> list = questions.stream().map(q -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", q.getId());
                map.put("categoryId", q.getCategoryId());
                map.put("title", q.getTitle());
                map.put("content", q.getContent());
                map.put("type", q.getType());
                map.put("difficulty", q.getDifficulty());
                map.put("answer", q.getAnswer());
                map.put("analysis", q.getAnalysis());
                map.put("tags", q.getTags());
                
                if (q.getCategoryId() != null) {
                    Category category = categoryService.getById(q.getCategoryId());
                    if (category != null) {
                        map.put("categoryName", category.getName());
                    }
                }
                
                if (q.getOptions() != null && !q.getOptions().isEmpty()) {
                    try {
                        List<String> options = objectMapper.readValue(q.getOptions(), new TypeReference<List<String>>() {});
                        map.put("options", options);
                    } catch (Exception e) {
                        map.put("options", q.getOptions());
                    }
                }
                
                return map;
            }).collect(java.util.stream.Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("total", total);
            result.put("list", list);
            
            return R.success(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("total", 0);
            result.put("list", java.util.Collections.emptyList());
            return R.success(result);
        }
    }

    @PostMapping("/sync-to-es")
    public R<String> syncToElasticsearch() {
        if (searchSyncService == null) {
            return R.error("ES同步服务未启用");
        }
        try {
            searchSyncService.syncAllQuestions();
            return R.success("同步任务已启动");
        } catch (Exception e) {
            return R.error("同步失败: " + e.getMessage());
        }
    }
}
