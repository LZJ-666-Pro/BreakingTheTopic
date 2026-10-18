package com.poti.practice.feign;

import com.poti.common.R;
import com.poti.practice.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "poti-question", configuration = FeignConfig.class)
public interface QuestionFeignClient {

    @GetMapping("/question/internal/{id}")
    R<Map<String, Object>> getQuestionById(@PathVariable("id") Long id);

    /**
     * 竞赛随机抽题（按题型：1单选 2多选 3填空 4解答，剥离答案/解析）
     * limit 上限 50 由 question 服务保证
     */
    @GetMapping("/question/internal/random")
    R<List<Map<String, Object>>> getRandomQuestions(@RequestParam("categoryId") Long categoryId,
                                                    @RequestParam("limit") Integer limit,
                                                    @RequestParam(value = "types", required = false) String types);

    /** 按 ID 批量取题（竞赛断点续答恢复快照用，剥离答案/解析） */
    @GetMapping("/question/internal/batch")
    R<List<Map<String, Object>>> getQuestionsByIds(@RequestParam("ids") String ids);

    /** 按 ID 批量取题（含答案/解析，仅交卷后试卷回顾使用） */
    @GetMapping("/question/internal/review-batch")
    R<List<Map<String, Object>>> getReviewQuestions(@RequestParam("ids") String ids);
}
