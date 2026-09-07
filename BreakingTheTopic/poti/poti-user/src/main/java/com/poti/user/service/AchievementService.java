package com.poti.user.service;

import com.poti.common.utils.Result;
import com.poti.user.dto.AchievementDTO;

public interface AchievementService {
    
    Result<AchievementDTO> getUserAchievements(Long userId, String category);
    
    Result<Void> checkAndUnlockAchievements(Long userId, String eventType, Integer value);
    
    Result<Void> initUserAchievements(Long userId);
    
    Result<Void> syncUserProgress(Long userId);
}
