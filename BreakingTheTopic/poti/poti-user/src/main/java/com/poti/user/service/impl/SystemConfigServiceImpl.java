package com.poti.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.poti.user.entity.SystemConfig;
import com.poti.user.mapper.SystemConfigMapper;
import com.poti.user.service.SystemConfigService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements SystemConfigService {

    @Override
    public String getValueByKey(String key) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, key);
        SystemConfig config = getOne(wrapper);
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    public Map<String, String> getAllContactConfig() {
        Map<String, String> result = new HashMap<>();
        
        List<String> keys = List.of(
            "contact_wechat", "contact_email", "contact_qq", "contact_weibo",
            "contact_work_time", "group_qq_1", "group_qq_2", "group_qq_3", "group_qq_4"
        );
        
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SystemConfig::getConfigKey, keys);
        List<SystemConfig> configs = list(wrapper);
        
        for (SystemConfig config : configs) {
            result.put(config.getConfigKey(), config.getConfigValue());
        }
        
        return result;
    }
}
