package com.poti.practice.controller;

import com.poti.common.R;
import com.poti.practice.service.PracticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/practice")
public class PracticeController {

    @Autowired
    private PracticeService practiceService;

    @PostMapping("/submit")
    public R<Map<String, Object>> submitAnswer(@RequestHeader("X-User-Id") Long userId,
                                               @RequestBody Map<String, Object> request) {
        try {
            Long questionId = Long.valueOf(request.get("questionId").toString());
            Integer userAnswer = Integer.valueOf(request.get("userAnswer").toString());
            Integer spendSeconds = request.get("spendSeconds") != null ? 
                Integer.valueOf(request.get("spendSeconds").toString()) : 0;

            Map<String, Object> result = practiceService.submitAnswer(userId, questionId, userAnswer, spendSeconds);
            return R.success(result);
        } catch (Exception e) {
            log.error("提交答案失败", e);
            return R.error("提交答案失败");
        }
    }

    @GetMapping("/history")
    public R<Map<String, Object>> getHistory(@RequestHeader("X-User-Id") Long userId,
                                           @RequestParam(defaultValue = "1") Integer pageNum,
                                           @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            Map<String, Object> history = practiceService.getHistory(userId, pageNum, pageSize);
            return R.success(history);
        } catch (Exception e) {
            log.error("获取刷题历史失败", e);
            return R.error("获取刷题历史失败");
        }
    }

    @GetMapping("/internal/statistics")
    public R<Map<String, Object>> getStatisticsInternal(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "userId", required = false) Long paramUserId) {
        try {
            Long userId = headerUserId != null ? headerUserId : paramUserId;
            if (userId == null) {
                return R.error("用户ID不能为空");
            }
            Map<String, Object> statistics = practiceService.getStatistics(userId);
            return R.success(statistics);
        } catch (Exception e) {
            log.error("获取学习统计失败", e);
            return R.error("获取学习统计失败");
        }
    }

    @GetMapping("/internal/calendar")
    public R<Map<String, Object>> getCalendarInternal(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "userId", required = false) Long paramUserId,
            @RequestParam("year") Integer year,
            @RequestParam("month") Integer month) {
        try {
            Long userId = headerUserId != null ? headerUserId : paramUserId;
            if (userId == null) {
                return R.error("用户ID不能为空");
            }
            Map<String, Object> calendar = practiceService.getCalendar(userId, year, month);
            return R.success(calendar);
        } catch (Exception e) {
            log.error("获取刷题日历失败", e);
            return R.error("获取刷题日历失败");
        }
    }

    @GetMapping("/internal/ranking")
    public R<List<Map<String, Object>>> getRanking(
            @RequestParam(defaultValue = "total") String type,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            int offset = page * limit;
            List<Map<String, Object>> ranking = practiceService.getRanking(type, offset, limit);
            return R.success(ranking);
        } catch (Exception e) {
            log.error("获取排行榜失败", e);
            return R.error("获取排行榜失败");
        }
    }

    @GetMapping("/internal/user-rank")
    public R<Map<String, Object>> getUserRank(
            @RequestParam("userId") Long userId,
            @RequestParam(defaultValue = "total") String type) {
        try {
            Map<String, Object> rank = practiceService.getUserRank(userId, type);
            return R.success(rank);
        } catch (Exception e) {
            log.error("获取用户排名失败", e);
            return R.error("获取用户排名失败");
        }
    }

    @GetMapping("/record/{questionId}")
    public R<Map<String, Object>> getRecordByQuestionId(@RequestHeader("X-User-Id") Long userId,
                                                         @PathVariable Long questionId) {
        try {
            Map<String, Object> record = practiceService.getRecordByQuestionId(userId, questionId);
            return R.success(record);
        } catch (Exception e) {
            log.error("获取刷题记录失败", e);
            return R.error("获取刷题记录失败");
        }
    }

    @DeleteMapping("/record/{recordId}")
    public R<Void> deleteRecord(@RequestHeader("X-User-Id") Long userId,
                                 @PathVariable Long recordId) {
        try {
            boolean success = practiceService.deleteRecord(userId, recordId);
            if (success) {
                return R.success();
            } else {
                return R.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除刷题记录失败", e);
            return R.error("删除刷题记录失败");
        }
    }

    @DeleteMapping("/clear")
    public R<Void> clearAllRecords(@RequestHeader("X-User-Id") Long userId) {
        try {
            practiceService.clearAllRecords(userId);
            return R.success();
        } catch (Exception e) {
            log.error("清空刷题记录失败", e);
            return R.error("清空刷题记录失败");
        }
    }
}
