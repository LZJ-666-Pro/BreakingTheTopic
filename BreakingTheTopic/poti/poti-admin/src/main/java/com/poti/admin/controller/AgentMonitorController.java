package com.poti.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.poti.admin.mapper.AgentMonitorMapper;
import com.poti.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 助手监控（poti_agent 库，只读）：使用概览 + 会话记录 + 工具调用日志。
 * 供管理端「AI 助手监控」页面使用，跟随 /admin/** 管理员 JWT 鉴权。
 */
@Slf4j
@RestController
@RequestMapping("/admin/agent")
public class AgentMonitorController {

    @Autowired
    private AgentMonitorMapper agentMonitorMapper;

    /** 使用概览：今日/累计核心指标 + 近7天工具成功率与平均耗时 */
    @GetMapping("/overview")
    public R<Map<String, Object>> overview() {
        try {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("todaySessions", agentMonitorMapper.countTodaySessions());
            data.put("todayQuestions", agentMonitorMapper.countTodayQuestions());
            data.put("todayToolCalls", agentMonitorMapper.countTodayToolCalls());
            data.put("todayActiveUsers", agentMonitorMapper.countTodayActiveUsers());
            data.put("totalSessions", agentMonitorMapper.countTotalSessions());
            data.put("totalMessages", agentMonitorMapper.countTotalMessages());
            data.put("totalUsers", agentMonitorMapper.countTotalUsers());

            long calls7d = agentMonitorMapper.countToolCalls7d();
            long success7d = agentMonitorMapper.countToolSuccess7d();
            data.put("toolCalls7d", calls7d);
            data.put("toolSuccessRate7d", calls7d > 0 ? Math.round(success7d * 1000.0 / calls7d) / 10.0 : null);
            data.put("avgToolCost7d", agentMonitorMapper.avgToolCost7d());
            return R.success(data);
        } catch (Exception e) {
            log.error("获取 AI 助手概览失败", e);
            return R.error("获取 AI 助手概览失败");
        }
    }

    /** 会话列表（含消息数），按标题关键字过滤 */
    @GetMapping("/sessions")
    public R<IPage<Map<String, Object>>> sessions(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        try {
            return R.success(agentMonitorMapper.selectSessions(
                    new Page<>(pageNum, pageSize),
                    keyword == null ? null : keyword.trim()));
        } catch (Exception e) {
            log.error("获取 AI 会话列表失败", e);
            return R.error("获取 AI 会话列表失败");
        }
    }

    /** 会话消息详情（按会话查看完整对话） */
    @GetMapping("/messages")
    public R<List<Map<String, Object>>> messages(@RequestParam Long sessionId) {
        try {
            return R.success(agentMonitorMapper.selectMessages(sessionId));
        } catch (Exception e) {
            log.error("获取 AI 会话消息失败", e);
            return R.error("获取 AI 会话消息失败");
        }
    }

    /** 工具调用日志（按工具名模糊 + 成功状态过滤） */
    @GetMapping("/toolLogs")
    public R<IPage<Map<String, Object>>> toolLogs(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String toolName,
            @RequestParam(required = false) Integer success) {
        try {
            return R.success(agentMonitorMapper.selectToolLogs(
                    new Page<>(pageNum, pageSize),
                    toolName == null ? null : toolName.trim(),
                    success));
        } catch (Exception e) {
            log.error("获取 AI 工具日志失败", e);
            return R.error("获取 AI 工具日志失败");
        }
    }
}
