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
