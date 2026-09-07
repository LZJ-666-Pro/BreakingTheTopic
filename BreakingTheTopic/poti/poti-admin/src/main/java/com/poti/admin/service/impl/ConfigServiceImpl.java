package com.poti.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.poti.admin.entity.Config;
import com.poti.admin.mapper.ConfigMapper;
import com.poti.admin.service.ConfigService;
import org.springframework.stereotype.Service;

@Service
public class ConfigServiceImpl extends ServiceImpl<ConfigMapper, Config> implements ConfigService {
    
    @Override
    public Config getByKey(String configKey) {
        LambdaQueryWrapper<Config> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Config::getConfigKey, configKey);
        return this.getOne(wrapper);
    }
    
    @Override
    public void updateByKey(String configKey, String configValue) {
        Config config = this.getByKey(configKey);
        if (config != null) {
            config.setConfigValue(configValue);
            this.updateById(config);
        }
    }
}
