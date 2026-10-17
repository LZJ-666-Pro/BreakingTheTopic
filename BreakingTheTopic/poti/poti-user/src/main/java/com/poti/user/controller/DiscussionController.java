package com.poti.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.poti.common.R;
import com.poti.user.entity.Discussion;
import com.poti.user.entity.User;
import com.poti.user.mapper.AdminConfigMapper;
import com.poti.user.service.DiscussionService;
import com.poti.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/discussion")
public class DiscussionController {

    @Autowired
    private DiscussionService discussionService;

    @Autowired
    private UserService userService;

    @Autowired
    private AdminConfigMapper adminConfigMapper;

    @GetMapping("/list/{questionId}")
    public R<Map<String, Object>> getDiscussions(@PathVariable Long questionId,
                                                  @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            // 后台「业务开关-讨论区」关闭时不返回讨论数据（配置缺失视为开启）
            if (!adminConfigMapper.isEnabled("discussion_enabled")) {
                return R.success(Map.of("discussions", List.of(), "disabled", true));
            }

            List<Discussion> discussions = discussionService.getDiscussionsByQuestionId(questionId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("discussions", discussions);
            
            if (userId != null && !discussions.isEmpty()) {
                List<Long> discussionIds = discussions.stream()
                    .map(Discussion::getId)
                    .collect(Collectors.toList());
                Map<Long, Boolean> likeStatus = discussionService.getUserLikeStatus(userId, discussionIds);
                result.put("likeStatus", likeStatus);
            }
            
            return R.success(result);
        } catch (Exception e) {
            log.error("获取讨论列表失败", e);
            return R.error("获取讨论列表失败");
        }
    }

    @PostMapping("/add")
    public R<Void> addDiscussion(@RequestHeader("X-User-Id") Long userId,
                                  @RequestBody Map<String, Object> request) {
        try {
            // 后台「业务开关-讨论区」关闭时禁止发表（配置缺失视为开启）
            if (!adminConfigMapper.isEnabled("discussion_enabled")) {
                return R.error("讨论区已关闭");
            }

            Long questionId = Long.parseLong(request.get("questionId").toString());
            String content = request.get("content").toString();
            
            User user = userService.getById(userId);
            String userName = user != null ? user.getNickname() : "匿名用户";
            String avatar = user != null ? user.getAvatarUrl() : null;
            
            discussionService.addDiscussion(userId, questionId, content, userName, avatar);
            
            return R.success();
        } catch (Exception e) {
            log.error("添加讨论失败", e);
            return R.error("添加讨论失败");
        }
    }

    @PostMapping("/like/{discussionId}")
    public R<Void> toggleLike(@RequestHeader("X-User-Id") Long userId,
                               @PathVariable Long discussionId) {
        try {
            discussionService.toggleLike(userId, discussionId);
            return R.success();
        } catch (Exception e) {
            log.error("点赞失败", e);
            return R.error("点赞失败");
        }
    }

    @DeleteMapping("/{discussionId}")
    public R<Void> deleteDiscussion(@RequestHeader("X-User-Id") Long userId,
                                     @PathVariable Long discussionId) {
        try {
            LambdaQueryWrapper<Discussion> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Discussion::getId, discussionId)
                   .eq(Discussion::getUserId, userId);
            
            discussionService.remove(wrapper);
            return R.success();
        } catch (Exception e) {
            log.error("删除讨论失败", e);
            return R.error("删除讨论失败");
        }
    }

    @GetMapping("/my-answers")
    public R<List<Map<String, Object>>> getMyAnswers(@RequestHeader("X-User-Id") Long userId) {
        try {
            List<Map<String, Object>> answers = discussionService.getMyAnswers(userId);
            return R.success(answers);
        } catch (Exception e) {
            log.error("获取我的回答失败", e);
            return R.error("获取我的回答失败");
        }
    }
}
