package com.poti.user.controller;

import com.poti.common.R;
import com.poti.user.service.SearchRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private SearchRecordService searchRecordService;

    @PostMapping("/record")
    public R<String> recordSearch(@RequestBody Map<String, String> body,
                                  @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        String keyword = body.get("keyword");
        if (keyword == null || keyword.trim().isEmpty()) {
            return R.error("关键词不能为空");
        }
        
        searchRecordService.recordSearch(keyword, userId);
        return R.success("记录成功");
    }

    @GetMapping("/hot")
    public R<Map<String, Object>> getHotKeywords(@RequestParam(defaultValue = "10") int limit) {
        List<String> keywords = searchRecordService.getHotKeywords(limit);
        
        Map<String, Object> result = new HashMap<>();
        result.put("keywords", keywords);
        return R.success(result);
    }
}
