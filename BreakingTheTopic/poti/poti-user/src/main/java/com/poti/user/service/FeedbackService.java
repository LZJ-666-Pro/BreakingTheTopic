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

    /**
     * 批量标记已处理（status 置 1，可附带统一回复话术）
     *
     * @return 实际处理的待处理条数
     */
    int batchMarkResolved(java.util.List<Long> ids, String reply);
}
