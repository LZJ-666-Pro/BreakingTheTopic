package com.poti.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.poti.admin.entity.Config;
import com.poti.admin.service.ConfigService;
import com.poti.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/config")
public class ConfigController {

    @Autowired
    private ConfigService configService;

    @GetMapping("/list")
    public R<List<Config>> list() {
        try {
            LambdaQueryWrapper<Config> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByAsc(Config::getId);
            List<Config> configs = configService.list(wrapper);
            return R.success(configs);
        } catch (Exception e) {
            log.error("获取配置列表失败", e);
            return R.error("获取配置列表失败");
        }
    }

    @GetMapping("/get/{key}")
    public R<Config> getByKey(@PathVariable String key) {
        try {
            Config config = configService.getByKey(key);
            if (config == null) {
                return R.error("配置项不存在");
            }
            return R.success(config);
        } catch (Exception e) {
            log.error("获取配置失败", e);
            return R.error("获取配置失败");
        }
    }

    @PostMapping("/update")
    public R<Void> update(@RequestBody Map<String, String> request) {
        try {
            String configKey = request.get("configKey");
            String configValue = request.get("configValue");
            
            if (configKey == null || configKey.trim().isEmpty()) {
                return R.error("配置键不能为空");
            }
            
            Config config = configService.getByKey(configKey);
            if (config == null) {
                return R.error("配置项不存在");
            }
            
            config.setConfigValue(configValue);
            configService.updateById(config);
            
            log.info("配置更新成功: {} = {}", configKey, configValue);
            return R.success(null);
        } catch (Exception e) {
            log.error("更新配置失败", e);
            return R.error("更新配置失败");
        }
    }

    @PostMapping("/batch-update")
    public R<Void> batchUpdate(@RequestBody List<Config> configs) {
        try {
            for (Config config : configs) {
                if (config.getConfigKey() != null && config.getConfigValue() != null) {
                    Config existingConfig = configService.getByKey(config.getConfigKey());
                    if (existingConfig != null) {
                        existingConfig.setConfigValue(config.getConfigValue());
                        configService.updateById(existingConfig);
                    }
                }
            }
            
            log.info("批量更新配置成功，共更新{}项", configs.size());
            return R.success(null);
        } catch (Exception e) {
            log.error("批量更新配置失败", e);
            return R.error("批量更新配置失败");
        }
    }

    @GetMapping("/all")
    public R<Map<String, String>> getAllConfigs() {
        try {
            List<Config> configs = configService.list();
            Map<String, String> configMap = new HashMap<>();
            for (Config config : configs) {
                configMap.put(config.getConfigKey(), config.getConfigValue());
            }
            return R.success(configMap);
        } catch (Exception e) {
            log.error("获取所有配置失败", e);
            return R.error("获取所有配置失败");
        }
    }
}
