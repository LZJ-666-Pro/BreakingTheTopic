package com.poti.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.poti.user.entity.Discussion;
import com.poti.user.entity.DiscussionLike;
import com.poti.user.mapper.DiscussionLikeMapper;
import com.poti.user.mapper.DiscussionMapper;
import com.poti.user.service.DiscussionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DiscussionServiceImpl extends ServiceImpl<DiscussionMapper, Discussion> implements DiscussionService {

    @Autowired
    private DiscussionLikeMapper discussionLikeMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public List<Discussion> getDiscussionsByQuestionId(Long questionId) {
        LambdaQueryWrapper<Discussion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Discussion::getQuestionId, questionId)
               .eq(Discussion::getStatus, 1)
               .orderByDesc(Discussion::getCreateTime);
        
        return this.list(wrapper);
    }

    @Override
    public void addDiscussion(Long userId, Long questionId, String content, String userName, String avatar) {
        Discussion discussion = new Discussion();
        discussion.setUserId(userId);
        discussion.setQuestionId(questionId);
        discussion.setContent(content);
        discussion.setUserName(userName);
        discussion.setAvatar(avatar);
        discussion.setLikeCount(0);
        discussion.setStatus(1);
        
        this.save(discussion);
    }

    @Override
    public void toggleLike(Long userId, Long discussionId) {
        log.info("toggleLike开始，userId={}, discussionId={}", userId, discussionId);
        
        Discussion discussion = this.getById(discussionId);
        if (discussion == null) {
            log.warn("讨论不存在，discussionId={}", discussionId);
            return;
        }
        
        DiscussionLike existingLike = discussionLikeMapper.selectByDiscussionAndUser(discussionId, userId);
        log.info("查询到的点赞记录: {}", existingLike);
        
        if (existingLike != null) {
            Integer deleted = existingLike.getDeleted();
            log.info("点赞记录存在，id={}, deleted={}", existingLike.getId(), deleted);
            
            if (deleted == null || deleted == 0) {
                int updateResult = discussionLikeMapper.updateDeleted(existingLike.getId(), 1);
                log.info("取消点赞，updateDeleted返回值={}", updateResult);
                
                discussion.setLikeCount(Math.max(0, discussion.getLikeCount() - 1));
            } else {
                int updateResult = discussionLikeMapper.updateDeleted(existingLike.getId(), 0);
                log.info("恢复点赞，updateDeleted返回值={}", updateResult);
                
                discussion.setLikeCount(discussion.getLikeCount() + 1);
            }
        } else {
            DiscussionLike like = new DiscussionLike();
            like.setUserId(userId);
            like.setDiscussionId(discussionId);
            like.setDeleted(0);
            int insertResult = discussionLikeMapper.insert(like);
            log.info("新增点赞记录，insert返回值={}", insertResult);
            
            discussion.setLikeCount(discussion.getLikeCount() + 1);
        }
        
        this.updateById(discussion);
        log.info("toggleLike结束，discussion.likeCount={}", discussion.getLikeCount());
    }

    @Override
    public boolean hasUserLiked(Long userId, Long discussionId) {
        DiscussionLike like = discussionLikeMapper.selectByDiscussionAndUser(discussionId, userId);
        return like != null && (like.getDeleted() == null || like.getDeleted() == 0);
    }

    @Override
    public Map<Long, Boolean> getUserLikeStatus(Long userId, List<Long> discussionIds) {
        Map<Long, Boolean> result = new HashMap<>();
        
        if (discussionIds == null || discussionIds.isEmpty()) {
            return result;
        }
        
        for (Long discussionId : discussionIds) {
            DiscussionLike like = discussionLikeMapper.selectByDiscussionAndUser(discussionId, userId);
            result.put(discussionId, like != null && (like.getDeleted() == null || like.getDeleted() == 0));
        }
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getMyAnswers(Long userId) {
        LambdaQueryWrapper<Discussion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Discussion::getUserId, userId)
               .eq(Discussion::getStatus, 1)
               .orderByDesc(Discussion::getCreateTime);
        
        List<Discussion> discussions = this.list(wrapper);
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (Discussion discussion : discussions) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", discussion.getId());
            item.put("questionId", discussion.getQuestionId());
            item.put("content", discussion.getContent());
            item.put("likeCount", discussion.getLikeCount());
            item.put("createTime", discussion.getCreateTime());
            
            try {
                String url = "http://localhost:8300/question/internal/" + discussion.getQuestionId();
                @SuppressWarnings("unchecked")
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                
                if (response != null && response.get("data") != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> questionData = (Map<String, Object>) response.get("data");
                    item.put("questionTitle", questionData.get("title"));
                    item.put("questionType", questionData.get("type"));
                }
            } catch (Exception e) {
                log.error("获取题目信息失败，questionId={}", discussion.getQuestionId(), e);
            }
            
            result.add(item);
        }
        
        return result;
    }
}
