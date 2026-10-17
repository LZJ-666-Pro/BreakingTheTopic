package com.poti.user.controller;

import com.poti.common.utils.Result;
import com.poti.user.dto.RankDTO;
import com.poti.user.service.RankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/rank")
public class RankingController {
    
    @Autowired
    private RankingService rankingService;
    
    /** 好友刷题排行（internal 通道，供 AI 导师工具调用） */
    @GetMapping("/internal/friends")
    public Result<RankDTO> getFriendRankingInternal(
            @RequestParam("userId") Long userId,
            @RequestParam(defaultValue = "total") String type) {
        return rankingService.getFriendRanking(userId, type, 0, 10);
    }

    @GetMapping
    public Result<RankDTO> getRanking(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam(defaultValue = "total") String type,
            @RequestParam(defaultValue = "world") String subtab,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        
        if ("friends".equals(subtab)) {
            return rankingService.getFriendRanking(userId, type, page, limit);
        } else {
            return rankingService.getRanking(userId, type, page, limit);
        }
    }
}
