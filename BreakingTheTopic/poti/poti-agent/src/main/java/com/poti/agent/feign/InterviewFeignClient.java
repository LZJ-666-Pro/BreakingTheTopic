package com.poti.agent.feign;

import com.poti.common.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * 面试特训营服务（poti-interview 8700）
 */
@FeignClient(name = "poti-interview", url = "${poti.service.interview-url:http://localhost:8700}", configuration = com.poti.agent.config.FeignConfig.class)
public interface InterviewFeignClient {

    /** 特训营列表（internal 通道，含用户加入状态） */
    @GetMapping("/camp/internal/list")
    R<List<Map<String, Object>>> getCampList(@RequestParam("userId") Long userId);
}
