package com.poti.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.poti.admin.dto.AiGenerateRequest;
import com.poti.admin.entity.AiTask;

public interface AiTaskService {
    
    AiTask createTask(AiGenerateRequest request);
    
    AiTask getTaskStatus(Long taskId);
    
    Page<AiTask> getTaskList(Integer page, Integer size);
    
    void processTask(Long taskId);
    
    void cancelTask(Long taskId);
    
    void deleteTask(Long taskId);
}
