package com.poti.admin.service.impl;

import com.poti.admin.mapper.DashboardMapper;
import com.poti.admin.service.DashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private DashboardMapper dashboardMapper;

    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> data = new HashMap<>();
        try {
            data.put("questionCount", dashboardMapper.countQuestions());
            data.put("categoryCount", dashboardMapper.countCategories());
            data.put("userCount", dashboardMapper.countUsers());
            data.put("enabledCount", dashboardMapper.countEnabledQuestions());
        } catch (Exception e) {
            log.error("获取概览数据失败", e);
            data.put("questionCount", 0);
            data.put("categoryCount", 0);
            data.put("userCount", 0);
            data.put("enabledCount", 0);
        }
        return data;
    }

    @Override
    public Map<String, Object> getQuestionStats() {
        Map<String, Object> data = new HashMap<>();
        try {
            data.put("easyCount", dashboardMapper.countEasyQuestions());
            data.put("mediumCount", dashboardMapper.countMediumQuestions());
            data.put("hardCount", dashboardMapper.countHardQuestions());
            
            Map<String, Integer> difficultyStats = new HashMap<>();
            difficultyStats.put("简单", dashboardMapper.countEasyQuestions());
            difficultyStats.put("中等", dashboardMapper.countMediumQuestions());
            difficultyStats.put("困难", dashboardMapper.countHardQuestions());
            data.put("difficultyStats", difficultyStats);
        } catch (Exception e) {
            log.error("获取题目统计失败", e);
            data.put("easyCount", 0);
            data.put("mediumCount", 0);
            data.put("hardCount", 0);
            data.put("difficultyStats", new HashMap<>());
        }
        return data;
    }

    @Override
    public Map<String, Object> getUserStats() {
        Map<String, Object> data = new HashMap<>();
        try {
            data.put("activeCount", dashboardMapper.countActiveUsers());
            data.put("disabledCount", dashboardMapper.countDisabledUsers());
        } catch (Exception e) {
            log.error("获取用户统计失败", e);
            data.put("activeCount", 0);
            data.put("disabledCount", 0);
        }
        return data;
    }

    @Override
    public List<Map<String, Object>> getCategoryStats() {
        try {
            return dashboardMapper.getCategoryStats();
        } catch (Exception e) {
            log.error("获取分类统计失败", e);
            return new ArrayList<>();
        }
    }
}
