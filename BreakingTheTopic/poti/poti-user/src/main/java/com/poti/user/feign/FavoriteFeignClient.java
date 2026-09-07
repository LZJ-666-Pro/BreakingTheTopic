package com.poti.user.feign;

import com.poti.common.R;
import com.poti.user.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "poti-favorite", path = "/favorite", configuration = FeignConfig.class)
public interface FavoriteFeignClient {

    @GetMapping("/internal/count")
    R<Integer> getFavoriteCount(@RequestParam("userId") Long userId);
}
