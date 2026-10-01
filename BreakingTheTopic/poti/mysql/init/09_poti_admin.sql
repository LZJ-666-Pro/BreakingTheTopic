SET NAMES utf8mb4;
-- poti_admin 管理后台初始化
-- 创建管理员数据库
CREATE DATABASE IF NOT EXISTS `poti_admin` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `poti_admin`;

-- 创建管理员表
CREATE TABLE IF NOT EXISTS `sys_admin` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码(BCrypt加密)', 
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `status` tinyint DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `role` tinyint DEFAULT 1 COMMENT '角色：1-普通管理员，2-超级管理员',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录IP',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统管理员表';

-- 插入默认管理员账号
-- 密码为 admin123，使用BCrypt加密
INSERT INTO `sys_admin` (`username`, `password`, `nickname`, `role`, `status`) VALUES
('admin', '$2a$10$iWgLSXfJRtXN5XdNpVBm.uIaVRelfHuj4Y52TkWQDY1ovjHRXVuoC', '超级管理员', 2, 1);

-- 系统配置表（管理后台“系统设置”页使用，Config 实体 @TableName("sys_config")）
CREATE TABLE IF NOT EXISTS `sys_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `config_value` varchar(500) DEFAULT NULL COMMENT '配置值',
  `config_name` varchar(100) DEFAULT NULL COMMENT '配置名称',
  `description` varchar(200) DEFAULT NULL COMMENT '配置描述',
  `status` tinyint DEFAULT '1' COMMENT '状态：1启用，0禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0未删除，1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

INSERT INTO `sys_config` (`config_key`, `config_value`, `config_name`, `description`, `status`) VALUES
('site_name', 'POTI面试刷题平台', '网站名称', '网站名称', 1),
('site_description', '专注于程序员面试的刷题平台', '网站描述', '网站描述', 1),
('site_keywords', 'Java,前端,面试,刷题', '网站关键词', 'SEO关键词，逗号分隔', 1),
('site_icp', '', 'ICP备案号', 'ICP备案号', 1),
('site_copyright', '', '版权信息', '页脚版权信息', 1),
('upload_max_size', '10', '上传大小限制', '上传文件大小限制(MB)', 1),
('upload_allow_types', 'jpg,jpeg,png,gif,pdf', '允许上传类型', '允许的文件扩展名，逗号分隔', 1),
('email_smtp_host', '', 'SMTP服务器', '邮件SMTP服务器地址', 1),
('email_smtp_port', '465', 'SMTP端口', '邮件SMTP端口', 1),
('email_smtp_username', '', 'SMTP用户名', '发件邮箱账号', 1),
('email_smtp_password', '', 'SMTP密码', '发件邮箱密码/授权码', 1),
('sms_access_key', '', '短信AccessKey', '短信服务AccessKey', 1),
('sms_access_secret', '', '短信AccessSecret', '短信服务AccessSecret', 1),
('sms_sign_name', '', '短信签名', '短信签名', 1),
('sms_template_code', '', '短信模板CODE', '短信模板CODE', 1);

CREATE TABLE IF NOT EXISTS `ai_task` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    `category_id` BIGINT COMMENT '分类ID',
    `category_name` VARCHAR(100) COMMENT '分类名称',
    `total_count` INT NOT NULL COMMENT '总题目数',
    `completed_count` INT DEFAULT 0 COMMENT '已完成数量',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0待处理 1处理中 2已完成 3失败 4已取消',
    `difficulty` VARCHAR(20) COMMENT '难度',
    `topic` VARCHAR(200) COMMENT '主题',
    `additional_requirements` TEXT COMMENT '额外要求',
    `error_message` TEXT COMMENT '错误信息',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI生成任务表';
