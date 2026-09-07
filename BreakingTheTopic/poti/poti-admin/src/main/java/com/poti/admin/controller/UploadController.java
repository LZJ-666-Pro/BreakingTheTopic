package com.poti.admin.controller;

import com.poti.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/admin/upload")
public class UploadController {

    @Value("${upload.path:./uploads}")
    private String uploadPath;

    @Value("${upload.url:http://localhost:8088/uploads}")
    private String uploadUrl;

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

            long fileSize = file.getSize();
            if (fileSize > 2 * 1024 * 1024) {
                return R.error("文件大小不能超过2MB");
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String fileName = UUID.randomUUID().toString().replace("-", "") + extension;

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
