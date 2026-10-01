SET NAMES utf8mb4;
-- poti_question 库建表脚本
-- 依据 poti-question 实体类(Question.java / Category.java)与初始数据列反推生成

USE `poti_question`;

-- 题目表
CREATE TABLE IF NOT EXISTS `question` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `title` varchar(500) NOT NULL COMMENT '题目标题',
  `content` text COMMENT '题目内容',
  `type` tinyint DEFAULT '1' COMMENT '题目类型：1单选，2多选',
  `difficulty` tinyint DEFAULT '1' COMMENT '难度：1简单，2中等，3困难',
  `options` text COMMENT '选项JSON',
  `answer` varchar(50) DEFAULT NULL COMMENT '答案',
  `analysis` text COMMENT '解析',
  `tags` varchar(255) DEFAULT NULL COMMENT '标签，逗号分隔',
  `view_count` int DEFAULT '0' COMMENT '浏览数',
  `favorite_count` int DEFAULT '0' COMMENT '收藏数',
  `status` tinyint DEFAULT '1' COMMENT '状态：0下架，1上架',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0未删除，1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目表';

-- 题目分类表
CREATE TABLE IF NOT EXISTS `category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '分类名称',
  `parent_id` bigint DEFAULT '0' COMMENT '父分类ID，0为顶级',
  `sort` int DEFAULT '0' COMMENT '排序',
  `icon` varchar(100) DEFAULT NULL COMMENT '图标标识',
  `icon_url` varchar(500) DEFAULT NULL COMMENT '图标URL',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0未删除，1已删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目分类表';
