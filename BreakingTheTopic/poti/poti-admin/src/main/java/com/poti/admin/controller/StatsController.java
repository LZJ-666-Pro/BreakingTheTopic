package com.poti.admin.controller;

import com.poti.admin.mapper.StatsMapper;
import com.poti.common.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 数据统计接口：聚合用户、刷题、题库、特训营四个维度的核心指标，
 * 一次返回，供后台「数据统计」页面渲染。
 */
@RestController
@RequestMapping("/admin/stats")
public class StatsController {

    @Autowired
    private StatsMapper statsMapper;

    @GetMapping("/overview")
    public R<Map<String, Object>> overview() {
        Map<String, Object> data = new LinkedHashMap<>();
        try {
            // 用户数据
            data.put("user", statsMapper.selectUserOverview());
            data.put("lostUsers", statsMapper.countLostUsers());
            data.put("retention", statsMapper.selectRetention());
            data.put("userTrend", statsMapper.selectUserTrend());
            data.put("streakDist", statsMapper.selectStreakDistribution());
        } catch (Exception e) {
            e.printStackTrace();
            data.put("user", new HashMap<>());
            data.put("lostUsers", 0);
            data.put("retention", new HashMap<>());
            data.put("userTrend", new HashMap<>());
            data.put("streakDist", new HashMap<>());
        }
        try {
            // 刷题行为数据
            data.put("practice", statsMapper.selectPracticeOverview());
            data.put("practiceTrend", statsMapper.selectPracticeTrend());
            data.put("difficultyStats", statsMapper.selectDifficultyStats());
            data.put("categoryHot", statsMapper.selectCategoryHot());
            data.put("hotQuestions", statsMapper.selectHotQuestions());
        } catch (Exception e) {
            e.printStackTrace();
            data.put("practice", new HashMap<>());
            data.put("practiceTrend", new HashMap<>());
            data.put("difficultyStats", new HashMap<>());
            data.put("categoryHot", new HashMap<>());
            data.put("hotQuestions", new HashMap<>());
        }
        try {
            // 题库数据
            data.put("question", statsMapper.selectQuestionOverview());
        } catch (Exception e) {
            data.put("question", new HashMap<>());
        }
        try {
            // 特训营数据
            data.put("camp", statsMapper.selectCampOverview());
        } catch (Exception e) {
            data.put("camp", new HashMap<>());
        }
        return R.success(data);
    }
}
