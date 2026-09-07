package com.poti.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.poti.user.entity.Feedback;

import java.util.List;
import java.util.Map;

public interface FeedbackService extends IService<Feedback> {

    List<Feedback> getUserFeedbackList(Long userId);

    Map<String, Object> getFeedbackPage(Integer pageNum, Integer pageSize, Integer status);

    boolean submitFeedback(Feedback feedback);

    boolean replyFeedback(Long id, String reply);
}
