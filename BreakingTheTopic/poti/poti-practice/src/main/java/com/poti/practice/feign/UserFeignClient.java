package com.poti.practice.feign;

import com.poti.common.R;
import com.poti.practice.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "poti-user", configuration = FeignConfig.class)
public interface UserFeignClient {

    /** 会员状态（internal 通道）——竞赛优先报名权益：会员发布后即可报名，非会员开赛前 24 小时内开放 */
    @GetMapping("/vip/internal/status")
    R<Map<String, Object>> getVipStatus(@RequestParam("userId") Long userId);
}
