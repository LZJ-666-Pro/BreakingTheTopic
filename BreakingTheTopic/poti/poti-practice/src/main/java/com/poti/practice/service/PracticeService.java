package com.poti.practice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.poti.practice.entity.Practice;

import java.util.List;
import java.util.Map;

public interface PracticeService extends IService<Practice> {
    
    Map<String, Object> submitAnswer(Long userId, Long questionId, Integer userAnswer, Integer spendSeconds);
    
    Map<String, Object> getHistory(Long userId, Integer pageNum, Integer pageSize);
    
    Map<String, Object> getStatistics(Long userId);
    
    Map<String, Object> getCalendar(Long userId, Integer year, Integer month);
    
    Map<String, Object> getRecordByQuestionId(Long userId, Long questionId);
    
    boolean deleteRecord(Long userId, Long recordId);
    
    boolean clearAllRecords(Long userId);
    
    List<Map<String, Object>> getRanking(String type, Integer offset, Integer limit);
    
    Map<String, Object> getUserRank(Long userId, String type);
}
