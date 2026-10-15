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
}
