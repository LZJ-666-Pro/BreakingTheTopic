SET NAMES utf8mb4;
USE `poti_interview`;
-- 面试记录表
CREATE TABLE IF NOT EXISTS `interview_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `title` VARCHAR(100) DEFAULT NULL COMMENT '面试标题',
  `type` VARCHAR(50) DEFAULT NULL COMMENT '面试类型',
  `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
  `total_questions` INT DEFAULT 0 COMMENT '总题数',
  `correct_count` INT DEFAULT 0 COMMENT '正确数',
  `spend_seconds` INT DEFAULT 0 COMMENT '花费秒数',
  `score` DECIMAL(5,2) DEFAULT 0.00 COMMENT '得分',
  `ai_feedback` TEXT DEFAULT NULL COMMENT 'AI综合评语',
  `status` INT DEFAULT 1 COMMENT '状态：1-进行中，2-已完成',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` INT DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面试记录表';

-- 面试题目详情表
CREATE TABLE IF NOT EXISTS `interview_question_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `interview_id` BIGINT NOT NULL COMMENT '面试记录ID',
  `question_id` BIGINT NOT NULL COMMENT '题目ID',
  `question_content` TEXT DEFAULT NULL COMMENT '题目内容（冗余存储）',
  `reference_answer` TEXT DEFAULT NULL COMMENT '参考答案解析（冗余存储）',
  `correct_answer` VARCHAR(50) DEFAULT NULL COMMENT '正确答案',
  `user_answer` TEXT DEFAULT NULL COMMENT '用户答案',
  `audio_url` VARCHAR(255) DEFAULT NULL COMMENT '语音文件URL',
  `is_correct` INT DEFAULT 0 COMMENT '是否正确：0-错误，1-正确',
  `score` DECIMAL(5,2) DEFAULT 0.00 COMMENT '得分',
  `ai_comment` TEXT DEFAULT NULL COMMENT 'AI评语',
  `answer_time_seconds` INT DEFAULT 0 COMMENT '答题用时（秒）',
  `order_num` INT DEFAULT 0 COMMENT '题目序号',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_interview_id` (`interview_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面试题目详情表';

-- 补丁:题目选项字段
ALTER TABLE interview_question_detail ADD COLUMN options TEXT COMMENT '题目选项' AFTER question_content;