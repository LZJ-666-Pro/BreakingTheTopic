package com.poti.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.poti.common.R;
import com.poti.user.entity.User;
import com.poti.user.feign.FavoriteFeignClient;
import com.poti.user.feign.PracticeFeignClient;
import com.poti.user.feign.WrongbookFeignClient;
import com.poti.user.mapper.UserMapper;
import com.poti.user.service.UserCacheService;
import com.poti.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务实现类
 * 提供用户相关的业务逻辑处理，包括用户信息管理、统计数据获取等
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private PracticeFeignClient practiceFeignClient;

    @Autowired
    private FavoriteFeignClient favoriteFeignClient;

    @Autowired
    private WrongbookFeignClient wrongbookFeignClient;

    @Autowired
    private UserCacheService userCacheService;

    /**
     * 根据微信openid查找或创建用户
     * 如果用户不存在，则自动创建新用户并设置默认信息
     * 
     * @param openid 微信用户的唯一标识
     * @return 用户对象
     */
    @Override
    public User findOrCreateByOpenid(String openid) {
        // 查找用户
        User user = baseMapper.selectByOpenid(openid);
        if (user == null) {
            // 用户不存在，创建新用户
            user = new User();
            user.setOpenid(openid);
            user.setNickname("微信用户");
            user.setStatus(1);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            user.setDeleted(0);
            baseMapper.insert(user);
        }
        // 缓存用户信息
        userCacheService.cacheUser(user);
        return user;
    }

    /**
     * 获取用户统计数据
     * 包括总答题数、正确数、错误数、收藏数等
     * 
     * @param userId 用户ID
     * @return 统计数据Map
     */
    @Override
    public Map<String, Object> getStatistics(Long userId) {
        log.info("获取用户统计数据，userId: {}", userId);
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalQuestionCount", 0);
        statistics.put("correctCount", 0);
        statistics.put("wrongCount", 0);
        statistics.put("todayCount", 0);
        statistics.put("todayCorrectCount", 0);
        statistics.put("todayWrongCount", 0);
        statistics.put("favoriteCount", 0);
        statistics.put("wrongbookCount", 0);
        
        try {
            log.info("调用 practiceFeignClient.getStatistics, userId: {}", userId);
            R<Map<String, Object>> practiceStatsResponse = practiceFeignClient.getStatistics(userId);
            log.info("practice 服务响应: {}", practiceStatsResponse);
            if (practiceStatsResponse != null && practiceStatsResponse.getData() != null) {
                Map<String, Object> practiceData = practiceStatsResponse.getData();
                if (practiceData.get("totalQuestionCount") != null) {
                    statistics.put("totalQuestionCount", practiceData.get("totalQuestionCount"));
                }
                if (practiceData.get("correctCount") != null) {
                    statistics.put("correctCount", practiceData.get("correctCount"));
                }
                if (practiceData.get("wrongCount") != null) {
                    statistics.put("wrongCount", practiceData.get("wrongCount"));
                }
                if (practiceData.get("todayCount") != null) {
                    statistics.put("todayCount", practiceData.get("todayCount"));
                }
                if (practiceData.get("todayCorrectCount") != null) {
                    statistics.put("todayCorrectCount", practiceData.get("todayCorrectCount"));
                }
                if (practiceData.get("todayWrongCount") != null) {
                    statistics.put("todayWrongCount", practiceData.get("todayWrongCount"));
                }
                if (practiceData.get("lastPracticeTime") != null) {
                    statistics.put("lastPracticeTime", practiceData.get("lastPracticeTime"));
                }
            }
        } catch (Exception e) {
            log.error("获取 practice 统计数据失败", e);
        }
        
        try {
            log.info("调用 favoriteFeignClient.getFavoriteCount, userId: {}", userId);
            R<Integer> favoriteCountResponse = favoriteFeignClient.getFavoriteCount(userId);
            log.info("favorite 服务响应: {}", favoriteCountResponse);
            if (favoriteCountResponse != null && favoriteCountResponse.getData() != null) {
                statistics.put("favoriteCount", favoriteCountResponse.getData());
            }
        } catch (Exception e) {
            log.error("获取 favorite 统计数据失败", e);
        }
        
        try {
            log.info("调用 wrongbookFeignClient.getWrongbookCount, userId: {}", userId);
            R<Integer> wrongbookCountResponse = wrongbookFeignClient.getWrongbookCount(userId);
            log.info("wrongbook 服务响应: {}", wrongbookCountResponse);
            if (wrongbookCountResponse != null && wrongbookCountResponse.getData() != null) {
                statistics.put("wrongbookCount", wrongbookCountResponse.getData());
            }
        } catch (Exception e) {
            log.error("获取 wrongbook 统计数据失败", e);
        }
        
        log.info("最终统计数据: {}", statistics);
        userCacheService.cacheUserStatistics(userId, statistics);
        return statistics;
    }

    @Override
    public Map<String, Object> getCalendar(Long userId, Integer year, Integer month) {
        R<Map<String, Object>> calendarResponse = practiceFeignClient.getCalendar(userId, year, month);
        return calendarResponse.getData();
    }
}
