package com.poti.practice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.poti.common.R;
import com.poti.practice.entity.Practice;
import com.poti.practice.feign.QuestionFeignClient;
import com.poti.practice.mapper.PracticeMapper;
import com.poti.practice.service.PracticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class PracticeServiceImpl extends ServiceImpl<PracticeMapper, Practice> implements PracticeService {

    @Autowired
    private QuestionFeignClient questionFeignClient;

    @Override
    public Map<String, Object> submitAnswer(Long userId, Long questionId, Integer userAnswer, Integer spendSeconds) {
        Map<String, Object> result = new HashMap<>();

        String correctAnswer = null;
        String analysis = null;
        boolean isCorrect = false;

        try {
            R<Map<String, Object>> questionResponse = questionFeignClient.getQuestionById(questionId);
            if (questionResponse != null && questionResponse.getData() != null) {
                Map<String, Object> questionData = questionResponse.getData();
                correctAnswer = (String) questionData.get("answer");
                analysis = (String) questionData.get("analysis");
                
                if (correctAnswer != null) {
                    String userAnswerStr = String.valueOf((char) ('A' + userAnswer));
                    isCorrect = correctAnswer.equalsIgnoreCase(userAnswerStr);
                }
            }
        } catch (Exception e) {
            System.err.println("获取题目信息失败: " + e.getMessage());
        }

        Practice practice = new Practice();
        practice.setUserId(userId);
        practice.setQuestionId(questionId);
        practice.setUserAnswer(String.valueOf((char) ('A' + userAnswer)));
        practice.setSpendSeconds(spendSeconds);
        practice.setIsCorrect(isCorrect);
        practice.setPracticeTime(LocalDateTime.now());
        practice.setCreateTime(LocalDateTime.now());
        baseMapper.insert(practice);

        result.put("isCorrect", isCorrect);
        result.put("correctAnswer", correctAnswer);
        result.put("analysis", analysis);

        return result;
    }

    @Override
    public Map<String, Object> getHistory(Long userId, Integer pageNum, Integer pageSize) {
        Map<String, Object> result = new HashMap<>();

        LambdaQueryWrapper<Practice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Practice::getUserId, userId)
               .orderByDesc(Practice::getCreateTime);

        Page<Practice> page = new Page<>(pageNum, pageSize);
        Page<Practice> resultPage = baseMapper.selectPage(page, wrapper);

        Map<Long, Practice> uniquePractices = new LinkedHashMap<>();
        for (Practice practice : resultPage.getRecords()) {
            if (!uniquePractices.containsKey(practice.getQuestionId())) {
                uniquePractices.put(practice.getQuestionId(), practice);
            }
        }

        List<Map<String, Object>> records = new ArrayList<>();
        for (Practice practice : uniquePractices.values()) {
            Map<String, Object> record = new HashMap<>();
            record.put("id", practice.getId());
            record.put("questionId", practice.getQuestionId());
            record.put("userAnswer", practice.getUserAnswer());
            record.put("isCorrect", practice.getIsCorrect());
            record.put("spendSeconds", practice.getSpendSeconds());
            record.put("practiceTime", practice.getPracticeTime());
            record.put("createTime", practice.getCreateTime());
            
            try {
                R<Map<String, Object>> questionResponse = questionFeignClient.getQuestionById(practice.getQuestionId());
                if (questionResponse != null && questionResponse.getData() != null) {
                    Map<String, Object> questionData = questionResponse.getData();
                    record.put("questionTitle", questionData.get("title"));
                    record.put("categoryName", questionData.get("categoryName"));
                } else {
                    record.put("questionTitle", "题目 #" + practice.getQuestionId());
                    record.put("categoryName", "题目 #" + practice.getQuestionId());
                }
            } catch (Exception e) {
                System.err.println("获取题目信息失败: " + e.getMessage());
                record.put("questionTitle", "题目 #" + practice.getQuestionId());
                record.put("categoryName", "题目 #" + practice.getQuestionId());
            }
            
            records.add(record);
        }

        result.put("total", uniquePractices.size());
        result.put("list", records);

        return result;
    }

    @Override
    public Map<String, Object> getStatistics(Long userId) {
        Map<String, Object> statistics = new HashMap<>();

        LambdaQueryWrapper<Practice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Practice::getUserId, userId);

        List<Practice> practices = baseMapper.selectList(wrapper);

        int totalCount = practices.size();
        int correctCount = (int) practices.stream().filter(p -> Boolean.TRUE.equals(p.getIsCorrect())).count();
        int wrongCount = totalCount - correctCount;

        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        int todayCount = 0;
        int todayCorrectCount = 0;
        for (Practice p : practices) {
            if (p.getPracticeTime() != null && p.getPracticeTime().isAfter(todayStart)) {
                todayCount++;
                if (Boolean.TRUE.equals(p.getIsCorrect())) {
                    todayCorrectCount++;
                }
            }
        }
        int todayWrongCount = todayCount - todayCorrectCount;

        statistics.put("totalQuestionCount", totalCount);
        statistics.put("correctCount", correctCount);
        statistics.put("wrongCount", wrongCount);
        statistics.put("todayCount", todayCount);
        statistics.put("todayCorrectCount", todayCorrectCount);
        statistics.put("todayWrongCount", todayWrongCount);

        if (!practices.isEmpty()) {
            practices.sort((a, b) -> {
                if (a.getCreateTime() == null && b.getCreateTime() == null) return 0;
                if (a.getCreateTime() == null) return 1;
                if (b.getCreateTime() == null) return -1;
                return b.getCreateTime().compareTo(a.getCreateTime());
            });
            statistics.put("lastPracticeTime", practices.get(0).getCreateTime());
        } else {
            statistics.put("lastPracticeTime", null);
        }

        return statistics;
    }

    @Override
    public Map<String, Object> getCalendar(Long userId, Integer year, Integer month) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> calendar = baseMapper.getCalendarByMonth(userId, year, month);
        result.put("list", calendar);
        return result;
    }

    @Override
    public Map<String, Object> getRecordByQuestionId(Long userId, Long questionId) {
        Map<String, Object> result = new HashMap<>();
        
        LambdaQueryWrapper<Practice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Practice::getUserId, userId)
               .eq(Practice::getQuestionId, questionId)
               .orderByDesc(Practice::getCreateTime);
        
        List<Practice> records = baseMapper.selectList(wrapper);
        
        result.put("total", records.size());
        result.put("list", records);
        
        if (!records.isEmpty()) {
            Practice latest = records.get(0);
            result.put("latestRecord", latest);
            result.put("isCorrect", latest.getIsCorrect());
        }
        
        return result;
    }

    @Override
    public boolean deleteRecord(Long userId, Long recordId) {
        LambdaQueryWrapper<Practice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Practice::getId, recordId)
               .eq(Practice::getUserId, userId);
        
        return baseMapper.delete(wrapper) > 0;
    }

    @Override
    public boolean clearAllRecords(Long userId) {
        LambdaQueryWrapper<Practice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Practice::getUserId, userId);
        
        return baseMapper.delete(wrapper) > 0;
    }

    @Override
    public List<Map<String, Object>> getRanking(String type, Integer offset, Integer limit) {
        switch (type) {
            case "total":
                return baseMapper.getRankingByTotalQuestions(offset, limit);
            case "today":
                return baseMapper.getRankingByTodayQuestions(offset, limit);
            case "accuracy":
                return baseMapper.getRankingByAccuracy(offset, limit);
            default:
                return baseMapper.getRankingByTotalQuestions(offset, limit);
        }
    }

    @Override
    public Map<String, Object> getUserRank(Long userId, String type) {
        Map<String, Object> result = new HashMap<>();
        Integer rank = null;
        
        switch (type) {
            case "total":
                rank = baseMapper.getUserRankByTotalQuestions(userId);
                break;
            case "today":
                rank = baseMapper.getUserRankByTodayQuestions(userId);
                break;
            case "accuracy":
                rank = baseMapper.getUserRankByAccuracy(userId);
                break;
            default:
                rank = baseMapper.getUserRankByTotalQuestions(userId);
        }
        
        result.put("rank", rank != null ? rank : 1);
        
        Map<String, Object> stats = getStatistics(userId);
        result.put("totalQuestions", stats.get("totalQuestionCount"));
        result.put("todayQuestions", stats.get("todayCount"));
        result.put("correctCount", stats.get("correctCount"));
        
        return result;
    }
}
