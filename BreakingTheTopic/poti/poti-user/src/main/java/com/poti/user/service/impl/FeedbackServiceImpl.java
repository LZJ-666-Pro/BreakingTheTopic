package com.poti.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.poti.user.entity.Feedback;
import com.poti.user.mapper.FeedbackMapper;
import com.poti.user.service.FeedbackService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements FeedbackService {

    @Override
    public List<Feedback> getUserFeedbackList(Long userId) {
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Feedback::getUserId, userId)
               .orderByDesc(Feedback::getCreateTime);
        return list(wrapper);
    }

    @Override
    public Map<String, Object> getFeedbackPage(Integer pageNum, Integer pageSize, Integer status) {
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Feedback::getStatus, status);
        }
        wrapper.orderByDesc(Feedback::getCreateTime);
        
        Page<Feedback> page = new Page<>(pageNum, pageSize);
        page(page, wrapper);
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", page.getTotal());
        result.put("records", page.getRecords());
        return result;
    }

    @Override
    public boolean submitFeedback(Feedback feedback) {
        feedback.setStatus(0);
        return save(feedback);
    }

    @Override
    public boolean replyFeedback(Long id, String reply) {
        Feedback feedback = getById(id);
        if (feedback == null) {
            return false;
        }
        feedback.setReply(reply);
        feedback.setReplyTime(LocalDateTime.now());
        feedback.setStatus(1);
        return updateById(feedback);
    }
}
