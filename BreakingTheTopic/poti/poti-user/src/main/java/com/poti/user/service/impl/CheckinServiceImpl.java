package com.poti.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.poti.user.entity.CheckIn;
import com.poti.user.entity.CheckinRewardLog;
import com.poti.user.entity.UserStats;
import com.poti.user.mapper.AdminConfigMapper;
import com.poti.user.mapper.CheckInMapper;
import com.poti.user.mapper.CheckinRewardLogMapper;
import com.poti.user.mapper.UserStatsMapper;
import com.poti.user.service.CheckinService;
import com.poti.user.service.VipService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CheckinServiceImpl extends ServiceImpl<CheckInMapper, CheckIn> implements CheckinService {

    /** 连签奖励总开关（后台 sys_config，实时读取；缺失视为开启） */
    private static final String ENABLED_KEY = "checkin_reward_enabled";

    /** 连签里程碑档位与对应奖励天数配置键（默认 3 天送 1 天 / 7 送 3 / 15 送 7 / 30 送 15） */
    private static final int[] REWARD_MILESTONES = {3, 7, 15, 30};
    private static final String[] REWARD_DAY_KEYS = {
            "checkin_reward_days_3", "checkin_reward_days_7", "checkin_reward_days_15", "checkin_reward_days_30"};
    private static final int[] DEFAULT_REWARD_DAYS = {1, 3, 7, 15};

    @Autowired
    private UserStatsMapper userStatsMapper;

    @Autowired
    private AdminConfigMapper adminConfigMapper;

    @Autowired
    private CheckinRewardLogMapper checkinRewardLogMapper;

    @Autowired
    private VipService vipService;

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

        // 单次查询近 365 天签到日期后内存判连续（替代逐日 count 循环查询，签到高峰期减少数百次 DB 往返）
        LambdaQueryWrapper<CheckIn> rangeQuery = new LambdaQueryWrapper<>();
        rangeQuery.eq(CheckIn::getUserId, userId)
                  .ge(CheckIn::getCheckinDate, today.minusDays(364))
                  .select(CheckIn::getCheckinDate);
        Set<LocalDate> dates = this.list(rangeQuery).stream()
                .map(CheckIn::getCheckinDate)
                .collect(Collectors.toSet());

        // 今天已有记录从今天往回数；否则从昨天往回数（今天的签到本次落库后计入，断签即从这里断开）
        LocalDate cursor = dates.contains(today) ? today : today.minusDays(1);
        int streak = 0;
        while (dates.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        if (!dates.contains(today)) {
            streak++;
        }

        return streak;
    }

    /**
     * 连签里程碑奖励发放。
     * 事务说明：本方法读取后台跨库配置（@DS admin），若放入 doCheckIn 的 @Transactional 内，
     * 事务连接会固定在主数据源导致 @DS 切换失效（SQL 打到 poti_user 报表不存在），
     * 因此由 Controller 在 doCheckIn 事务提交后单独调用（方法自身无事务，单条语句各自提交）。
     */
    @Override
    public Integer grantMilestoneReward(Long userId, int streakDays) {
        try {
            if (!adminConfigMapper.isEnabled(ENABLED_KEY)) {
                return null;
            }
            int idx = milestoneIndex(streakDays);
            if (idx < 0) {
                return null;
            }
            int rewardDays = readRewardDays(idx);
            if (rewardDays <= 0) {
                return null;
            }
            // 本轮连签起点：断签归零后开启新一轮，cycle_start_date 变化即可再次获得同档奖励；
            // 唯一键 uk_user_streak_cycle 保证同轮连签同一档位只发一次（含并发重复签到兜底）
            LocalDate cycleStart = LocalDate.now().minusDays(streakDays - 1L);
            LambdaQueryWrapper<CheckinRewardLog> granted = new LambdaQueryWrapper<>();
            granted.eq(CheckinRewardLog::getUserId, userId)
                    .eq(CheckinRewardLog::getStreakDays, streakDays)
                    .eq(CheckinRewardLog::getCycleStartDate, cycleStart);
            if (checkinRewardLogMapper.selectCount(granted) > 0) {
                return null;
            }
            CheckinRewardLog rewardLog = new CheckinRewardLog();
            rewardLog.setUserId(userId);
            rewardLog.setStreakDays(streakDays);
            rewardLog.setRewardDays(rewardDays);
            rewardLog.setCycleStartDate(cycleStart);
            checkinRewardLogMapper.insert(rewardLog);
            vipService.extendMembership(userId, rewardDays);
            log.info("签到奖励发放成功: userId={}, 连签{}天送{}天会员, 本轮起点{}", userId, streakDays, rewardDays, cycleStart);
            return rewardDays;
        } catch (Exception e) {
            // 奖励失败不影响签到结果，仅记录日志便于人工补偿
            log.error("签到奖励发放失败: userId={}, streakDays={}", userId, streakDays, e);
            return null;
        }
    }

    @Override
    public Map<String, Object> getRewardHint(int streakDays) {
        try {
            if (!adminConfigMapper.isEnabled(ENABLED_KEY)) {
                return null;
            }
            int idx = nextMilestoneIndex(streakDays);
            if (idx < 0) {
                return null;
            }
            int rewardDays = readRewardDays(idx);
            if (rewardDays <= 0) {
                return null;
            }
            Map<String, Object> hint = new HashMap<>();
            hint.put("nextStreakDays", REWARD_MILESTONES[idx]);
            hint.put("remainDays", REWARD_MILESTONES[idx] - streakDays);
            hint.put("rewardDays", rewardDays);
            return hint;
        } catch (Exception e) {
            // 提示读取失败不展示即可，不影响签到状态
            log.warn("读取签到奖励提示失败", e);
            return null;
        }
    }

    /** 当前连签天数命中的里程碑下标（精确匹配），未命中返回 -1 */
    private int milestoneIndex(int streakDays) {
        for (int i = 0; i < REWARD_MILESTONES.length; i++) {
            if (REWARD_MILESTONES[i] == streakDays) {
                return i;
            }
        }
        return -1;
    }

    /** 下一个未达成里程碑下标（严格大于当前天数），全部达成返回 -1 */
    private int nextMilestoneIndex(int streakDays) {
        for (int i = 0; i < REWARD_MILESTONES.length; i++) {
            if (REWARD_MILESTONES[i] > streakDays) {
                return i;
            }
        }
        return -1;
    }

    /** 读取档位奖励天数（sys_config 实时读取，后台改完即生效；缺失/非法按默认档位） */
    private int readRewardDays(int idx) {
        String value = adminConfigMapper.selectValueByKey(REWARD_DAY_KEYS[idx]);
        if (value != null && !value.trim().isEmpty()) {
            try {
                int days = Integer.parseInt(value.trim());
                if (days >= 0) {
                    return days;
                }
                log.warn("{} 配置为负数，按默认奖励处理: {}", REWARD_DAY_KEYS[idx], value);
            } catch (NumberFormatException e) {
                log.warn("{} 配置非法，按默认奖励处理: {}", REWARD_DAY_KEYS[idx], value);
            }
        }
        return DEFAULT_REWARD_DAYS[idx];
    }
}
