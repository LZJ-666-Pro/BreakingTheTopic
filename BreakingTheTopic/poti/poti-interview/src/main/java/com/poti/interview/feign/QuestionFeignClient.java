package com.poti.interview.feign;

import com.poti.common.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient("poti-question")
public interface QuestionFeignClient {

    @GetMapping("/question/random/type")
    R<List<Map<String, Object>>> getRandomQuestions(@RequestParam("type") String type, 
                                                     @RequestParam("limit") int limit);
    
    @GetMapping("/question/random/category")
    R<List<Map<String, Object>>> getRandomQuestionsByCategory(@RequestParam("categoryId") Long categoryId, 
                                                               @RequestParam("limit") int limit);
    
    @GetMapping("/question/category/list")
    R<List<Map<String, Object>>> getCategoryList();
}
