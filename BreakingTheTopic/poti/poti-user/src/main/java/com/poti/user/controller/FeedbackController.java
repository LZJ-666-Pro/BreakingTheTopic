package com.poti.user.controller;

import com.poti.common.R;
import com.poti.user.entity.Feedback;
import com.poti.user.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @PostMapping("/submit")
    public R<String> submitFeedback(@RequestBody Feedback feedback,
                                     @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId != null) {
            feedback.setUserId(userId);
        }
        
        if (feedback.getContent() == null || feedback.getContent().trim().isEmpty()) {
            return R.error("反馈内容不能为空");
        }
        
        boolean success = feedbackService.submitFeedback(feedback);
        if (success) {
            return R.success("提交成功，感谢您的反馈！");
        }
        return R.error("提交失败，请稍后重试");
    }

    @GetMapping("/list")
    public R<List<Feedback>> getUserFeedbackList(@RequestHeader("X-User-Id") Long userId) {
        List<Feedback> list = feedbackService.getUserFeedbackList(userId);
        return R.success(list);
    }

    @GetMapping("/detail/{id}")
    public R<Feedback> getFeedbackDetail(@PathVariable Long id) {
        Feedback feedback = feedbackService.getById(id);
        if (feedback == null) {
            return R.error("反馈不存在");
        }
        return R.success(feedback);
    }

    @GetMapping("/page")
    public R<Map<String, Object>> getFeedbackPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status) {
        Map<String, Object> result = feedbackService.getFeedbackPage(pageNum, pageSize, status);
        return R.success(result);
    }

    @PostMapping("/reply/{id}")
    public R<String> replyFeedback(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String reply = body.get("reply");
        if (reply == null || reply.trim().isEmpty()) {
            return R.error("回复内容不能为空");
        }
        
        boolean success = feedbackService.replyFeedback(id, reply);
        if (success) {
            return R.success("回复成功");
        }
        return R.error("回复失败");
    }

    @DeleteMapping("/delete/{id}")
    public R<String> deleteFeedback(@PathVariable Long id,
                                     @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        Feedback feedback = feedbackService.getById(id);
        if (feedback == null) {
            return R.error("反馈不存在");
        }
        
        if (userId != null && userId != 0 && !userId.equals(feedback.getUserId())) {
            return R.error("无权删除此反馈");
        }
        
        boolean success = feedbackService.removeById(id);
        if (success) {
            return R.success("删除成功");
        }
        return R.error("删除失败");
    }
}
