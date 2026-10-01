SET NAMES utf8mb4;
USE `poti_practice`;
-- 练习记录表
CREATE TABLE IF NOT EXISTS `practice_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `question_id` bigint NOT NULL COMMENT '题目ID',
  `user_answer` varchar(10) DEFAULT NULL COMMENT '用户答案',
  `is_correct` tinyint(1) DEFAULT '0' COMMENT '是否正确：0错误，1正确',
  `spend_seconds` int DEFAULT '0' COMMENT '答题用时(秒)',
  `practice_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '练习时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0未删除，1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_question_id` (`question_id`),
  KEY `idx_practice_time` (`practice_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='练习记录表';
