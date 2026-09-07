package com.poti.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.poti.common.R;
import com.poti.common.utils.Result;
import com.poti.user.dto.AchievementDTO;
import com.poti.user.entity.Achievement;
import com.poti.user.entity.UserAchievement;
import com.poti.user.feign.FavoriteFeignClient;
import com.poti.user.feign.PracticeFeignClient;
import com.poti.user.mapper.AchievementMapper;
import com.poti.user.mapper.UserAchievementMapper;
import com.poti.user.mapper.UserStatsMapper;
import com.poti.user.entity.UserStats;
import com.poti.user.service.AchievementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AchievementServiceImpl implements AchievementService {
    
    @Autowired
    private AchievementMapper achievementMapper;
    
    @Autowired
    private UserAchievementMapper userAchievementMapper;
    
    @Autowired
    private PracticeFeignClient practiceFeignClient;
    
    @Autowired
    private FavoriteFeignClient favoriteFeignClient;
    
    @Autowired
    private UserStatsMapper userStatsMapper;
    
    @Override
    public Result<AchievementDTO> getUserAchievements(Long userId, String category) {
        try {
            List<Map<String, Object>> userAchievements = userAchievementMapper.getUserAchievements(userId);
            
            log.info("获取用户成就: userId={}, 成就数量={}", userId, userAchievements != null ? userAchievements.size() : 0);
            
            if (userAchievements == null || userAchievements.isEmpty()) {
                initUserAchievements(userId);
                userAchievements = userAchievementMapper.getUserAchievements(userId);
            }
            
            if (userAchievements != null && !userAchievements.isEmpty()) {
                Map<String, Object> firstAchievement = userAchievements.get(0);
                log.info("第一个成就数据: {}", firstAchievement);
            }
            
            Map<String, Object> summaryData = userAchievementMapper.getAchievementSummary(userId);
            AchievementDTO.AchievementSummaryDTO summary = new AchievementDTO.AchievementSummaryDTO();
            
            if (summaryData != null) {
                summary.setTotal(((Number) summaryData.get("total")).intValue());
                summary.setAchieved(((Number) summaryData.get("achieved")).intValue());
                summary.setPoints(((Number) summaryData.get("points")).intValue());
            } else {
                summary.setTotal(0);
                summary.setAchieved(0);
                summary.setPoints(0);
            }
            
            List<AchievementDTO.AchievementItemDTO> achievements = userAchievements.stream()
                    .filter(data -> "all".equals(category) || category.equals(data.get("category")))
                    .map(this::convertToAchievementItem)
                    .collect(Collectors.toList());
            
            AchievementDTO achievementDTO = new AchievementDTO();
            achievementDTO.setSummary(summary);
            achievementDTO.setAchievements(achievements);
            
            return Result.success(achievementDTO);
        } catch (Exception e) {
            log.error("获取成就列表失败", e);
            return Result.error("获取成就列表失败：" + e.getMessage());
        }
    }
    
    @Override
    public Result<Void> checkAndUnlockAchievements(Long userId, String eventType, Integer value) {
        try {
            LambdaQueryWrapper<Achievement> achievementWrapper = new LambdaQueryWrapper<>();
            achievementWrapper.eq(Achievement::getType, eventType)
                    .eq(Achievement::getDeleted, 0);
            List<Achievement> achievements = achievementMapper.selectList(achievementWrapper);
            
            for (Achievement achievement : achievements) {
                if (value >= achievement.getConditionValue()) {
                    LambdaQueryWrapper<UserAchievement> uaWrapper = new LambdaQueryWrapper<>();
                    uaWrapper.eq(UserAchievement::getUserId, userId)
                            .eq(UserAchievement::getAchievementId, achievement.getId());
                    UserAchievement userAchievement = userAchievementMapper.selectOne(uaWrapper);
                    
                    if (userAchievement != null && userAchievement.getIsUnlocked() == 0) {
                        userAchievement.setIsUnlocked(1);
                        userAchievement.setUnlockTime(LocalDateTime.now());
                        userAchievement.setCurrentProgress(value);
                        userAchievementMapper.updateById(userAchievement);
                    }
                }
            }
            
            return Result.success();
        } catch (Exception e) {
            return Result.error("检查成就失败：" + e.getMessage());
        }
    }
    
    @Override
    public Result<Void> initUserAchievements(Long userId) {
        try {
            LambdaQueryWrapper<UserAchievement> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.eq(UserAchievement::getUserId, userId);
            long count = userAchievementMapper.selectCount(checkWrapper);
            
            if (count > 0) {
                return Result.success();
            }
            
            LambdaQueryWrapper<Achievement> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Achievement::getDeleted, 0)
                    .orderByAsc(Achievement::getSortOrder);
            List<Achievement> achievements = achievementMapper.selectList(wrapper);
            
            for (Achievement achievement : achievements) {
                UserAchievement userAchievement = new UserAchievement();
                userAchievement.setUserId(userId);
                userAchievement.setAchievementId(achievement.getId());
                userAchievement.setCurrentProgress(0);
                userAchievement.setIsUnlocked(0);
                userAchievementMapper.insert(userAchievement);
            }
            
            return Result.success();
        } catch (Exception e) {
            return Result.error("初始化成就失败：" + e.getMessage());
        }
    }
    
    private AchievementDTO.AchievementItemDTO convertToAchievementItem(Map<String, Object> data) {
        AchievementDTO.AchievementItemDTO item = new AchievementDTO.AchievementItemDTO();
        item.setId(((Number) data.get("achievement_id")).longValue());
        item.setName((String) data.get("name"));
        item.setIcon((String) data.get("icon"));
        item.setDescription((String) data.get("description"));
        item.setCategory((String) data.get("category"));
        item.setType((String) data.get("type"));
        item.setConditionValue(((Number) data.get("condition_value")).intValue());
        item.setPoints(((Number) data.get("reward_points")).intValue());
        item.setAchieved(((Number) data.get("is_unlocked")).intValue() == 1);
        
        AchievementDTO.ProgressDTO progress = new AchievementDTO.ProgressDTO();
        Object currentProgressObj = data.get("current_progress");
        int currentProgress = 0;
        if (currentProgressObj instanceof Number) {
            currentProgress = ((Number) currentProgressObj).intValue();
        }
        progress.setCurrent(currentProgress);
        progress.setTotal(((Number) data.get("condition_value")).intValue());
        
        log.debug("成就 {} 进度: {}/{}", item.getName(), currentProgress, progress.getTotal());
        
        if (progress.getTotal() > 0) {
            progress.setPercent(Math.min(100, progress.getCurrent() * 100 / progress.getTotal()));
        } else {
            progress.setPercent(0);
        }
        
        item.setProgress(progress);
        
        if (!item.getAchieved() && progress.getTotal() > 0) {
            int remaining = progress.getTotal() - progress.getCurrent();
            if (remaining > 0) {
                String hint = "";
                switch (item.getType()) {
                    case "TOTAL_ANSWER":
                        hint = "还差" + remaining + "道题即可解锁";
                        break;
                    case "CONSECUTIVE_CORRECT":
                        hint = "还差" + remaining + "道连续正确即可解锁";
                        break;
                    case "TOTAL_FAVORITE":
                        hint = "还差" + remaining + "道收藏即可解锁";
                        break;
                    case "CONSECUTIVE_CHECKIN":
                        hint = "还差" + remaining + "天连续签到即可解锁";
                        break;
                    case "ALL_CATEGORIES":
                        hint = "还差" + remaining + "个分类即可解锁";
                        break;
                    default:
                        hint = "继续努力即可解锁";
                }
                item.setHint(hint);
            }
        }
        
        return item;
    }
    
    @Override
    public Result<Void> syncUserProgress(Long userId) {
        try {
            initUserAchievements(userId);
            
            int totalAnswer = 0;
            int totalFavorite = 0;
            int consecutiveCheckin = 0;
            
            try {
                R<Map<String, Object>> statsResult = practiceFeignClient.getStatistics(userId);
                if (statsResult != null && statsResult.getCode() == 200 && statsResult.getData() != null) {
                    Map<String, Object> stats = statsResult.getData();
                    totalAnswer = getIntValue(stats, "totalQuestionCount", 0);
                    log.info("用户 {} 刷题数量: {}", userId, totalAnswer);
                }
            } catch (Exception e) {
                log.warn("获取练习统计数据失败: {}", e.getMessage());
            }
            
            try {
                R<Integer> favoriteResult = favoriteFeignClient.getFavoriteCount(userId);
                if (favoriteResult != null && favoriteResult.getCode() == 200 && favoriteResult.getData() != null) {
                    totalFavorite = favoriteResult.getData();
                    log.info("用户 {} 收藏数量: {}", userId, totalFavorite);
                }
            } catch (Exception e) {
                log.warn("获取收藏数据失败: {}", e.getMessage());
            }
            
            try {
                UserStats userStats = userStatsMapper.selectByUserId(userId);
                if (userStats != null) {
                    consecutiveCheckin = userStats.getConsecutiveDays() != null ? userStats.getConsecutiveDays() : 0;
                    log.info("用户 {} 连续签到天数: {}", userId, consecutiveCheckin);
                }
            } catch (Exception e) {
                log.warn("获取签到数据失败: {}", e.getMessage());
            }
            
            updateAchievementProgress(userId, "TOTAL_ANSWER", totalAnswer);
            updateAchievementProgress(userId, "TOTAL_FAVORITE", totalFavorite);
            updateAchievementProgress(userId, "CONSECUTIVE_CHECKIN", consecutiveCheckin);
            
            return Result.success();
        } catch (Exception e) {
            log.error("同步用户成就进度失败", e);
            return Result.error("同步失败：" + e.getMessage());
        }
    }
    
    private void updateAchievementProgress(Long userId, String eventType, int value) {
        LambdaQueryWrapper<Achievement> achievementWrapper = new LambdaQueryWrapper<>();
        achievementWrapper.eq(Achievement::getType, eventType)
                .eq(Achievement::getDeleted, 0);
        List<Achievement> achievements = achievementMapper.selectList(achievementWrapper);
        
        log.info("更新成就进度: userId={}, type={}, value={}, 找到{}个成就", userId, eventType, value, achievements.size());
        
        for (Achievement achievement : achievements) {
            LambdaQueryWrapper<UserAchievement> uaWrapper = new LambdaQueryWrapper<>();
            uaWrapper.eq(UserAchievement::getUserId, userId)
                    .eq(UserAchievement::getAchievementId, achievement.getId());
            UserAchievement userAchievement = userAchievementMapper.selectOne(uaWrapper);
            
            if (userAchievement != null) {
                userAchievement.setCurrentProgress(value);
                
                if (value >= achievement.getConditionValue() && userAchievement.getIsUnlocked() == 0) {
                    userAchievement.setIsUnlocked(1);
                    userAchievement.setUnlockTime(LocalDateTime.now());
                    log.info("解锁成就: userId={}, achievement={}, progress={}/{}", 
                            userId, achievement.getName(), value, achievement.getConditionValue());
                }
                
                userAchievementMapper.updateById(userAchievement);
            }
        }
    }
    
    private int getIntValue(Map<String, Object> map, String key, int defaultValue) {
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }
}
