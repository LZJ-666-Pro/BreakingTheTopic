package com.poti.user.controller;

import com.poti.common.R;
import com.poti.user.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/config")
public class SystemConfigController {

    @Autowired
    private SystemConfigService systemConfigService;

    @GetMapping("/contact")
    public R<Map<String, String>> getContactConfig() {
        Map<String, String> config = systemConfigService.getAllContactConfig();
        return R.success(config);
    }

    @GetMapping("/value/{key}")
    public R<String> getConfigValue(@PathVariable String key) {
        String value = systemConfigService.getValueByKey(key);
        return R.success(value);
    }
}
