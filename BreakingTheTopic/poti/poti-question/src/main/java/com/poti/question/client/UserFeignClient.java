package com.poti.question.client;

import com.poti.common.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 用户服务（poti-user 8200）内部接口客户端
 */
@FeignClient(name = "poti-user", url = "${poti.service.user-url:http://localhost:8200}", configuration = com.poti.question.config.FeignConfig.class)
public interface UserFeignClient {

    /** 题解查看校验（internal 通道）——会员无限查看；非会员每日 5 篇（user 服务 Redis Set 按题去重计数） */
    @GetMapping("/vip/internal/check-solution")
    R<Map<String, Object>> checkSolutionView(@RequestParam("userId") Long userId,
                                             @RequestParam(value = "questionId", required = false) Long questionId);
}
