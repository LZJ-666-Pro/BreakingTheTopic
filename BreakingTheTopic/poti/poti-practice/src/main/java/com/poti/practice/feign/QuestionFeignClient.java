package com.poti.practice.feign;

import com.poti.common.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient("poti-question")
public interface QuestionFeignClient {

    @GetMapping("/question/internal/{id}")
    R<Map<String, Object>> getQuestionById(@PathVariable("id") Long id);
}
