package com.poti.admin.controller;

import com.poti.admin.entity.Config;
import com.poti.admin.service.ConfigService;
import com.poti.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/admin/upload")
public class UploadController {

    @Value("${upload.path:./uploads}")
    private String uploadPath;

    @Value("${upload.url:http://localhost:8088/uploads}")
    private String uploadUrl;

    @Autowired
    private ConfigService configService;

    /** 上传大小上限（MB）：后台「系统配置-上传设置-文件大小限制」，缺失回退 2MB */
    private long maxUploadSizeMb() {
        Config config = configService.getByKey("upload_max_size");
        if (config != null && config.getConfigValue() != null) {
            try {
                long mb = Long.parseLong(config.getConfigValue().trim());
                if (mb > 0) {
                    return mb;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return 2;
    }

    /** 允许的扩展名白名单：后台「系统配置-上传设置-允许的文件类型」，缺失回退常见图片格式 */
    private java.util.Set<String> allowedTypes() {
        java.util.Set<String> defaults = java.util.Set.of("jpg", "jpeg", "png", "gif", "webp");
        Config config = configService.getByKey("upload_allow_types");
        if (config == null || config.getConfigValue() == null || config.getConfigValue().isBlank()) {
            return defaults;
        }
        java.util.Set<String> types = new java.util.HashSet<>();
        for (String t : config.getConfigValue().split("[,，]")) {
            String v = t.trim().toLowerCase(Locale.ROOT).replaceFirst("^\\.", "");
            if (!v.isEmpty()) {
                types.add(v);
            }
        }
        return types.isEmpty() ? defaults : types;
    }

    @PostMapping("/avatar")
    public R<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            log.info("收到头像上传请求，文件名：{}，大小：{} bytes",
                file.getOriginalFilename(), file.getSize());

            if (file.isEmpty()) {
                return R.error("上传文件不能为空");
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return R.error("只能上传图片文件");
            }

            // 大小与类型限制读取后台「上传设置」配置，保存后即时生效
            long maxSizeMb = maxUploadSizeMb();
            if (file.getSize() > maxSizeMb * 1024 * 1024) {
                return R.error("文件大小不能超过" + maxSizeMb + "MB");
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1)
                        .toLowerCase(Locale.ROOT);
            }
            if (!allowedTypes().contains(extension)) {
                return R.error("不支持的文件类型：" + extension);
            }

            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String fileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;

            String relativePath = "avatar/" + datePath;

            File uploadDir = new File(uploadPath);
            if (!uploadDir.isAbsolute()) {
                uploadDir = new File(System.getProperty("user.dir"), uploadPath);
            }

            File dir = new File(uploadDir, relativePath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File destFile = new File(dir, fileName);
            file.transferTo(destFile.getAbsoluteFile());

            String fileUrl = uploadUrl + "/" + relativePath + "/" + fileName;
            log.info("头像上传成功，文件路径：{}，访问URL：{}", destFile.getAbsolutePath(), fileUrl);
            log.info("返回给前端的URL：{}", fileUrl);

            return R.success(fileUrl);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return R.error("文件上传失败：" + e.getMessage());
        }
    }
}