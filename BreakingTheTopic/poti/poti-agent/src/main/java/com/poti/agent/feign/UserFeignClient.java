package com.poti.agent.feign;

import com.poti.common.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 用户/签到服务（poti-user 8200）
 */
@FeignClient(name = "poti-user", url = "${poti.service.user-url:http://localhost:8200}", configuration = com.poti.agent.config.FeignConfig.class)
public interface UserFeignClient {

    /** 签到状态（连续天数）——poti-user 需提供 /checkin/internal/status */
    @GetMapping("/checkin/internal/status")
    R<Map<String, Object>> getCheckinStatus(@RequestParam("userId") Long userId);

    /** 好友刷题排行（internal 通道，含用户自己） */
    @GetMapping("/user/rank/internal/friends")
    R<Map<String, Object>> getFriendRanking(@RequestParam("userId") Long userId,
                                            @RequestParam(value = "type", defaultValue = "total") String type);

    /** 会员状态（internal 通道）——AI 提问每日限额 VIP 联动（VIP 100 次/普通 20 次） */
    @GetMapping("/vip/internal/status")
    R<Map<String, Object>> getVipStatus(@RequestParam("userId") Long userId);
}
