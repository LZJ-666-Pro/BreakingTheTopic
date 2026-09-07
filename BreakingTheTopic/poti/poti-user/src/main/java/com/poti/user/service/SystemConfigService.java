package com.poti.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.poti.user.entity.SystemConfig;

import java.util.Map;

public interface SystemConfigService extends IService<SystemConfig> {

    String getValueByKey(String key);

    Map<String, String> getAllContactConfig();
}
