package com.poti.user.controller;

import com.poti.common.R;
import com.poti.user.service.CheckinService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/checkin")
public class CheckinController {

    @Autowired
    private CheckinService checkinService;

    @PostMapping("/do")
    public R<Map<String, Object>> doCheckIn(@RequestParam Long userId) {
        try {
            log.info("用户 {} 开始签到", userId);
            
            Map<String, Object> result = checkinService.doCheckIn(userId);
            
            if ((Boolean) result.get("alreadyCheckedIn")) {
                return R.error("今天已经签到过了");
            }
            
            return R.success(result);
        } catch (Exception e) {
            log.error("签到失败", e);
            return R.error("签到失败: " + e.getMessage());
        }
    }

    @GetMapping("/status")
    public R<Map<String, Object>> getCheckInStatus(@RequestParam Long userId) {
        try {
            boolean hasCheckedIn = checkinService.hasCheckedInToday(userId);
            int streakDays = checkinService.getStreakDays(userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("hasCheckedIn", hasCheckedIn);
            result.put("streakDays", streakDays);
            
            return R.success(result);
        } catch (Exception e) {
            log.error("获取签到状态失败", e);
            return R.error("获取签到状态失败: " + e.getMessage());
        }
    }
}
