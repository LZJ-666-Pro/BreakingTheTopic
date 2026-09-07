package com.poti.user.feign;

import com.poti.common.R;
import com.poti.user.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "poti-practice", path = "/practice", configuration = FeignConfig.class)
public interface PracticeFeignClient {

    @GetMapping("/internal/statistics")
    R<Map<String, Object>> getStatistics(@RequestParam("userId") Long userId);

    @GetMapping("/internal/calendar")
    R<Map<String, Object>> getCalendar(@RequestParam("userId") Long userId,
                                       @RequestParam("year") Integer year,
                                       @RequestParam("month") Integer month);

    @GetMapping("/internal/ranking")
    R<List<Map<String, Object>>> getRanking(
            @RequestParam("type") String type,
            @RequestParam("page") Integer page,
            @RequestParam("limit") Integer limit);

    @GetMapping("/internal/user-rank")
    R<Map<String, Object>> getUserRank(
            @RequestParam("userId") Long userId,
            @RequestParam("type") String type);
}
