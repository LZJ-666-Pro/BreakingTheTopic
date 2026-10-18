package com.poti.user.controller;

import com.poti.common.R;
import com.poti.user.entity.Feedback;
import com.poti.user.service.FeedbackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
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

    /** internal 通道（供 AI 助手代当前用户提交反馈，身份由服务间令牌信任链传递） */
    @PostMapping("/internal/submit")
    public R<String> submitFeedbackInternal(@RequestParam("userId") Long userId,
                                            @RequestBody Map<String, String> body) {
        try {
            String content = body != null ? body.get("content") : null;
            if (content == null || content.trim().isEmpty()) {
                return R.error("反馈内容不能为空");
            }
            Feedback feedback = new Feedback();
            feedback.setUserId(userId);
            feedback.setType(body.get("type") != null && !body.get("type").isBlank()
                    ? body.get("type") : "其他");
            feedback.setContent(content.trim());
            boolean success = feedbackService.submitFeedback(feedback);
            return success ? R.success("提交成功，感谢您的反馈！") : R.error("提交失败，请稍后重试");
        } catch (Exception e) {
            log.error("internal 提交反馈失败: userId={}", userId, e);
            return R.error("提交失败，请稍后重试");
        }
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

    /** 批量标记已处理（后台管理：status 置 1，可选统一回复话术） */
    @PostMapping("/batch/resolve")
    public R<String> batchResolveFeedback(@RequestBody Map<String, Object> body) {
        Object raw = body.get("ids");
        if (!(raw instanceof List<?> list) || list.isEmpty()) {
            return R.error("请选择要处理的反馈");
        }
        List<Long> ids = new java.util.ArrayList<>();
        for (Object o : list) {
            try {
                ids.add(Long.parseLong(o.toString()));
            } catch (NumberFormatException ignored) {
            }
        }
        if (ids.isEmpty()) {
            return R.error("请选择要处理的反馈");
        }
        String reply = body.get("reply") != null ? body.get("reply").toString() : null;
        int count = feedbackService.batchMarkResolved(ids, reply);
        if (count == 0) {
            return R.error("所选反馈中没有待处理记录");
        }
        return R.success("已处理 " + count + " 条反馈");
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
