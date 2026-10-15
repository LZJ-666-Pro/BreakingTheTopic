package com.poti.agent.feign;

import com.poti.common.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 刷题统计服务（poti-practice 8400）
 */
@FeignClient(name = "poti-practice", url = "${poti.service.practice-url:http://localhost:8400}", configuration = com.poti.agent.config.FeignConfig.class)
public interface PracticeFeignClient {

    /** 用户刷题统计：累计/今日题数、答对答错、正确率 */
    @GetMapping("/practice/internal/statistics")
    R<Map<String, Object>> getStatistics(@RequestParam("userId") Long userId);
}
