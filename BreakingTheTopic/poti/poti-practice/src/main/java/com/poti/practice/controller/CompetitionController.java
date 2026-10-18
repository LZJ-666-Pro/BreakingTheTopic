package com.poti.practice.controller;

import com.poti.common.R;
import com.poti.practice.service.CompetitionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 竞赛用户端接口（小程序端）
 * 身份一律取网关鉴权过滤器注入的 X-User-Id，不信任前端传参
 */
@Slf4j
@RestController
@RequestMapping("/competition")
public class CompetitionController {

    @Autowired
    private CompetitionService competitionService;

    /** 竞赛分页列表：tab∈{signup报名中, ongoing进行中, ended已结束}，缺省全部 */
    @GetMapping("/page")
    public R<Map<String, Object>> page(@RequestHeader("X-User-Id") Long userId,
                                       @RequestParam(defaultValue = "1") Integer pageNum,
                                       @RequestParam(defaultValue = "10") Integer pageSize,
                                       @RequestParam(required = false) String tab) {
        try {
            return R.success(competitionService.pageList(userId, pageNum, pageSize, tab));
        } catch (Exception e) {
            log.error("获取竞赛列表失败", e);
            return R.error("获取竞赛列表失败");
        }
    }

    /** 竞赛详情（含我的报名状态与我的成绩） */
    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@RequestHeader("X-User-Id") Long userId,
                                         @PathVariable Long id) {
        try {
            return R.success(competitionService.detail(userId, id));
        } catch (Exception e) {
            log.error("获取竞赛详情失败", e);
            return R.error(e.getMessage());
        }
    }

    /** 报名竞赛 */
    @PostMapping("/{id}/signup")
    public R<Void> signup(@RequestHeader("X-User-Id") Long userId,
                          @PathVariable Long id) {
        try {
            competitionService.signup(userId, id);
            return R.success();
        } catch (Exception e) {
            log.error("竞赛报名失败", e);
            return R.error(e.getMessage());
        }
    }

    /** 开赛：抽题或断点续答，返回 {questions:[...]}（不含答案），与前端 competition-quiz 页解析结构一致 */
    @PostMapping("/{id}/start")
    public R<Map<String, Object>> start(@RequestHeader("X-User-Id") Long userId,
                                        @PathVariable Long id) {
        try {
            Map<String, Object> data = new java.util.HashMap<>();
            data.put("questions", competitionService.start(userId, id));
            return R.success(data);
        } catch (Exception e) {
            log.error("竞赛开赛失败", e);
            return R.error(e.getMessage());
        }
    }

    /** 试卷回顾：每题的我的作答、参考答案与解析（交卷后可用），返回 {list:[...]} */
    @GetMapping("/{id}/review")
    public R<Map<String, Object>> review(@RequestHeader("X-User-Id") Long userId,
                                         @PathVariable Long id) {
        try {
            Map<String, Object> data = new java.util.HashMap<>();
            data.put("list", competitionService.review(userId, id));
            return R.success(data);
        } catch (Exception e) {
            log.error("获取试卷回顾失败", e);
            return R.error(e.getMessage());
        }
    }

    /** 交卷判分：body {answers:[{questionId,answer}]} */
    @PostMapping("/{id}/submit")
    public R<Map<String, Object>> submit(@RequestHeader("X-User-Id") Long userId,
                                         @PathVariable Long id,
                                         @RequestBody Map<String, Object> body) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> answers = (List<Map<String, Object>>) body.get("answers");
            return R.success(competitionService.submit(userId, id, answers));
        } catch (Exception e) {
            log.error("竞赛交卷失败", e);
            return R.error(e.getMessage());
        }
    }

    /** 排行榜前50（已交卷者，成绩脱敏） */
    @GetMapping("/{id}/rank")
    public R<List<Map<String, Object>>> rank(@PathVariable Long id) {
        try {
            return R.success(competitionService.rank(id));
        } catch (Exception e) {
            log.error("获取竞赛排行榜失败", e);
            return R.error(e.getMessage());
        }
    }

    /** 我的竞赛记录（已交卷，含竞赛标题/得分/排名） */
    @GetMapping("/my/list")
    public R<Map<String, Object>> myList(@RequestHeader("X-User-Id") Long userId,
                                         @RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            return R.success(competitionService.myList(userId, pageNum, pageSize));
        } catch (Exception e) {
            log.error("获取我的竞赛记录失败", e);
            return R.error("获取我的竞赛记录失败");
        }
    }
}
