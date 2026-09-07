package com.poti.interview.controller;

import com.poti.common.R;
import com.poti.interview.service.InterviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/interview")
public class InterviewController {

    @Autowired
    private InterviewService interviewService;

    @GetMapping("/types")
    public R<List<Map<String, Object>>> getInterviewTypes() {
        try {
            List<Map<String, Object>> types = interviewService.getInterviewTypes();
            return R.success(types);
        } catch (Exception e) {
            log.error("获取面试类型失败", e);
            return R.error("获取面试类型失败");
        }
    }

    @GetMapping("/start")
    public R<Map<String, Object>> startInterview(@RequestHeader("X-User-Id") Long userId,
                                                  @RequestParam String type) {
        try {
            Map<String, Object> result = interviewService.startInterview(userId, type);
            return R.success(result);
        } catch (Exception e) {
            log.error("开始面试失败", e);
            return R.error("开始面试失败");
        }
    }

    @PostMapping("/answer")
    public R<Map<String, Object>> submitAnswer(@RequestHeader("X-User-Id") Long userId,
                                                @RequestBody Map<String, Object> request) {
        try {
            if (request.get("interviewId") == null || request.get("questionId") == null) {
                return R.error("参数错误");
            }
            Long interviewId = Long.valueOf(request.get("interviewId").toString());
            Long questionId = Long.valueOf(request.get("questionId").toString());
            String userAnswer = (String) request.get("userAnswer");
            String audioUrl = (String) request.get("audioUrl");
            Integer answerTimeSeconds = request.get("answerTimeSeconds") != null ?
                Integer.valueOf(request.get("answerTimeSeconds").toString()) : 0;

            Map<String, Object> result = interviewService.submitAnswer(userId, interviewId, questionId, 
                                                                        userAnswer, audioUrl, answerTimeSeconds);
            
            if (Boolean.FALSE.equals(result.get("success"))) {
                return R.error((String) result.get("message"));
            }
            
            return R.success(result);
        } catch (Exception e) {
            log.error("提交答案失败", e);
            return R.error("提交答案失败");
        }
    }

    @PostMapping("/finish")
    public R<Map<String, Object>> finishInterview(@RequestHeader("X-User-Id") Long userId,
                                                   @RequestBody Map<String, Object> request) {
        try {
            Long interviewId = Long.valueOf(request.get("interviewId").toString());
            Integer spendSeconds = request.get("spendSeconds") != null ?
                Integer.valueOf(request.get("spendSeconds").toString()) : 0;

            Map<String, Object> result = interviewService.finishInterview(userId, interviewId, spendSeconds);
            
            if (Boolean.FALSE.equals(result.get("success"))) {
                return R.error((String) result.get("message"));
            }
            
            return R.success(result);
        } catch (Exception e) {
            log.error("结束面试失败", e);
            return R.error("结束面试失败");
        }
    }

    @PostMapping("/submit")
    public R<Map<String, Object>> submitInterview(@RequestHeader("X-User-Id") Long userId,
                                                   @RequestBody Map<String, Object> request) {
        try {
            String type = (String) request.get("type");
            List<Map<String, Object>> answers = (List<Map<String, Object>>) request.get("answers");
            Integer spendSeconds = request.get("spendSeconds") != null ? 
                Integer.valueOf(request.get("spendSeconds").toString()) : 0;

            Map<String, Object> result = interviewService.submitInterview(userId, type, answers, spendSeconds);
            return R.success(result);
        } catch (Exception e) {
            log.error("提交面试失败", e);
            return R.error("提交面试失败");
        }
    }

    @GetMapping("/question")
    public R<Map<String, Object>> getCurrentQuestion(@RequestHeader("X-User-Id") Long userId,
                                                      @RequestParam Long interviewId,
                                                      @RequestParam Integer orderNum) {
        try {
            Map<String, Object> result = interviewService.getCurrentQuestion(userId, interviewId, orderNum);
            
            if (Boolean.FALSE.equals(result.get("found"))) {
                return R.error("题目不存在");
            }
            
            return R.success(result);
        } catch (Exception e) {
            log.error("获取题目失败", e);
            return R.error("获取题目失败");
        }
    }

    @GetMapping("/history")
    public R<Map<String, Object>> getHistory(@RequestHeader("X-User-Id") Long userId,
                                              @RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            Map<String, Object> history = interviewService.getHistory(userId, pageNum, pageSize);
            return R.success(history);
        } catch (Exception e) {
            log.error("获取面试历史失败", e);
            return R.error("获取面试历史失败");
        }
    }

    @GetMapping("/statistics")
    public R<Map<String, Object>> getStatistics(@RequestHeader("X-User-Id") Long userId) {
        try {
            Map<String, Object> statistics = interviewService.getStatistics(userId);
            return R.success(statistics);
        } catch (Exception e) {
            log.error("获取面试统计失败", e);
            return R.error("获取面试统计失败");
        }
    }

    @GetMapping("/detail/{interviewId}")
    public R<Map<String, Object>> getInterviewDetail(@RequestHeader("X-User-Id") Long userId,
                                                       @PathVariable Long interviewId) {
        try {
            Map<String, Object> detail = interviewService.getInterviewDetail(userId, interviewId);
            
            if (Boolean.FALSE.equals(detail.get("found"))) {
                return R.error("面试记录不存在");
            }
            
            return R.success(detail);
        } catch (Exception e) {
            log.error("获取面试详情失败", e);
            return R.error("获取面试详情失败");
        }
    }

    @DeleteMapping("/{interviewId}")
    public R<Void> deleteInterview(@RequestHeader("X-User-Id") Long userId,
                                    @PathVariable Long interviewId) {
        try {
            boolean success = interviewService.deleteInterview(userId, interviewId);
            if (success) {
                return R.success();
            } else {
                return R.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除面试记录失败", e);
            return R.error("删除面试记录失败");
        }
    }

    @DeleteMapping("/clear")
    public R<Void> clearAllInterviews(@RequestHeader("X-User-Id") Long userId) {
        try {
            interviewService.clearAllInterviews(userId);
            return R.success();
        } catch (Exception e) {
            log.error("清空面试记录失败", e);
            return R.error("清空面试记录失败");
        }
    }

    @GetMapping("/wrong")
    public R<List<Map<String, Object>>> getWrongQuestions(@RequestHeader("X-User-Id") Long userId) {
        try {
            List<Map<String, Object>> wrongQuestions = interviewService.getWrongQuestions(userId);
            return R.success(wrongQuestions);
        } catch (Exception e) {
            log.error("获取面试错题失败", e);
            return R.error("获取面试错题失败");
        }
    }

    @DeleteMapping("/wrong/{detailId}")
    public R<Void> removeWrongQuestion(@RequestHeader("X-User-Id") Long userId, @PathVariable Long detailId) {
        try {
            boolean success = interviewService.removeWrongQuestion(userId, detailId);
            if (success) {
                return R.success();
            } else {
                return R.error("移除失败");
            }
        } catch (Exception e) {
            log.error("移除面试错题失败", e);
            return R.error("移除面试错题失败");
        }
    }

    @DeleteMapping("/wrong/clear")
    public R<Void> clearAllWrongQuestions(@RequestHeader("X-User-Id") Long userId) {
        try {
            interviewService.clearAllWrongQuestions(userId);
            return R.success();
        } catch (Exception e) {
            log.error("清空面试错题失败", e);
            return R.error("清空面试错题失败");
        }
    }
}
