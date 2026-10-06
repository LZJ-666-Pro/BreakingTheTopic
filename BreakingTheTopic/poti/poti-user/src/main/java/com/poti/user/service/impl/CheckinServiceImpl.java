package com.poti.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.poti.user.entity.CheckIn;
import com.poti.user.entity.UserStats;
import com.poti.user.mapper.CheckInMapper;
import com.poti.user.mapper.UserStatsMapper;
import com.poti.user.service.CheckinService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class CheckinServiceImpl extends ServiceImpl<CheckInMapper, CheckIn> implements CheckinService {

    @Autowired
    private UserStatsMapper userStatsMapper;

    @Override
    @Transactional
    public Map<String, Object> doCheckIn(Long userId) {
        LocalDate today = LocalDate.now();
        log.info("用户 {} 开始签到，日期: {}", userId, today);
        
        LambdaQueryWrapper<CheckIn> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CheckIn::getUserId, userId)
                    .eq(CheckIn::getCheckinDate, today);
        
        CheckIn existingCheckIn = this.getOne(queryWrapper);
        
        if (existingCheckIn != null) {
            log.info("用户 {} 今天已经签到，连续签到天数: {}", userId, existingCheckIn.getStreakDays());
            Map<String, Object> result = new HashMap<>();
            result.put("alreadyCheckedIn", true);
            result.put("streakDays", existingCheckIn.getStreakDays());
            return result;
        }
        
        int streakDays = calculateStreakDays(userId);
        log.info("计算出的连续签到天数: {}", streakDays);
        
        CheckIn checkIn = new CheckIn();
        checkIn.setUserId(userId);
        checkIn.setCheckinDate(today);
        checkIn.setStreakDays(streakDays);
        
        boolean saved = this.save(checkIn);
        log.info("签到记录保存结果: {}, 签到天数: {}", saved, checkIn.getStreakDays());
        
        updateUserStatsStreakDays(userId, streakDays);
        
        Map<String, Object> result = new HashMap<>();
        result.put("alreadyCheckedIn", false);
        result.put("streakDays", streakDays);
        log.info("返回签到结果: {}", result);
        return result;
    }
    
    private void updateUserStatsStreakDays(Long userId, int streakDays) {
        try {
            UserStats userStats = userStatsMapper.selectByUserId(userId);
            
            if (userStats == null) {
                userStats = new UserStats();
                userStats.setUserId(userId);
                userStats.setConsecutiveDays(streakDays);
                userStats.setTotalQuestions(0);
                userStats.setCorrectQuestions(0);
                userStats.setWrongQuestions(0);
                userStats.setTodayQuestions(0);
                userStats.setTodayCorrect(0);
                userStats.setTotalTime(0);
                userStats.setTodayTime(0);
                userStats.setTotalPoints(0);
                userStats.setAchievementPoints(0);
                userStatsMapper.insert(userStats);
                log.info("创建用户统计记录，连续签到天数: {}", streakDays);
            } else {
                userStats.setConsecutiveDays(streakDays);
                userStatsMapper.updateById(userStats);
                log.info("更新用户统计记录，连续签到天数: {}", streakDays);
            }
        } catch (Exception e) {
            log.error("更新用户统计连续签到天数失败", e);
        }
    }

    @Override
    public boolean hasCheckedInToday(Long userId) {
        LocalDate today = LocalDate.now();
        
        LambdaQueryWrapper<CheckIn> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CheckIn::getUserId, userId)
                    .eq(CheckIn::getCheckinDate, today);
        
        return this.count(queryWrapper) > 0;
    }

    @Override
    public int getStreakDays(Long userId) {
        LocalDate today = LocalDate.now();
        
        LambdaQueryWrapper<CheckIn> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CheckIn::getUserId, userId)
                    .eq(CheckIn::getCheckinDate, today);
        
        CheckIn todayCheckIn = this.getOne(queryWrapper);
        
        if (todayCheckIn != null) {
            return todayCheckIn.getStreakDays();
        }
        
        LambdaQueryWrapper<CheckIn> lastCheckInQuery = new LambdaQueryWrapper<>();
        lastCheckInQuery.eq(CheckIn::getUserId, userId)
                        .orderByDesc(CheckIn::getCheckinDate)
                        .last("LIMIT 1");
        
        CheckIn lastCheckIn = this.getOne(lastCheckInQuery);
        
        if (lastCheckIn != null) {
            LocalDate lastCheckInDate = lastCheckIn.getCheckinDate();
            long daysSinceLastCheckIn = java.time.temporal.ChronoUnit.DAYS.between(lastCheckInDate, today);
            
            if (daysSinceLastCheckIn > 1) {
                // 断一天即归零：上次签到不是今天或昨天，连续中断
                return 0;
            }
            
            return lastCheckIn.getStreakDays();
        }
        
        return 0;
    }
    
    private int calculateStreakDays(Long userId) {
        LocalDate today = LocalDate.now();
        int streak = 0;
        
        LambdaQueryWrapper<CheckIn> todayQuery = new LambdaQueryWrapper<>();
        todayQuery.eq(CheckIn::getUserId, userId)
                  .eq(CheckIn::getCheckinDate, today);
        
        boolean hasTodayRecord = this.count(todayQuery) > 0;
        
        if (hasTodayRecord) {
            for (int i = 0; i < 365; i++) {
                LocalDate checkDate = today.minusDays(i);
                
                LambdaQueryWrapper<CheckIn> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(CheckIn::getUserId, userId)
                            .eq(CheckIn::getCheckinDate, checkDate);
                
                if (this.count(queryWrapper) > 0) {
                    streak++;
                } else {
                    break;
                }
            }
        } else {
            for (int i = 1; i < 366; i++) {
                LocalDate checkDate = today.minusDays(i);
                
                LambdaQueryWrapper<CheckIn> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(CheckIn::getUserId, userId)
                            .eq(CheckIn::getCheckinDate, checkDate);
                
                if (this.count(queryWrapper) > 0) {
                    streak++;
                } else {
                    break;
                }
            }
            
            streak++;
        }
        
        return streak;
    }
}
