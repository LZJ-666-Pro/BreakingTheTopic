-- 每日一题答题记录：用户每日一题「答完即不再展示」的数据支撑
-- 用户维度一天一条（换设备/清缓存不影响，后端持久判定）
CREATE DATABASE IF NOT EXISTS poti_question DEFAULT CHARACTER SET utf8mb4;
USE poti_question;

CREATE TABLE IF NOT EXISTS daily_question_answer (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  question_id BIGINT NOT NULL COMMENT '每日一题对应的题目ID',
  answer_date DATE NOT NULL COMMENT '作答日期（当日每日一题归属日）',
  is_correct TINYINT DEFAULT 0 COMMENT '是否答对：1对 0错',
  answer_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '作答时间',
  UNIQUE KEY uk_user_date (user_id, answer_date),
  KEY idx_answer_date (answer_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日一题答题记录表';
