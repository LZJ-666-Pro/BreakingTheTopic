package com.poti.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.poti.common.R;
import com.poti.common.utils.Result;
import com.poti.user.entity.FriendRequest;
import com.poti.user.entity.Friendship;
import com.poti.user.entity.User;
import com.poti.user.feign.PracticeFeignClient;
import com.poti.user.mapper.FriendRequestMapper;
import com.poti.user.mapper.FriendshipMapper;
import com.poti.user.mapper.UserMapper;
import com.poti.user.service.FriendshipService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class FriendshipServiceImpl implements FriendshipService {
    
    @Autowired
    private FriendshipMapper friendshipMapper;
    
    @Autowired
    private FriendRequestMapper friendRequestMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PracticeFeignClient practiceFeignClient;
    
    @Override
    @Transactional
    public Result<Void> sendFriendRequest(Long userId, Long friendId, String message) {
        try {
            if (userId.equals(friendId)) {
                return Result.error("不能添加自己为好友");
            }
            
            LambdaQueryWrapper<Friendship> friendshipQuery = new LambdaQueryWrapper<>();
            friendshipQuery.eq(Friendship::getUserId, userId)
                          .eq(Friendship::getFriendId, friendId);
            Friendship existingFriendship = friendshipMapper.selectOne(friendshipQuery);
            if (existingFriendship != null) {
                return Result.error("已经是好友或已发送申请");
            }
            
            LambdaQueryWrapper<FriendRequest> requestQuery = new LambdaQueryWrapper<>();
            requestQuery.eq(FriendRequest::getUserId, userId)
                       .eq(FriendRequest::getFriendId, friendId)
                       .eq(FriendRequest::getStatus, 0);
            FriendRequest existingRequest = friendRequestMapper.selectOne(requestQuery);
            if (existingRequest != null) {
                return Result.error("已发送过申请，请等待对方处理");
            }
            
            FriendRequest request = new FriendRequest();
            request.setUserId(userId);
            request.setFriendId(friendId);
            request.setMessage(message);
            request.setStatus(0);
            friendRequestMapper.insert(request);
            
            return Result.success(null);
        } catch (Exception e) {
            log.error("发送好友申请失败", e);
            return Result.error("发送好友申请失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public Result<Void> acceptFriendRequest(Long requestId, Long userId) {
        try {
            log.info("接受好友申请，requestId: {}, userId: {}", requestId, userId);
            FriendRequest request = friendRequestMapper.selectById(requestId);
            if (request == null) {
                log.warn("申请不存在，requestId: {}", requestId);
                return Result.error("申请不存在");
            }
            
            if (!request.getFriendId().equals(userId)) {
                log.warn("无权处理此申请，request.friendId: {}, userId: {}", request.getFriendId(), userId);
                return Result.error("无权处理此申请");
            }
            
            if (request.getStatus() != 0) {
                log.warn("该申请已处理，request.status: {}", request.getStatus());
                return Result.error("该申请已处理");
            }
            
            request.setStatus(1);
            friendRequestMapper.updateById(request);
            log.info("更新好友申请状态为已同意");
            
            createOrUpdateFriendship(request.getUserId(), request.getFriendId());
            createOrUpdateFriendship(request.getFriendId(), request.getUserId());
            
            log.info("接受好友申请成功");
            return Result.success(null);
        } catch (Exception e) {
            log.error("接受好友申请失败", e);
            return Result.error("接受好友申请失败：" + e.getMessage());
        }
    }
    
    private void createOrUpdateFriendship(Long userId, Long friendId) {
        log.info("创建或更新好友关系: userId={}, friendId={}", userId, friendId);
        
        Friendship existingFriendship = friendshipMapper.selectByUserIdAndFriendId(userId, friendId);
        
        if (existingFriendship != null) {
            log.info("好友关系已存在，id={}, deleted={}", existingFriendship.getId(), existingFriendship.getDeleted());
            if (existingFriendship.getDeleted() == 1) {
                friendshipMapper.restoreFriendship(existingFriendship.getId());
                log.info("恢复已删除的好友关系");
            } else {
                log.info("好友关系已存在且正常，无需操作");
            }
        } else {
            Friendship friendship = new Friendship();
            friendship.setUserId(userId);
            friendship.setFriendId(friendId);
            friendship.setStatus(1);
            friendshipMapper.insert(friendship);
            log.info("创建新的好友关系");
        }
    }
    
    @Override
    @Transactional
    public Result<Void> rejectFriendRequest(Long requestId, Long userId) {
        try {
            FriendRequest request = friendRequestMapper.selectById(requestId);
            if (request == null) {
                return Result.error("申请不存在");
            }
            
            if (!request.getFriendId().equals(userId)) {
                return Result.error("无权处理此申请");
            }
            
            if (request.getStatus() != 0) {
                return Result.error("该申请已处理");
            }
            
            request.setStatus(2);
            friendRequestMapper.updateById(request);
            
            return Result.success(null);
        } catch (Exception e) {
            log.error("拒绝好友申请失败", e);
            return Result.error("拒绝好友申请失败：" + e.getMessage());
        }
    }
    
    @Override
    public Result<List<Map<String, Object>>> getPendingRequests(Long userId) {
        try {
            List<Map<String, Object>> requests = friendRequestMapper.getPendingRequests(userId);
            return Result.success(requests);
        } catch (Exception e) {
            log.error("获取待处理好友申请失败", e);
            return Result.error("获取待处理好友申请失败：" + e.getMessage());
        }
    }
    
    @Override
    public Result<List<Map<String, Object>>> getSentRequests(Long userId) {
        try {
            List<Map<String, Object>> requests = friendRequestMapper.getSentRequests(userId);
            return Result.success(requests);
        } catch (Exception e) {
            log.error("获取已发送好友申请失败", e);
            return Result.error("获取已发送好友申请失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public Result<Void> deleteFriend(Long userId, Long friendId) {
        try {
            LambdaQueryWrapper<Friendship> query1 = new LambdaQueryWrapper<>();
            query1.eq(Friendship::getUserId, userId)
                  .eq(Friendship::getFriendId, friendId);
            friendshipMapper.delete(query1);
            
            LambdaQueryWrapper<Friendship> query2 = new LambdaQueryWrapper<>();
            query2.eq(Friendship::getUserId, friendId)
                  .eq(Friendship::getFriendId, userId);
            friendshipMapper.delete(query2);
            
            return Result.success(null);
        } catch (Exception e) {
            log.error("删除好友失败", e);
            return Result.error("删除好友失败：" + e.getMessage());
        }
    }
    
    @Override
    public Result<List<Map<String, Object>>> getFriendList(Long userId) {
        try {
            log.info("获取好友列表，userId: {}", userId);
            List<Long> friendIds = friendshipMapper.getFriendIds(userId);
            log.info("好友ID列表: {}", friendIds);
            
            if (friendIds.isEmpty()) {
                log.info("好友列表为空");
                return Result.success(List.of());
            }
            
            List<Map<String, Object>> friends = new ArrayList<>();
            
            for (Long friendId : friendIds) {
                try {
                    User user = userMapper.selectById(friendId);
                    if (user == null) {
                        continue;
                    }
                    
                    R<Map<String, Object>> statsResponse = practiceFeignClient.getStatistics(friendId);
                    
                    Map<String, Object> friend = new HashMap<>();
                    friend.put("user_id", friendId);
                    friend.put("nickname", user.getNickname());
                    friend.put("avatar_url", user.getAvatarUrl());
                    
                    if (statsResponse != null && statsResponse.getData() != null) {
                        Map<String, Object> stats = statsResponse.getData();
                        friend.put("total_questions", stats.get("totalQuestionCount") != null ? 
                                ((Number) stats.get("totalQuestionCount")).intValue() : 0);
                        friend.put("today_questions", stats.get("todayCount") != null ? 
                                ((Number) stats.get("todayCount")).intValue() : 0);
                        
                        Integer total = stats.get("totalQuestionCount") != null ? 
                                ((Number) stats.get("totalQuestionCount")).intValue() : 0;
                        Integer correct = stats.get("correctCount") != null ? 
                                ((Number) stats.get("correctCount")).intValue() : 0;
                        friend.put("accuracy", total > 0 ? Math.round(correct * 100.0 / total * 100) / 100.0 : 0);
                    } else {
                        friend.put("total_questions", 0);
                        friend.put("today_questions", 0);
                        friend.put("accuracy", 0);
                    }
                    
                    friends.add(friend);
                } catch (Exception e) {
                    log.error("获取好友 {} 统计数据失败", friendId, e);
                }
            }
            
            friends.sort((a, b) -> {
                Object aVal = a.get("total_questions");
                Object bVal = b.get("total_questions");
                int aNum = aVal != null ? ((Number) aVal).intValue() : 0;
                int bNum = bVal != null ? ((Number) bVal).intValue() : 0;
                return Integer.compare(bNum, aNum);
            });
            
            log.info("查询到的好友数量: {}, 好友列表: {}", friends.size(), friends);
            
            return Result.success(friends);
        } catch (Exception e) {
            log.error("获取好友列表失败", e);
            return Result.error("获取好友列表失败：" + e.getMessage());
        }
    }
    
    @Override
    public Result<String> generateInviteCode(Long userId) {
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            if (user.getUniqueId() != null && !user.getUniqueId().isEmpty()) {
                return Result.success(user.getUniqueId());
            }
            
            String inviteCode = generateUniqueCode();
            userMapper.updateUniqueId(userId, inviteCode);
            log.info("生成邀请码成功: userId={}, inviteCode={}", userId, inviteCode);
            
            return Result.success(inviteCode);
        } catch (Exception e) {
            log.error("生成邀请码失败", e);
            return Result.error("生成邀请码失败：" + e.getMessage());
        }
    }
    
    private String generateUniqueCode() {
        String code = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        User existingUser = userMapper.selectByUniqueId(code);
        if (existingUser != null) {
            return generateUniqueCode();
        }
        return code;
    }
    
    @Override
    @Transactional
    public Result<Void> addFriendByInviteCode(Long userId, String inviteCode, String message) {
        try {
            if (inviteCode == null || inviteCode.trim().isEmpty()) {
                return Result.error("邀请码不能为空");
            }
            
            User targetUser = userMapper.selectByUniqueId(inviteCode.trim());
            if (targetUser == null) {
                return Result.error("邀请码无效");
            }
            
            if (targetUser.getId().equals(userId)) {
                return Result.error("不能添加自己为好友");
            }
            
            return sendFriendRequest(userId, targetUser.getId(), message);
        } catch (Exception e) {
            log.error("通过邀请码添加好友失败", e);
            return Result.error("通过邀请码添加好友失败：" + e.getMessage());
        }
    }
    
    @Override
    public Result<List<Map<String, Object>>> getFriendRecommendations(Long userId, Integer limit) {
        try {
            if (limit == null || limit <= 0) {
                limit = 10;
            }
            
            List<Map<String, Object>> recommendations = friendshipMapper.getFriendRecommendations(userId, limit);
            
            for (Map<String, Object> rec : recommendations) {
                Long recUserId = ((Number) rec.get("user_id")).longValue();
                try {
                    R<Map<String, Object>> statsResponse = practiceFeignClient.getStatistics(recUserId);
                    if (statsResponse != null && statsResponse.getData() != null) {
                        Map<String, Object> stats = statsResponse.getData();
                        rec.put("total_questions", stats.get("totalQuestionCount") != null ? 
                                ((Number) stats.get("totalQuestionCount")).intValue() : 0);
                    } else {
                        rec.put("total_questions", 0);
                    }
                } catch (Exception e) {
                    log.error("获取推荐用户 {} 统计数据失败", recUserId, e);
                    rec.put("total_questions", 0);
                }
            }
            
            return Result.success(recommendations);
        } catch (Exception e) {
            log.error("获取好友推荐失败", e);
            return Result.error("获取好友推荐失败：" + e.getMessage());
        }
    }
}
