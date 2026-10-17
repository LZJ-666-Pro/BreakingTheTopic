package com.poti.agent.feign;

import com.poti.common.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.Set;

/**
 * 搜索服务（poti-search 8800）
 */
@FeignClient(name = "poti-search", url = "${poti.service.search-url:http://localhost:8800}", configuration = com.poti.agent.config.FeignConfig.class)
public interface SearchFeignClient {

    /** 题目搜索（internal 通道：不写搜索历史/热搜）——需要 poti-search 新增 internal 接口 */
    @GetMapping("/search/internal/question")
    R<Map<String, Object>> searchQuestions(@RequestParam(value = "keyword", required = false) String keyword,
                                           @RequestParam(value = "categoryId", required = false) Long categoryId,
                                           @RequestParam(value = "difficulty", required = false) Integer difficulty,
                                           @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                           @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize);

    /** 热搜词（公开白名单接口 /search/hot） */
    @GetMapping("/search/hot")
    R<Set<Object>> getHotSearches(@RequestParam(value = "limit", defaultValue = "10") Integer limit);
}
