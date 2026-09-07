package com.poti.user.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.poti.user.entity.SearchRecord;
import com.poti.user.mapper.SearchRecordMapper;
import com.poti.user.service.SearchRecordService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@DS("search")
public class SearchRecordServiceImpl extends ServiceImpl<SearchRecordMapper, SearchRecord> implements SearchRecordService {

    @Override
    public void recordSearch(String keyword, Long userId) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }
        
        keyword = keyword.trim();
        
        LambdaQueryWrapper<SearchRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SearchRecord::getKeyword, keyword);
        SearchRecord record = getOne(wrapper);
        
        if (record != null) {
            record.setSearchCount(record.getSearchCount() + 1);
            record.setLastSearchTime(LocalDateTime.now());
            
            if (userId != null && !userId.equals(record.getUserId())) {
                record.setUserCount(record.getUserCount() + 1);
            }
            
            updateById(record);
        } else {
            record = new SearchRecord();
            record.setKeyword(keyword);
            record.setUserId(userId);
            record.setSearchCount(1);
            record.setUserCount(1);
            record.setLastSearchTime(LocalDateTime.now());
            save(record);
        }
    }

    @Override
    public List<String> getHotKeywords(int limit) {
        List<SearchRecord> records = baseMapper.selectHotKeywords(limit);
        return records.stream()
                .map(SearchRecord::getKeyword)
                .collect(Collectors.toList());
    }
}
