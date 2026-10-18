package com.poti.wrongbook.feign;

import com.poti.common.R;
import com.poti.wrongbook.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@FeignClient(name = "poti-question", configuration = FeignConfig.class)
public interface QuestionFeignClient {

    /** 全量分类 id+名称（错题统计按真实分类聚合时解析分类名） */
    @GetMapping("/question/category/internal/names")
    R<List<Map<String, Object>>> getCategoryNames();
}
