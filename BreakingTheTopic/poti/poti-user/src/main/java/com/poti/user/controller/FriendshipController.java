package com.poti.user.controller;

import com.poti.common.utils.Result;
import com.poti.user.service.FriendshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/friendship")
public class FriendshipController {
    
    @Autowired
    private FriendshipService friendshipService;
    
    @PostMapping("/request")
    public Result<Void> sendFriendRequest(
            @RequestParam Long userId,
            @RequestParam Long friendId,
            @RequestParam(required = false) String message) {
        return friendshipService.sendFriendRequest(userId, friendId, message);
    }
    
    @PostMapping("/accept")
    public Result<Void> acceptFriendRequest(
            @RequestParam Long requestId,
            @RequestParam Long userId) {
        return friendshipService.acceptFriendRequest(requestId, userId);
    }
    
    @PostMapping("/reject")
    public Result<Void> rejectFriendRequest(
            @RequestParam Long requestId,
            @RequestParam Long userId) {
        return friendshipService.rejectFriendRequest(requestId, userId);
    }
    
    @GetMapping("/pending")
    public Result<List<Map<String, Object>>> getPendingRequests(@RequestParam Long userId) {
        return friendshipService.getPendingRequests(userId);
    }
    
    @GetMapping("/sent")
    public Result<List<Map<String, Object>>> getSentRequests(@RequestParam Long userId) {
        return friendshipService.getSentRequests(userId);
    }
    
    @DeleteMapping("/delete")
    public Result<Void> deleteFriend(
            @RequestParam Long userId,
            @RequestParam Long friendId) {
        return friendshipService.deleteFriend(userId, friendId);
    }
    
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> getFriendList(@RequestParam Long userId) {
        return friendshipService.getFriendList(userId);
    }
    
    @GetMapping("/invite-code")
    public Result<String> generateInviteCode(@RequestParam Long userId) {
        return friendshipService.generateInviteCode(userId);
    }
    
    @PostMapping("/add-by-code")
    public Result<Void> addFriendByInviteCode(
            @RequestParam Long userId,
            @RequestParam String inviteCode,
            @RequestParam(required = false) String message) {
        return friendshipService.addFriendByInviteCode(userId, inviteCode, message);
    }
    
    @GetMapping("/recommendations")
    public Result<List<Map<String, Object>>> getFriendRecommendations(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "10") Integer limit) {
        return friendshipService.getFriendRecommendations(userId, limit);
    }
}
