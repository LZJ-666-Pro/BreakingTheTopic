package com.poti.user.service;

import com.poti.common.utils.Result;
import java.util.List;
import java.util.Map;

public interface FriendshipService {
    
    Result<Void> sendFriendRequest(Long userId, Long friendId, String message);
    
    Result<Void> acceptFriendRequest(Long requestId, Long userId);
    
    Result<Void> rejectFriendRequest(Long requestId, Long userId);
    
    Result<List<Map<String, Object>>> getPendingRequests(Long userId);
    
    Result<List<Map<String, Object>>> getSentRequests(Long userId);
    
    Result<Void> deleteFriend(Long userId, Long friendId);
    
    Result<List<Map<String, Object>>> getFriendList(Long userId);
    
    Result<String> generateInviteCode(Long userId);
    
    Result<Void> addFriendByInviteCode(Long userId, String inviteCode, String message);
    
    Result<List<Map<String, Object>>> getFriendRecommendations(Long userId, Integer limit);
}
