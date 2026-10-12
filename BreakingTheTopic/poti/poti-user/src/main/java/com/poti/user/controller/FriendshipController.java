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

    // 用户身份统一取自鉴权过滤器注入的 X-User-Id（由 JWT 解析而来，无法伪造），
    // 不再信任前端传参，防止越权查看/操作他人好友关系

    @PostMapping("/request")
    public Result<Void> sendFriendRequest(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam Long friendId,
            @RequestParam(required = false) String message) {
        return friendshipService.sendFriendRequest(userId, friendId, message);
    }

    @PostMapping("/accept")
    public Result<Void> acceptFriendRequest(
            @RequestParam Long requestId,
            @RequestHeader("X-User-Id") Long userId) {
        return friendshipService.acceptFriendRequest(requestId, userId);
    }

    @PostMapping("/reject")
    public Result<Void> rejectFriendRequest(
            @RequestParam Long requestId,
            @RequestHeader("X-User-Id") Long userId) {
        return friendshipService.rejectFriendRequest(requestId, userId);
    }

    @GetMapping("/pending")
    public Result<List<Map<String, Object>>> getPendingRequests(@RequestHeader("X-User-Id") Long userId) {
        return friendshipService.getPendingRequests(userId);
    }

    @GetMapping("/sent")
    public Result<List<Map<String, Object>>> getSentRequests(@RequestHeader("X-User-Id") Long userId) {
        return friendshipService.getSentRequests(userId);
    }

    @DeleteMapping("/delete")
    public Result<Void> deleteFriend(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam Long friendId) {
        return friendshipService.deleteFriend(userId, friendId);
    }

    @GetMapping("/list")
    public Result<List<Map<String, Object>>> getFriendList(@RequestHeader("X-User-Id") Long userId) {
        return friendshipService.getFriendList(userId);
    }

    @GetMapping("/invite-code")
    public Result<String> generateInviteCode(@RequestHeader("X-User-Id") Long userId) {
        return friendshipService.generateInviteCode(userId);
    }

    @PostMapping("/add-by-code")
    public Result<Void> addFriendByInviteCode(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam String inviteCode,
            @RequestParam(required = false) String message) {
        return friendshipService.addFriendByInviteCode(userId, inviteCode, message);
    }

    @GetMapping("/recommendations")
    public Result<List<Map<String, Object>>> getFriendRecommendations(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "10") Integer limit) {
        return friendshipService.getFriendRecommendations(userId, limit);
    }
}
