package com.poti.wrongbook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.poti.common.R;
import com.poti.wrongbook.entity.Wrongbook;
import com.poti.wrongbook.feign.QuestionFeignClient;
import com.poti.wrongbook.mapper.WrongbookMapper;
import com.poti.wrongbook.service.WrongbookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class WrongbookServiceImpl extends ServiceImpl<WrongbookMapper, Wrongbook> implements WrongbookService {

    @Autowired
    private QuestionFeignClient questionFeignClient;

    @Override
    public Map<String, Object> getStatistics(Long userId) {
        Map<String, Object> result = new HashMap<>();

        LambdaQueryWrapper<Wrongbook> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Wrongbook::getUserId, userId);

        List<Wrongbook> wrongbooks = this.list(wrapper);

        int totalWrong = wrongbooks.size();
        int mastered = (int) wrongbooks.stream().filter(w -> w.getMastered() != null && w.getMastered() == 1).count();
        int notMastered = totalWrong - mastered;

        // 按真实分类聚合（错题表存 categoryId，分类名经 question 服务 internal 接口解析）
        Map<Long, Integer> categoryCount = new HashMap<>();
        int uncategorized = 0;
        for (Wrongbook w : wrongbooks) {
            if (w.getCategoryId() != null) {
                categoryCount.merge(w.getCategoryId(), 1, Integer::sum);
            } else {
                uncategorized++;
            }
        }

        Map<Long, String> categoryNameMap = new HashMap<>();
        try {
            R<List<Map<String, Object>>> resp = questionFeignClient.getCategoryNames();
            if (resp != null && resp.getCode() != null && resp.getCode() == 200 && resp.getData() != null) {
                for (Map<String, Object> row : resp.getData()) {
                    Object id = row.get("id");
                    Object name = row.get("name");
                    if (id != null && name != null) {
                        categoryNameMap.put(((Number) id).longValue(), name.toString());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("解析错题分类名失败，降级显示未知分类", e);
        }

        List<Map<String, Object>> byCategory = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : categoryCount.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("categoryId", entry.getKey());
            item.put("categoryName", categoryNameMap.getOrDefault(entry.getKey(), "未知分类"));
            item.put("count", entry.getValue());
            byCategory.add(item);
        }
        // 历史数据可能未存分类 id，兜底归入「未分类」保证占比完整
        if (uncategorized > 0) {
            Map<String, Object> item = new HashMap<>();
            item.put("categoryId", 0L);
            item.put("categoryName", "未分类");
            item.put("count", uncategorized);
            byCategory.add(item);
        }
        // 错误数多的分类排前面，便于看薄弱点
        byCategory.sort((a, b) -> Integer.compare((int) b.get("count"), (int) a.get("count")));

        result.put("totalWrong", totalWrong);
        result.put("mastered", mastered);
        result.put("notMastered", notMastered);
        result.put("byCategory", byCategory);

        return result;
    }
}
