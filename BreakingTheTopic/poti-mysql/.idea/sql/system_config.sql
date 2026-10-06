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

-- 初始化联系方式配置（个人项目：值留空即前端自动隐藏对应条目）
-- 有真实联系方式后，把对应的值填上再执行，或直接 UPDATE：
--   UPDATE system_config SET config_value='你的微信号' WHERE config_key='contact_wechat';
INSERT INTO `system_config` (`config_key`, `config_value`, `config_desc`) VALUES
('contact_wechat', '', '开发者微信'),
('contact_email', '', '联系邮箱'),
('contact_qq', '', '交流QQ群'),
('contact_work_time', '', '回复时间'),
('group_qq_1', '', 'QQ群1（格式：群名称:群号）'),
('group_qq_2', '', 'QQ群2（格式：群名称:群号）'),
('group_qq_3', '', 'QQ群3（格式：群名称:群号）'),
('group_qq_4', '', 'QQ群4（格式：群名称:群号）');
