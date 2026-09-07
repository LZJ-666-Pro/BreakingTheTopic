package com.poti.user.controller;

import com.poti.common.utils.Result;
import com.poti.user.dto.AchievementDTO;
import com.poti.user.service.AchievementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/achievement")
public class AchievementController {
    
    @Autowired
    private AchievementService achievementService;
    
    @GetMapping
    public Result<AchievementDTO> getUserAchievements(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(defaultValue = "all") String category) {
        
        Long userId = headerUserId != null ? headerUserId : 1L;
        return achievementService.getUserAchievements(userId, category);
    }
    
    @PostMapping("/check")
    public Result<Void> checkAndUnlockAchievements(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam String eventType,
            @RequestParam Integer value) {
        
        Long userId = headerUserId != null ? headerUserId : 1L;
        return achievementService.checkAndUnlockAchievements(userId, eventType, value);
    }
    
    @PostMapping("/init")
    public Result<Void> initUserAchievements(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        Long userId = headerUserId != null ? headerUserId : 1L;
        return achievementService.initUserAchievements(userId);
    }
    
    @PostMapping("/sync")
    public Result<Void> syncUserProgress(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        Long userId = headerUserId != null ? headerUserId : 1L;
        return achievementService.syncUserProgress(userId);
    }
}
