package com.poti.agent.feign;

import com.poti.common.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 错题本服务（poti-wrongbook 8500）
 */
@FeignClient(name = "poti-wrongbook", url = "${poti.service.wrongbook-url:http://localhost:8500}", configuration = com.poti.agent.config.FeignConfig.class)
public interface WrongbookFeignClient {

    /** 错题数量 */
    @GetMapping("/wrongbook/internal/count")
    R<Integer> getWrongCount(@RequestParam("userId") Long userId);

    /** 错题分布（分类计数 + 难度计数 + 错题标题样例）——需要 poti-wrongbook 新增 internal 接口 */
    @GetMapping("/wrongbook/internal/distribution")
    R<Map<String, Object>> getDistribution(@RequestParam("userId") Long userId);
}
