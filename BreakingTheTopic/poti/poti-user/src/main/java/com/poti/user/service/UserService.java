package com.poti.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.poti.user.entity.User;

import java.util.Map;

public interface UserService extends IService<User> {

    User findOrCreateByOpenid(String openid);

    Map<String, Object> getStatistics(Long userId);

    Map<String, Object> getCalendar(Long userId, Integer year, Integer month);
}