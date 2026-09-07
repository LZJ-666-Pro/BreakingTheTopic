package com.poti.wrongbook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.poti.wrongbook.entity.Wrongbook;
import com.poti.wrongbook.mapper.WrongbookMapper;
import com.poti.wrongbook.service.WrongbookService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class WrongbookServiceImpl extends ServiceImpl<WrongbookMapper, Wrongbook> implements WrongbookService {

    @Override
    public Map<String, Object> getStatistics(Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        LambdaQueryWrapper<Wrongbook> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Wrongbook::getUserId, userId);
        
        List<Wrongbook> wrongbooks = this.list(wrapper);
        
        int totalWrong = wrongbooks.size();
        int mastered = (int) wrongbooks.stream().filter(w -> w.getMastered() != null && w.getMastered() == 1).count();
        int notMastered = totalWrong - mastered;
        
        Map<Long, Integer> categoryCount = new HashMap<>();
        for (Wrongbook w : wrongbooks) {
            categoryCount.merge(w.getQuestionId() % 6 + 1, 1, Integer::sum);
        }
        
        List<Map<String, Object>> byCategory = new ArrayList<>();
        String[] categoryNames = {"Java", "Python", "MySQL", "Redis", "Spring", "消息队列"};
        for (Map.Entry<Long, Integer> entry : categoryCount.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            int idx = entry.getKey().intValue() - 1;
            item.put("categoryName", idx >= 0 && idx < categoryNames.length ? categoryNames[idx] : "其他");
            item.put("count", entry.getValue());
            byCategory.add(item);
        }
        
        result.put("totalWrong", totalWrong);
        result.put("mastered", mastered);
        result.put("notMastered", notMastered);
        result.put("byCategory", byCategory);
        
        return result;
    }
}
