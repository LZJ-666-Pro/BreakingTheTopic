package com.poti.user.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${avatar.upload.path:./uploads/avatars/}")
    private String avatarUploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 与 UserController.uploadAvatar 保持一致的路径解析逻辑
        String absoluteUploadPath = new File(avatarUploadPath).getAbsolutePath();
        File uploadDir = new File(absoluteUploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        String location = "file:" + uploadDir.getAbsolutePath() + "/";
        registry.addResourceHandler("/avatars/**")
                .addResourceLocations(location);
    }
}