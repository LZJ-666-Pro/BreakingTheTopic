-- ============================================================
-- 37_competition_vip.sql  竞赛功能（poti_practice 库）+ 会员功能（poti_user 库）
-- 对应需求文档：docs/竞赛与会员功能-需求文档.md
-- ============================================================

USE poti_practice;

-- 竞赛主表：status 仅作发布标记（0草稿 1已发布），进行中/已结束由 start_time/end_time 动态推导
CREATE TABLE IF NOT EXISTS competition (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(100) NOT NULL COMMENT '竞赛名称',
  description VARCHAR(500) DEFAULT '' COMMENT '竞赛说明',
  category_id BIGINT NOT NULL COMMENT '题库分类ID（抽题范围）',
  question_count INT NOT NULL DEFAULT 10 COMMENT '抽题数量',
  duration_minutes INT NOT NULL DEFAULT 30 COMMENT '答题时长（分钟）',
  start_time DATETIME NOT NULL COMMENT '开赛时间',
  end_time DATETIME NOT NULL COMMENT '结束时间',
  score_per_question INT NOT NULL DEFAULT 10 COMMENT '每题分值',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '0草稿 1已发布',
  created_by BIGINT DEFAULT NULL COMMENT '创建管理员ID',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_status_start (status, start_time),
  KEY idx_category (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='答题竞赛';

-- 竞赛报名表
CREATE TABLE IF NOT EXISTS competition_signup (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  competition_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_comp_user (competition_id, user_id),
  KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='竞赛报名';

-- 竞赛作答记录表：开赛即生成（题库快照），交卷补全判分字段
CREATE TABLE IF NOT EXISTS competition_record (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  competition_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  question_ids JSON NOT NULL COMMENT '开赛时抽到的题目ID快照',
  answers JSON DEFAULT NULL COMMENT '用户提交答案 [{questionId,answer}]',
  score INT DEFAULT NULL COMMENT '得分',
  correct_count INT DEFAULT NULL COMMENT '答对题数',
  cost_seconds INT DEFAULT NULL COMMENT '用时（秒）',
  submit_time DATETIME DEFAULT NULL COMMENT '交卷时间',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_comp_user (competition_id, user_id),
  KEY idx_score (competition_id, score, cost_seconds)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='竞赛作答记录';

-- ============================================================

USE poti_user;

-- 会员字段（vip_level 查询时按 vip_expire_time 动态校验，不依赖定时任务）
ALTER TABLE `user`
  ADD COLUMN vip_level TINYINT NOT NULL DEFAULT 0 COMMENT '会员等级 0普通 1VIP' AFTER status,
  ADD COLUMN vip_expire_time DATETIME DEFAULT NULL COMMENT '会员到期时间' AFTER vip_level;

-- 会员订单表（模拟支付 simulated=1；真实微信支付 pay_status 由回调/查单置 1 并记录 transaction_id）
CREATE TABLE IF NOT EXISTS vip_order (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  order_no VARCHAR(64) NOT NULL COMMENT '订单号',
  amount DECIMAL(10,2) NOT NULL COMMENT '支付金额（元）',
  months INT NOT NULL DEFAULT 1 COMMENT '时长（月）',
  pay_status TINYINT NOT NULL DEFAULT 1 COMMENT '支付状态 0待支付 1成功',
  simulated TINYINT NOT NULL DEFAULT 1 COMMENT '是否模拟支付',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  pay_time DATETIME DEFAULT NULL,
  transaction_id VARCHAR(64) DEFAULT NULL COMMENT '微信支付订单号',
  UNIQUE KEY uk_order_no (order_no),
  KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员订单';

-- ============================================================
-- poti_question 库随机抽题复用现有 question 表，无需建表
-- ============================================================
