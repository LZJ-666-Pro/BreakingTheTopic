-- 重置签到表
-- 方式1：删除所有签到记录（保留表结构）
TRUNCATE TABLE checkin_record;

-- 方式2：删除所有签到记录（使用DELETE，可以回滚）
-- DELETE FROM checkin_record;

-- 方式3：删除并重建表（完全重置）
-- DROP TABLE IF EXISTS checkin_record;
-- CREATE TABLE checkin_record (
--   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
--   `user_id` bigint NOT NULL COMMENT '用户ID',
--   `checkin_date` date NOT NULL COMMENT '签到日期',
--   `checkin_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '签到时间',
--   `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
--   `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0未删除，1已删除',
--   PRIMARY KEY (`id`),
--   UNIQUE KEY `uk_user_date` (`user_id`, `checkin_date`),
--   KEY `idx_user_id` (`user_id`),
--   KEY `idx_checkin_date` (`checkin_date`)
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到记录表';

-- 查看重置后的表
SELECT * FROM checkin_record;
