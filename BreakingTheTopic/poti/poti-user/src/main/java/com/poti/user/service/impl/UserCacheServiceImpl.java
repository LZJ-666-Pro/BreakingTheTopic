package com.poti.user.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poti.common.redis.service.RedisService;
import com.poti.user.entity.User;
import com.poti.user.service.UserCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserCacheServiceImpl implements UserCacheService {

    @Autowired
    private RedisService redisService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String USER_CACHE_PREFIX = "user:info:";
    private static final String USER_STATS_CACHE_PREFIX = "user:stats:";
    private static final long DEFAULT_CACHE_TIMEOUT = 30;
    private static final TimeUnit DEFAULT_CACHE_UNIT = TimeUnit.MINUTES;

    @Override
    public User getUserFromCache(Long userId) {
        try {
            Object cachedUser = redisService.get(USER_CACHE_PREFIX + userId);
            if (cachedUser != null) {
                return objectMapper.readValue(cachedUser.toString(), User.class);
            }
        } catch (JsonProcessingException e) {
            log.error("从缓存反序列化用户信息失败: userId={}", userId, e);
        }
        return null;
    }

    @Override
    public void cacheUser(User user) {
        cacheUser(user, DEFAULT_CACHE_TIMEOUT, DEFAULT_CACHE_UNIT);
    }

    @Override
    public void cacheUser(User user, long timeout, TimeUnit unit) {
        try {
            String userJson = objectMapper.writeValueAsString(user);
            redisService.set(USER_CACHE_PREFIX + user.getId(), userJson, timeout, unit);
        } catch (JsonProcessingException e) {
            log.error("缓存用户信息失败", e);
        }
    }

    @Override
    public void removeUserCache(Long userId) {
        redisService.delete(USER_CACHE_PREFIX + userId);
    }

    @Override
    public void cacheUserStatistics(Long userId, Map<String, Object> statistics) {
        try {
            String statsJson = objectMapper.writeValueAsString(statistics);
            redisService.set(USER_STATS_CACHE_PREFIX + userId, statsJson, 10, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.error("缓存用户统计数据失败", e);
        }
    }

    @Override
    public Map<String, Object> getUserStatisticsFromCache(Long userId) {
        try {
            Object cachedStats = redisService.get(USER_STATS_CACHE_PREFIX + userId);
            if (cachedStats != null) {
                return objectMapper.readValue(cachedStats.toString(), Map.class);
            }
        } catch (JsonProcessingException e) {
            log.error("读取用户统计数据缓存失败", e);
        }
        return null;
    }

    @Override
    public void removeUserStatisticsCache(Long userId) {
        redisService.delete(USER_STATS_CACHE_PREFIX + userId);
    }
}
