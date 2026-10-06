-- 特训营功能建表补丁（存量数据库手动执行）
-- 执行范围：poti_interview 库
USE `poti_interview`;

-- 特训营报名表
CREATE TABLE IF NOT EXISTS `camp_member` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `camp_id` VARCHAR(50) NOT NULL COMMENT '训练营标识',
  `status` INT DEFAULT 1 COMMENT '状态：1-进行中，2-已结营',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` INT DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_camp` (`user_id`, `camp_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='特训营报名表';

-- 特训营打卡表
CREATE TABLE IF NOT EXISTS `camp_checkin` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `camp_id` VARCHAR(50) NOT NULL COMMENT '训练营标识',
  `day_num` INT NOT NULL COMMENT '打卡对应的训练天数',
  `checkin_date` DATE NOT NULL COMMENT '打卡日期',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_camp_day` (`user_id`, `camp_id`, `day_num`),
  KEY `idx_user_camp_date` (`user_id`, `camp_id`, `checkin_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='特训营打卡表';
