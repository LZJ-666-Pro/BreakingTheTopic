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
