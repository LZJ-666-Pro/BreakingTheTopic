-- 在 poti_user 数据库中执行
USE poti_user;

-- 系统配置表
CREATE TABLE IF NOT EXISTS `system_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value` VARCHAR(500) DEFAULT NULL COMMENT '配置值',
    `config_desc` VARCHAR(200) DEFAULT NULL COMMENT '配置描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 初始化联系方式配置
INSERT INTO `system_config` (`config_key`, `config_value`, `config_desc`) VALUES
('contact_wechat', 'poti_helper', '官方微信'),
('contact_email', 'support@poti.com', '官方邮箱'),
('contact_qq', '123456789', 'QQ群'),
('contact_weibo', '@面试鸭官方', '官方微博'),
('contact_work_time', '工作日 9:00-18:00', '工作时间'),
('group_qq_1', '面试鸭官方群:123456789', 'QQ群1'),
('group_qq_2', 'Java面试群:234567890', 'QQ群2'),
('group_qq_3', '前端面试群:345678901', 'QQ群3'),
('group_qq_4', '校招求职群:456789012', 'QQ群4');
