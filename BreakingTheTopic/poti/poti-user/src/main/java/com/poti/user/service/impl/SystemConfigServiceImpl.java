package com.poti.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.poti.user.entity.SystemConfig;
import com.poti.user.mapper.AdminConfigMapper;
import com.poti.user.mapper.SystemConfigMapper;
import com.poti.user.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements SystemConfigService {

    @Autowired
    private AdminConfigMapper adminConfigMapper;

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

        // 后台「网站设置」跨库补充（poti_admin.sys_config），供小程序联系页「关于本站」展示
        List<String> siteKeys = List.of("site_name", "site_description", "site_icp", "site_copyright");
        for (Map<String, Object> row : adminConfigMapper.selectValuesByKeys(siteKeys)) {
            Object k = row.get("configKey");
            Object v = row.get("configValue");
            if (k != null && v != null) {
                result.put(k.toString(), v.toString());
            }
        }

        return result;
    }
}
