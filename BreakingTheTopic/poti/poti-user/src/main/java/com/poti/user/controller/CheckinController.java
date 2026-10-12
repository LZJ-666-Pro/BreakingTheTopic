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

    // 用户身份统一取自鉴权过滤器注入的 X-User-Id（由 JWT 解析而来，无法伪造），
    // 不再信任前端传参，防止代他人签到/查看他人签到状态；
    // 前端签到请求已统一携带 JWT，/checkin/** 同步移出了免鉴权白名单

    @PostMapping("/do")
    public R<Map<String, Object>> doCheckIn(@RequestHeader("X-User-Id") Long userId) {
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
    public R<Map<String, Object>> getCheckInStatus(@RequestHeader("X-User-Id") Long userId) {
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
