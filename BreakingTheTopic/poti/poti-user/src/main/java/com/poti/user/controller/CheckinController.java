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

            // 里程碑奖励在签到事务提交后发放（内部读后台跨库配置，不能进事务）；
            // 发放失败不影响签到结果，仅响应中无 rewardDays 字段
            Integer rewardDays = checkinService.grantMilestoneReward(userId, (Integer) result.get("streakDays"));
            if (rewardDays != null) {
                result.put("rewardDays", rewardDays);
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

            // 下一个连签奖励提示（开关关闭/全部达成时无该字段）
            Map<String, Object> nextReward = checkinService.getRewardHint(streakDays);
            if (nextReward != null) {
                result.put("nextReward", nextReward);
            }

            return R.success(result);
        } catch (Exception e) {
            log.error("获取签到状态失败", e);
            return R.error("获取签到状态失败: " + e.getMessage());
        }
    }

    /**
     * internal：签到状态查询（AI 助手 poti-agent 经内部令牌调用，userId 由调用方显式指定）
     */
    @GetMapping("/internal/status")
    public R<Map<String, Object>> getCheckInStatusInternal(@RequestParam("userId") Long userId) {
        try {
            boolean hasCheckedIn = checkinService.hasCheckedInToday(userId);
            int streakDays = checkinService.getStreakDays(userId);

            Map<String, Object> result = new HashMap<>();
            result.put("hasCheckedIn", hasCheckedIn);
            result.put("streakDays", streakDays);

            return R.success(result);
        } catch (Exception e) {
            log.error("internal 获取签到状态失败", e);
            return R.error("获取签到状态失败: " + e.getMessage());
        }
    }

    /**
     * internal：签到（AI 助手 poti-agent 经内部令牌调用）
     * 逻辑与 /do 完全一致：签到事务提交后再发放里程碑奖励（内部读后台跨库配置，不能进事务）
     */
    @PostMapping("/internal/do")
    public R<Map<String, Object>> doCheckInInternal(@RequestParam("userId") Long userId) {
        try {
            Map<String, Object> result = checkinService.doCheckIn(userId);

            if ((Boolean) result.get("alreadyCheckedIn")) {
                return R.error("今天已经签到过了");
            }

            Integer rewardDays = checkinService.grantMilestoneReward(userId, (Integer) result.get("streakDays"));
            if (rewardDays != null) {
                result.put("rewardDays", rewardDays);
            }
            return R.success(result);
        } catch (Exception e) {
            log.error("internal 签到失败: userId={}", userId, e);
            return R.error("签到失败: " + e.getMessage());
        }
    }
}
