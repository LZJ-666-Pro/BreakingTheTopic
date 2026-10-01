package com.poti.admin.service.impl;

import com.poti.admin.entity.User;
import com.poti.admin.mapper.DashboardMapper;
import com.poti.admin.mapper.UserMapper;
import com.poti.admin.service.DashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private DashboardMapper dashboardMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> data = new HashMap<>();
        try {
            data.put("questionCount", dashboardMapper.countQuestions());
            data.put("categoryCount", dashboardMapper.countCategories());
            data.put("userCount", dashboardMapper.countUsers());
            data.put("enabledCount", dashboardMapper.countEnabledQuestions());
            data.put("todayQuestionCount", dashboardMapper.countTodayQuestions());
            data.put("pendingFeedbackCount", dashboardMapper.countPendingFeedback());
            data.put("weekActiveUserCount", dashboardMapper.countWeekActiveUsers());
        } catch (Exception e) {
            log.error("获取概览数据失败", e);
            data.put("questionCount", 0);
            data.put("categoryCount", 0);
            data.put("userCount", 0);
            data.put("enabledCount", 0);
            data.put("todayQuestionCount", 0);
            data.put("pendingFeedbackCount", 0);
            data.put("weekActiveUserCount", 0);
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
            data.put("todayActiveCount", dashboardMapper.countTodayActiveUsers());
            data.put("weekActiveCount", dashboardMapper.countWeekActiveUsers());
        } catch (Exception e) {
            log.error("获取用户统计失败", e);
            data.put("activeCount", 0);
            data.put("disabledCount", 0);
            data.put("todayActiveCount", 0);
            data.put("weekActiveCount", 0);
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

    @Override
    public List<Map<String, Object>> getRecentActiveUsers() {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            List<Map<String, Object>> practiceUsers = dashboardMapper.getRecentPracticeUsers();
            if (practiceUsers == null || practiceUsers.isEmpty()) {
                return result;
            }

            List<Long> userIds = practiceUsers.stream()
                    .map(item -> ((Number) item.get("userId")).longValue())
                    .distinct()
                    .collect(Collectors.toList());

            Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                    .collect(Collectors.toMap(User::getId, Function.identity()));

            for (Map<String, Object> item : practiceUsers) {
                Long userId = ((Number) item.get("userId")).longValue();
                User user = userMap.get(userId);
                if (user == null) {
                    continue;
                }
                Map<String, Object> row = new HashMap<>();
                row.put("userId", userId);
                row.put("nickname", user.getNickname());
                row.put("avatarUrl", user.getAvatarUrl());
                row.put("practiceCount", item.get("practiceCount"));
                row.put("lastPracticeTime", item.get("lastPracticeTime"));
                result.add(row);
            }
        } catch (Exception e) {
            log.error("获取最近活跃用户失败", e);
        }
        return result;
    }
}
