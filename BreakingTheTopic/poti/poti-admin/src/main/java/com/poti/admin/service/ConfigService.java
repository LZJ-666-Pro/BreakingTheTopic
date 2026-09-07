package com.poti.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.poti.admin.entity.Config;

public interface ConfigService extends IService<Config> {
    
    Config getByKey(String configKey);
    
    void updateByKey(String configKey, String configValue);
}
