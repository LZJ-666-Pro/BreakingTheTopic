package com.poti.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.poti.user.entity.Discussion;

import java.util.List;
import java.util.Map;

public interface DiscussionService extends IService<Discussion> {
    
    List<Discussion> getDiscussionsByQuestionId(Long questionId);
    
    void addDiscussion(Long userId, Long questionId, String content, String userName, String avatar);
    
    void toggleLike(Long userId, Long discussionId);
    
    boolean hasUserLiked(Long userId, Long discussionId);
    
    Map<Long, Boolean> getUserLikeStatus(Long userId, List<Long> discussionIds);
    
    List<Map<String, Object>> getMyAnswers(Long userId);
}
