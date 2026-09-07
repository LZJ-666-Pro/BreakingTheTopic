package com.poti.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.poti.common.R;
import com.poti.common.utils.Result;
import com.poti.user.dto.RankDTO;
import com.poti.user.entity.User;
import com.poti.user.entity.UserStats;
import com.poti.user.feign.PracticeFeignClient;
import com.poti.user.mapper.FriendshipMapper;
import com.poti.user.mapper.UserMapper;
import com.poti.user.mapper.UserStatsMapper;
import com.poti.user.service.RankingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RankingServiceImpl implements RankingService {
    
    @Autowired
    private UserStatsMapper userStatsMapper;
    
    @Autowired
    private FriendshipMapper friendshipMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PracticeFeignClient practiceFeignClient;
    
    @Override
    public Result<RankDTO> getRanking(Long userId, String type, Integer page, Integer limit) {
        try {
            if ("streak".equals(type)) {
                return getStreakRanking(userId, page, limit);
            }
            
            R<List<Map<String, Object>>> response = practiceFeignClient.getRanking(type, page, limit);
            
            if (response == null || response.getData() == null) {
                log.error("从 practice 服务获取排行榜失败");
                return Result.error("获取排行榜失败");
            }
            
            List<Map<String, Object>> rankData = response.getData();
            List<RankDTO.RankItemDTO> rankItems = convertPracticeDataToRankItems(rankData, type);
            
            RankDTO rankDTO = new RankDTO();
            rankDTO.setTopThree(rankItems.stream().limit(3).collect(Collectors.toList()));
            rankDTO.setRankList(rankItems.stream().skip(3).collect(Collectors.toList()));
            rankDTO.setMyRank(getMyRankFromPractice(userId, type));
            rankDTO.setHasMore(rankItems.size() >= limit);
            
            return Result.success(rankDTO);
        } catch (Exception e) {
            log.error("获取排行榜失败", e);
            return Result.error("获取排行榜失败：" + e.getMessage());
        }
    }
    
    private Result<RankDTO> getStreakRanking(Long userId, Integer page, Integer limit) {
        try {
            int offset = page * limit;
            List<Map<String, Object>> rankData = userStatsMapper.getRankingByConsecutiveDays(offset, limit);
            List<RankDTO.RankItemDTO> rankItems = convertToRankItems(rankData, "streak");
            
            RankDTO rankDTO = new RankDTO();
            rankDTO.setTopThree(rankItems.stream().limit(3).collect(Collectors.toList()));
            rankDTO.setRankList(rankItems.stream().skip(3).collect(Collectors.toList()));
            rankDTO.setMyRank(getMyRank(userId, "streak"));
            rankDTO.setHasMore(rankItems.size() >= limit);
            
            return Result.success(rankDTO);
        } catch (Exception e) {
            log.error("获取连续签到排行榜失败", e);
            return Result.error("获取排行榜失败：" + e.getMessage());
        }
    }
    
    @Override
    public Result<RankDTO> getFriendRanking(Long userId, String type, Integer page, Integer limit) {
        try {
            List<Long> friendIds = friendshipMapper.getFriendIds(userId);
            
            if (friendIds == null) {
                friendIds = new ArrayList<>();
            }
            
            friendIds.add(userId);
            
            if ("streak".equals(type)) {
                return getFriendStreakRanking(userId, friendIds, page, limit);
            }
            
            List<Map<String, Object>> rankData = getFriendRankingFromPractice(friendIds, type);
            List<RankDTO.RankItemDTO> rankItems = convertPracticeDataToRankItems(rankData, type);
            
            RankDTO.RankItemDTO myRank = getMyRankInFriends(userId, rankItems, type);
            
            RankDTO rankDTO = new RankDTO();
            rankDTO.setTopThree(rankItems.stream().limit(3).collect(Collectors.toList()));
            rankDTO.setRankList(rankItems.stream().skip(3).collect(Collectors.toList()));
            rankDTO.setMyRank(myRank);
            rankDTO.setHasMore(rankItems.size() >= limit);
            
            return Result.success(rankDTO);
        } catch (Exception e) {
            log.error("获取好友排行榜失败", e);
            return Result.error("获取好友排行榜失败：" + e.getMessage());
        }
    }
    
    private Result<RankDTO> getFriendStreakRanking(Long userId, List<Long> friendIds, Integer page, Integer limit) {
        try {
            int offset = page * limit;
            String orderField = "us.consecutive_days";
            
            List<Map<String, Object>> rankData = friendshipMapper.getFriendRankingByField(
                    friendIds, orderField, offset, limit);
            
            List<RankDTO.RankItemDTO> rankItems = convertToRankItems(rankData, "streak");
            
            RankDTO.RankItemDTO myRank = getMyRankInFriends(userId, rankItems, "streak");
            
            RankDTO rankDTO = new RankDTO();
            rankDTO.setTopThree(rankItems.stream().limit(3).collect(Collectors.toList()));
            rankDTO.setRankList(rankItems.stream().skip(3).collect(Collectors.toList()));
            rankDTO.setMyRank(myRank);
            rankDTO.setHasMore(rankItems.size() >= limit);
            
            return Result.success(rankDTO);
        } catch (Exception e) {
            log.error("获取好友连续签到排行榜失败", e);
            return Result.error("获取排行榜失败：" + e.getMessage());
        }
    }
    
    private List<Map<String, Object>> getFriendRankingFromPractice(List<Long> friendIds, String type) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Long friendId : friendIds) {
            try {
                log.info("获取好友 {} 的统计数据", friendId);
                R<Map<String, Object>> statsResponse = practiceFeignClient.getStatistics(friendId);
                log.info("好友 {} 统计数据响应: {}", friendId, statsResponse);
                
                if (statsResponse != null && statsResponse.getData() != null) {
                    Map<String, Object> stats = statsResponse.getData();
                    log.info("好友 {} 统计数据: totalQuestionCount={}, todayCount={}, correctCount={}", 
                            friendId, stats.get("totalQuestionCount"), stats.get("todayCount"), stats.get("correctCount"));
                    
                    User user = userMapper.selectById(friendId);
                    
                    Map<String, Object> item = new HashMap<>();
                    item.put("user_id", friendId);
                    item.put("nickname", user != null ? user.getNickname() : "用户" + friendId);
                    item.put("avatar_url", user != null ? user.getAvatarUrl() : null);
                    item.put("total_questions", stats.get("totalQuestionCount"));
                    item.put("today_questions", stats.get("todayCount"));
                    item.put("correct_questions", stats.get("correctCount"));
                    
                    Integer total = stats.get("totalQuestionCount") != null ? 
                            ((Number) stats.get("totalQuestionCount")).intValue() : 0;
                    Integer correct = stats.get("correctCount") != null ? 
                            ((Number) stats.get("correctCount")).intValue() : 0;
                    item.put("accuracy", total > 0 ? Math.round(correct * 100.0 / total * 100) / 100.0 : 0);
                    
                    result.add(item);
                } else {
                    log.warn("好友 {} 统计数据为空", friendId);
                }
            } catch (Exception e) {
                log.error("获取用户 {} 统计数据失败", friendId, e);
            }
        }
        
        String sortField;
        switch (type) {
            case "total":
                sortField = "total_questions";
                break;
            case "today":
                sortField = "today_questions";
                break;
            case "accuracy":
                sortField = "accuracy";
                break;
            default:
                sortField = "total_questions";
        }
        
        result.sort((a, b) -> {
            Object aVal = a.get(sortField);
            Object bVal = b.get(sortField);
            double aNum = aVal != null ? ((Number) aVal).doubleValue() : 0;
            double bNum = bVal != null ? ((Number) bVal).doubleValue() : 0;
            return Double.compare(bNum, aNum);
        });
        
        return result;
    }
    
    private List<RankDTO.RankItemDTO> convertPracticeDataToRankItems(List<Map<String, Object>> rankData, String type) {
        List<RankDTO.RankItemDTO> items = new ArrayList<>();
        int rank = 1;
        
        for (Map<String, Object> data : rankData) {
            Long userId = ((Number) data.get("user_id")).longValue();
            
            User user = userMapper.selectById(userId);
            
            RankDTO.RankItemDTO item = new RankDTO.RankItemDTO();
            item.setId(userId);
            item.setRank(rank++);
            item.setNickname(user != null ? user.getNickname() : "用户" + userId);
            item.setAvatarUrl(user != null ? user.getAvatarUrl() : "");
            
            Object scoreValue;
            switch (type) {
                case "total":
                    scoreValue = data.get("total_questions");
                    item.setScore(scoreValue != null ? ((Number) scoreValue).intValue() : 0);
                    break;
                case "today":
                    scoreValue = data.get("today_questions");
                    item.setScore(scoreValue != null ? ((Number) scoreValue).intValue() : 0);
                    break;
                case "accuracy":
                    scoreValue = data.get("accuracy");
                    item.setScore((scoreValue != null ? ((Number) scoreValue).doubleValue() : 0) + "%");
                    break;
                default:
                    scoreValue = data.get("total_questions");
                    item.setScore(scoreValue != null ? ((Number) scoreValue).intValue() : 0);
            }
            
            items.add(item);
        }
        
        return items;
    }
    
    private RankDTO.RankItemDTO getMyRankFromPractice(Long userId, String type) {
        try {
            R<Map<String, Object>> response = practiceFeignClient.getUserRank(userId, type);
            
            if (response == null || response.getData() == null) {
                return getMyRank(userId, type);
            }
            
            Map<String, Object> rankData = response.getData();
            
            RankDTO.RankItemDTO item = new RankDTO.RankItemDTO();
            item.setId(userId);
            item.setRank(rankData.get("rank") != null ? ((Number) rankData.get("rank")).intValue() : 1);
            item.setNickname("我");
            item.setAvatarUrl("");
            
            switch (type) {
                case "total":
                    item.setScore(rankData.get("totalQuestions") != null ? 
                            ((Number) rankData.get("totalQuestions")).intValue() : 0);
                    break;
                case "today":
                    item.setScore(rankData.get("todayQuestions") != null ? 
                            ((Number) rankData.get("todayQuestions")).intValue() : 0);
                    break;
                case "accuracy":
                    Integer total = rankData.get("totalQuestions") != null ? 
                            ((Number) rankData.get("totalQuestions")).intValue() : 0;
                    Integer correct = rankData.get("correctCount") != null ? 
                            ((Number) rankData.get("correctCount")).intValue() : 0;
                    item.setScore(total > 0 ? Math.round(correct * 100.0 / total * 100) / 100.0 + "%" : "0%");
                    break;
                default:
                    item.setScore(rankData.get("totalQuestions") != null ? 
                            ((Number) rankData.get("totalQuestions")).intValue() : 0);
            }
            
            return item;
        } catch (Exception e) {
            log.error("从 practice 服务获取用户排名失败", e);
            return getMyRank(userId, type);
        }
    }
    
    private List<RankDTO.RankItemDTO> convertToRankItems(List<Map<String, Object>> rankData, String type) {
        List<RankDTO.RankItemDTO> items = new ArrayList<>();
        int rank = 1;
        
        for (Map<String, Object> data : rankData) {
            RankDTO.RankItemDTO item = new RankDTO.RankItemDTO();
            item.setId(((Number) data.get("user_id")).longValue());
            item.setRank(rank++);
            item.setNickname((String) data.get("nickname"));
            item.setAvatarUrl((String) data.get("avatar_url"));
            
            Object scoreValue;
            switch (type) {
                case "total":
                    scoreValue = data.get("total_questions");
                    item.setScore(scoreValue != null ? scoreValue : 0);
                    break;
                case "today":
                    scoreValue = data.get("today_questions");
                    item.setScore(scoreValue != null ? scoreValue : 0);
                    break;
                case "accuracy":
                    scoreValue = data.get("accuracy");
                    item.setScore((scoreValue != null ? scoreValue : 0) + "%");
                    break;
                case "streak":
                    scoreValue = data.get("consecutive_days");
                    item.setScore((scoreValue != null ? scoreValue : 0) + "天");
                    break;
                default:
                    scoreValue = data.get("total_questions");
                    item.setScore(scoreValue != null ? scoreValue : 0);
            }
            
            items.add(item);
        }
        
        return items;
    }
    
    private RankDTO.RankItemDTO getMyRank(Long userId, String type) {
        LambdaQueryWrapper<UserStats> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserStats::getUserId, userId);
        UserStats userStats = userStatsMapper.selectOne(wrapper);
        
        RankDTO.RankItemDTO item = new RankDTO.RankItemDTO();
        item.setId(userId);
        item.setRank(1);
        item.setNickname("我");
        item.setAvatarUrl("");
        
        if (userStats == null) {
            switch (type) {
                case "total":
                case "today":
                    item.setScore(0);
                    break;
                case "accuracy":
                    item.setScore("0%");
                    break;
                case "streak":
                    item.setScore("0天");
                    break;
                default:
                    item.setScore(0);
            }
            return item;
        }
        
        Integer myRank = null;
        switch (type) {
            case "total":
                myRank = userStatsMapper.getUserRankByTotalQuestions(userId);
                break;
            case "today":
                myRank = userStatsMapper.getUserRankByTodayQuestions(userId);
                break;
            case "accuracy":
                myRank = userStatsMapper.getUserRankByAccuracy(userId);
                break;
            case "streak":
                myRank = userStatsMapper.getUserRankByConsecutiveDays(userId);
                break;
            default:
                myRank = userStatsMapper.getUserRankByTotalQuestions(userId);
        }
        
        item.setRank(myRank != null ? myRank : 1);
        
        Integer totalQuestions = userStats.getTotalQuestions() != null ? userStats.getTotalQuestions() : 0;
        Integer todayQuestions = userStats.getTodayQuestions() != null ? userStats.getTodayQuestions() : 0;
        Integer correctQuestions = userStats.getCorrectQuestions() != null ? userStats.getCorrectQuestions() : 0;
        Integer consecutiveDays = userStats.getConsecutiveDays() != null ? userStats.getConsecutiveDays() : 0;
        
        switch (type) {
            case "total":
                item.setScore(totalQuestions);
                break;
            case "today":
                item.setScore(todayQuestions);
                break;
            case "accuracy":
                if (totalQuestions > 0) {
                    item.setScore(Math.round(correctQuestions * 100.0 / totalQuestions * 100) / 100.0 + "%");
                } else {
                    item.setScore("0%");
                }
                break;
            case "streak":
                item.setScore(consecutiveDays + "天");
                break;
            default:
                item.setScore(totalQuestions);
        }
        
        return item;
    }
    
    private RankDTO.RankItemDTO getMyRankInFriends(Long userId, List<RankDTO.RankItemDTO> rankItems, String type) {
        for (RankDTO.RankItemDTO item : rankItems) {
            if (item.getId().equals(userId)) {
                RankDTO.RankItemDTO myRank = new RankDTO.RankItemDTO();
                myRank.setId(userId);
                myRank.setRank(item.getRank());
                myRank.setNickname("我");
                myRank.setAvatarUrl("");
                myRank.setScore(item.getScore());
                return myRank;
            }
        }
        
        return getMyRankFromPractice(userId, type);
    }
}
