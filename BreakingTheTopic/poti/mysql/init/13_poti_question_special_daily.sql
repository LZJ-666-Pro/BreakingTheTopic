-- 刷题专题 + 每日一题（poti_question 库）
USE `poti_question`;

-- 专题表
CREATE TABLE IF NOT EXISTS special_topic (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL COMMENT '专题名称',
  cover VARCHAR(255) NOT NULL DEFAULT '' COMMENT '封面图URL',
  description VARCHAR(500) NOT NULL DEFAULT '' COMMENT '简介',
  difficulty TINYINT NOT NULL DEFAULT 2 COMMENT '难度：1简单 2中等 3困难',
  estimated_days INT NOT NULL DEFAULT 7 COMMENT '预计天数',
  unlock_type TINYINT NOT NULL DEFAULT 0 COMMENT '解锁规则：0自由刷 1按顺序解锁',
  sort INT NOT NULL DEFAULT 0 COMMENT '推荐权重，越大越靠前',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '1上架 0下架',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_status (status),
  KEY idx_sort (sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='刷题专题表';

-- 专题-题目关联表（题目在专题内按 sort 排序）
CREATE TABLE IF NOT EXISTS special_question (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  topic_id BIGINT NOT NULL,
  question_id BIGINT NOT NULL,
  sort INT NOT NULL DEFAULT 0 COMMENT '题目顺序，越小越靠前',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_topic_question (topic_id, question_id),
  KEY idx_topic (topic_id, sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='专题题目关联表';

-- 每日一题表（每天一题，日期唯一由应用层保证）
CREATE TABLE IF NOT EXISTS daily_question (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  question_id BIGINT NOT NULL,
  date DATE NOT NULL COMMENT '展示日期',
  reason VARCHAR(200) NOT NULL DEFAULT '' COMMENT '推荐理由',
  hint VARCHAR(200) NOT NULL DEFAULT '' COMMENT '解题提示',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_date (date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='每日一题表';

-- 示例专题（从现有题库挑题）
INSERT INTO special_topic (name, description, difficulty, estimated_days, unlock_type, sort) VALUES
('面试必刷 100 题', '精选面试高频题目，覆盖 Java 基础、并发、JVM、算法等核心考点，带你系统备战技术面试。', 2, 14, 1, 300),
('MySQL 高频考点突击', '索引、事务、锁、主从复制、分库分表——把 MySQL 面试问烂的知识点一次刷透。', 2, 7, 0, 200),
('算法入门：从零开始刷题', '适合刚接触算法的同学：数组、链表、哈希、排序、二分，配套精选题单循序渐进。', 1, 10, 1, 100);

INSERT INTO special_question (topic_id, question_id, sort)
SELECT 1, id, ROW_NUMBER() OVER (ORDER BY id) - 1 FROM question
WHERE deleted = 0 AND status = 1 AND (tags LIKE '%面试%' OR category_id = 20) ORDER BY id LIMIT 20;

INSERT INTO special_question (topic_id, question_id, sort)
SELECT 2, id, ROW_NUMBER() OVER (ORDER BY id) - 1 FROM question
WHERE deleted = 0 AND status = 1 AND tags LIKE 'MySQL%' ORDER BY id LIMIT 10;

INSERT INTO special_question (topic_id, question_id, sort)
SELECT 3, id, ROW_NUMBER() OVER (ORDER BY id) - 1 FROM question
WHERE deleted = 0 AND status = 1 AND (category_id = 34 OR tags LIKE '算法%') ORDER BY id LIMIT 10;

-- 示例每日一题（今天起连续 3 天）
INSERT INTO daily_question (question_id, date, reason, hint)
SELECT id, CURDATE(), '经典基础题，热热身', '先想一想常见的实现方式，再看选项排除' FROM question WHERE deleted = 0 AND status = 1 ORDER BY id LIMIT 1;
INSERT INTO daily_question (question_id, date, reason, hint)
SELECT id, CURDATE() + INTERVAL 1 DAY, '结合实际开发的常见坑点', '注意关键字的作用范围' FROM question WHERE deleted = 0 AND status = 1 ORDER BY id LIMIT 1 OFFSET 1;
INSERT INTO daily_question (question_id, date, reason, hint)
SELECT id, CURDATE() + INTERVAL 2 DAY, '进阶挑战，做对算我输', '从概念定义出发逐项分析' FROM question WHERE deleted = 0 AND status = 1 ORDER BY id LIMIT 1 OFFSET 2;
