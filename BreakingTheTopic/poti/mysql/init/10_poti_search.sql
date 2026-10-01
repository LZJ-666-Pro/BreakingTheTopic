SET NAMES utf8mb4;
-- poti-search 库建表脚本(poti-user 服务的 search 数据源使用)
CREATE DATABASE IF NOT EXISTS `poti-search` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `poti-search`;

-- 搜索记录表(热门搜索统计)
CREATE TABLE IF NOT EXISTS `search_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `keyword` varchar(255) NOT NULL COMMENT '搜索关键词',
  `user_id` bigint DEFAULT NULL COMMENT '最近搜索用户ID',
  `search_count` int NOT NULL DEFAULT '1' COMMENT '搜索次数',
  `user_count` int NOT NULL DEFAULT '1' COMMENT '搜索用户数',
  `last_search_time` datetime DEFAULT NULL COMMENT '最近搜索时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_keyword` (`keyword`),
  KEY `idx_search_count` (`search_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='搜索记录表';
