package com.poti.agent.feign;

import com.poti.common.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * 收藏服务（poti-favorite 8600）
 */
@FeignClient(name = "poti-favorite", url = "${poti.service.favorite-url:http://localhost:8600}", configuration = com.poti.agent.config.FeignConfig.class)
public interface FavoriteFeignClient {

    /** 收藏列表（internal 通道；Favorite 实体无答案字段，天然防作弊） */
    @GetMapping("/favorite/internal/list")
    R<List<Map<String, Object>>> getFavoriteList(@RequestParam("userId") Long userId,
                                                 @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                 @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize);
}
