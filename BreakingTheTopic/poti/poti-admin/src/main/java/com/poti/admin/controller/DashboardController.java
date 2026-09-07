package com.poti.admin.controller;

import com.poti.admin.service.DashboardService;
import com.poti.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/overview")
    public R<Map<String, Object>> getOverview() {
        try {
            Map<String, Object> data = dashboardService.getOverview();
            return R.success(data);
        } catch (Exception e) {
            log.error("获取概览数据失败", e);
            return R.error("获取数据失败");
        }
    }

    @GetMapping("/question-stats")
    public R<Map<String, Object>> getQuestionStats() {
        try {
            Map<String, Object> data = dashboardService.getQuestionStats();
            return R.success(data);
        } catch (Exception e) {
            log.error("获取题目统计失败", e);
            return R.error("获取数据失败");
        }
    }

    @GetMapping("/user-stats")
    public R<Map<String, Object>> getUserStats() {
        try {
            Map<String, Object> data = dashboardService.getUserStats();
            return R.success(data);
        } catch (Exception e) {
            log.error("获取用户统计失败", e);
            return R.error("获取数据失败");
        }
    }

    @GetMapping("/category-stats")
    public R<List<Map<String, Object>>> getCategoryStats() {
        try {
            List<Map<String, Object>> data = dashboardService.getCategoryStats();
            return R.success(data);
        } catch (Exception e) {
            log.error("获取分类统计失败", e);
            return R.error("获取数据失败");
        }
    }
}
