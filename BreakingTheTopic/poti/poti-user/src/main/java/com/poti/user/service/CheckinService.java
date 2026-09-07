package com.poti.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.poti.user.entity.CheckIn;

import java.util.Map;

public interface CheckinService extends IService<CheckIn> {
    
    Map<String, Object> doCheckIn(Long userId);
    
    boolean hasCheckedInToday(Long userId);
    
    int getStreakDays(Long userId);
}
