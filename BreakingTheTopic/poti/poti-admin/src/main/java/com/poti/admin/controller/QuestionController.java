package com.poti.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.poti.admin.dto.AiGenerateRequest;
import com.poti.admin.dto.AiQuestionDTO;
import com.poti.admin.entity.AiTask;
import com.poti.admin.entity.Question;
import com.poti.admin.service.AiQuestionService;
import com.poti.admin.service.AiTaskService;
import com.poti.admin.service.QuestionService;
import com.poti.common.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/question")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AiQuestionService aiQuestionService;

    @Autowired
    private AiTaskService aiTaskService;

    @GetMapping("/page")
    public R<Page<Question>> pageList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        Page<Question> result = questionService.pageList(page, size, categoryId, difficulty, status, keyword);
        return R.success(result);
    }

    @GetMapping("/{id}")
    public R<Question> getById(@PathVariable Long id) {
        Question question = questionService.getById(id);
        if (question == null) {
            return R.error("题目不存在");
        }
        return R.success(question);
    }

    @PostMapping
    public R<Void> add(@RequestBody Question question) {
        boolean success = questionService.addQuestion(question);
        return success ? R.success(null) : R.error("添加失败");
    }

    @PutMapping
    public R<Void> update(@RequestBody Question question) {
        boolean success = questionService.updateQuestion(question);
        return success ? R.success(null) : R.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        boolean success = questionService.deleteQuestion(id);
        return success ? R.success(null) : R.error("删除失败");
    }

    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        boolean success = questionService.updateStatus(id, status);
        return success ? R.success(null) : R.error("操作失败");
    }

    @PostMapping("/import")
    public R<Map<String, Object>> importQuestions(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> result = questionService.importQuestionsFromExcel(file);
            return R.success(result);
        } catch (Exception e) {
            return R.error("导入失败: " + e.getMessage());
        }
    }

    @PostMapping("/ai/generate")
    public R<Map<String, Object>> generateByAi(@RequestBody AiGenerateRequest request) {
        try {
            List<AiQuestionDTO> questions = aiQuestionService.generateQuestions(request);
            Map<String, Object> result = new HashMap<>();
            result.put("questions", questions);
            result.put("count", questions.size());
            return R.success(result);
        } catch (Exception e) {
            return R.error("AI生成失败: " + e.getMessage());
        }
    }

    @PostMapping("/ai/save")
    public R<Void> saveAiQuestions(@RequestParam Long categoryId, @RequestBody List<AiQuestionDTO> questions) {
        try {
            boolean success = aiQuestionService.saveGeneratedQuestions(categoryId, questions);
            return success ? R.success(null) : R.error("保存失败");
        } catch (Exception e) {
            return R.error("保存失败: " + e.getMessage());
        }
    }

    @PostMapping("/ai/task/create")
    public R<Map<String, Object>> createAiTask(@RequestBody AiGenerateRequest request) {
        try {
            AiTask task = aiTaskService.createTask(request);
            aiTaskService.processTask(task.getId());
            
            Map<String, Object> result = new HashMap<>();
            result.put("taskId", task.getId());
            result.put("message", "任务已创建，正在后台生成");
            return R.success(result);
        } catch (Exception e) {
            return R.error("创建任务失败: " + e.getMessage());
        }
    }

    @GetMapping("/ai/task/{taskId}")
    public R<AiTask> getAiTaskStatus(@PathVariable Long taskId) {
        AiTask task = aiTaskService.getTaskStatus(taskId);
        if (task == null) {
            return R.error("任务不存在");
        }
        return R.success(task);
    }

    @GetMapping("/ai/task/list")
    public R<Page<AiTask>> getAiTaskList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<AiTask> taskPage = aiTaskService.getTaskList(page, size);
        return R.success(taskPage);
    }

    @PostMapping("/ai/task/{taskId}/cancel")
    public R<Void> cancelAiTask(@PathVariable Long taskId) {
        aiTaskService.cancelTask(taskId);
        return R.success(null);
    }

    @DeleteMapping("/ai/task/{taskId}")
    public R<Void> deleteAiTask(@PathVariable Long taskId) {
        aiTaskService.deleteTask(taskId);
        return R.success(null);
    }
}
