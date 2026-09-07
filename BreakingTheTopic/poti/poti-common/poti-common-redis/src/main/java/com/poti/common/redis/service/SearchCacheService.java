package com.poti.common.redis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class SearchCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String SEARCH_HISTORY_PREFIX = "search:history:";
    private static final String SEARCH_HOT_KEY = "search:hot";
    private static final int MAX_HISTORY_SIZE = 10;
    private static final long HOT_SEARCH_EXPIRE = 7;

    public void addSearchHistory(Long userId, String keyword) {
        String key = SEARCH_HISTORY_PREFIX + userId;
        List<Object> history = redisTemplate.opsForList().range(key, 0, -1);
        
        if (history != null && history.contains(keyword)) {
            redisTemplate.opsForList().remove(key, 1, keyword);
        }
        
        redisTemplate.opsForList().leftPush(key, keyword);
        
        Long size = redisTemplate.opsForList().size(key);
        if (size != null && size > MAX_HISTORY_SIZE) {
            redisTemplate.opsForList().rightPop(key);
        }
        
        redisTemplate.expire(key, 30, TimeUnit.DAYS);
    }

    public List<Object> getSearchHistory(Long userId) {
        String key = SEARCH_HISTORY_PREFIX + userId;
        return redisTemplate.opsForList().range(key, 0, MAX_HISTORY_SIZE - 1);
    }

    public void clearSearchHistory(Long userId) {
        String key = SEARCH_HISTORY_PREFIX + userId;
        redisTemplate.delete(key);
    }

    public void incrementHotSearch(String keyword) {
        redisTemplate.opsForZSet().incrementScore(SEARCH_HOT_KEY, keyword, 1);
    }

    public Set<Object> getHotSearches(int limit) {
        return redisTemplate.opsForZSet().reverseRange(SEARCH_HOT_KEY, 0, limit - 1);
    }

    public Set<ZSetOperations.TypedTuple<Object>> getHotSearchesWithScores(int limit) {
        return redisTemplate.opsForZSet().reverseRangeWithScores(SEARCH_HOT_KEY, 0, limit - 1);
    }

    public Double getSearchScore(String keyword) {
        return redisTemplate.opsForZSet().score(SEARCH_HOT_KEY, keyword);
    }

    public void removeHotSearch(String keyword) {
        redisTemplate.opsForZSet().remove(SEARCH_HOT_KEY, keyword);
    }

    public Long getHotSearchRank(String keyword) {
        return redisTemplate.opsForZSet().reverseRank(SEARCH_HOT_KEY, keyword);
    }
}
