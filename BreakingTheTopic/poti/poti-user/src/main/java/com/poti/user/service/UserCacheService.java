package com.poti.user.service;

import com.poti.user.entity.User;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface UserCacheService {
    
    User getUserFromCache(Long userId);
    
    void cacheUser(User user);
    
    void cacheUser(User user, long timeout, TimeUnit unit);
    
    void removeUserCache(Long userId);
    
    void cacheUserStatistics(Long userId, Map<String, Object> statistics);
    
    Map<String, Object> getUserStatisticsFromCache(Long userId);
    
    void removeUserStatisticsCache(Long userId);
}
