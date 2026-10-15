package com.poti.agent.feign;

import com.poti.common.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * 题库服务（poti-question 8300）
 */
@FeignClient(name = "poti-question", url = "${poti.service.question-url:http://localhost:8300}", configuration = com.poti.agent.config.FeignConfig.class)
public interface QuestionFeignClient {

    /** 题目详情（含 answer/analysis，工具层对 LLM 剥离 answer 字段防作弊） */
    @GetMapping("/question/internal/{id}")
    R<Map<String, Object>> getQuestion(@PathVariable("id") Long id);

    /** 全量分组+分类列表（公开白名单接口，含 questionCount） */
    @GetMapping("/question/category/groups")
    R<Map<String, Object>> getCategoryGroups();
}
