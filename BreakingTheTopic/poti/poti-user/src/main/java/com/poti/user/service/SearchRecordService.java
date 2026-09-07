package com.poti.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.poti.user.entity.SearchRecord;

import java.util.List;

public interface SearchRecordService extends IService<SearchRecord> {

    void recordSearch(String keyword, Long userId);

    List<String> getHotKeywords(int limit);
}
