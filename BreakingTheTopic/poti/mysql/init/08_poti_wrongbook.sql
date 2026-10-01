SET NAMES utf8mb4;
-- poti_wrongbook 库建表脚本
-- 依据 poti-wrongbook 实体类(Wrongbook.java)反推生成

USE `poti_wrongbook`;

CREATE TABLE IF NOT EXISTS `wrongbook` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `question_id` bigint NOT NULL COMMENT '题目ID',
  `category_id` bigint DEFAULT NULL COMMENT '分类ID',
  `title` varchar(500) DEFAULT NULL COMMENT '题目标题',
  `type` tinyint DEFAULT '1' COMMENT '题目类型：1单选，2多选',
  `difficulty` tinyint DEFAULT '1' COMMENT '难度：1简单，2中等，3困难',
  `wrong_count` int DEFAULT '1' COMMENT '错误次数',
  `last_wrong_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '最近错误时间',
  `review_times` int DEFAULT '0' COMMENT '复习次数',
  `last_review_time` datetime DEFAULT NULL COMMENT '最近复习时间',
  `mastered` tinyint DEFAULT '0' COMMENT '是否掌握：0未掌握，1已掌握',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0未删除，1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_question_id` (`question_id`),
  KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='错题本表';
