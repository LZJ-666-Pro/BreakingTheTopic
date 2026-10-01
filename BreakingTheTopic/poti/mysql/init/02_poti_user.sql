SET NAMES utf8mb4;
USE `poti_user`;
-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `openid` varchar(100) DEFAULT NULL COMMENT '微信openid',
  `unionid` varchar(100) DEFAULT NULL COMMENT '微信unionid',
  `nickname` varchar(100) DEFAULT NULL COMMENT '昵称',
  `avatar_url` varchar(500) DEFAULT NULL COMMENT '头像URL',
  `gender` tinyint DEFAULT '0' COMMENT '性别：0未知，1男，2女',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `unique_id` varchar(50) DEFAULT NULL COMMENT '唯一ID，用于搜索添加好友',
  `status` tinyint DEFAULT '1' COMMENT '状态：0禁用，1正常',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0未删除，1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`),
  UNIQUE KEY `uk_unique_id` (`unique_id`),
  KEY `idx_unionid` (`unionid`),
  KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 好友申请表
CREATE TABLE IF NOT EXISTS `friend_request` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '申请人ID',
  `friend_id` bigint NOT NULL COMMENT '被申请人ID',
  `message` varchar(200) DEFAULT NULL COMMENT '申请消息',
  `status` tinyint DEFAULT '0' COMMENT '状态：0待处理，1已接受，2已拒绝',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0未删除，1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_friend_id` (`friend_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友申请表';

-- 会话表
CREATE TABLE IF NOT EXISTS `conversation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user1_id` bigint NOT NULL COMMENT '用户1ID',
  `user2_id` bigint NOT NULL COMMENT '用户2ID',
  `last_message` varchar(500) DEFAULT NULL COMMENT '最后一条消息',
  `last_message_time` datetime DEFAULT NULL COMMENT '最后一条消息时间',
  `user1_unread` int DEFAULT '0' COMMENT '用户1未读消息数',
  `user2_unread` int DEFAULT '0' COMMENT '用户2未读消息数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0未删除，1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_users` (`user1_id`, `user2_id`),
  KEY `idx_user1_id` (`user1_id`),
  KEY `idx_user2_id` (`user2_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话表';

-- 消息表
CREATE TABLE IF NOT EXISTS `message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `conversation_id` bigint NOT NULL COMMENT '会话ID',
  `sender_id` bigint NOT NULL COMMENT '发送者ID',
  `receiver_id` bigint NOT NULL COMMENT '接收者ID',
  `content` varchar(1000) NOT NULL COMMENT '消息内容',
  `type` tinyint DEFAULT '1' COMMENT '消息类型：1文本，2图片，3语音',
  `status` tinyint DEFAULT '1' COMMENT '状态：1已发送，2已读',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0未删除，1已删除',
  PRIMARY KEY (`id`),
  KEY `idx_conversation_id` (`conversation_id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

-- 用户统计表
CREATE TABLE IF NOT EXISTS `user_stats` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `total_questions` int DEFAULT '0' COMMENT '累计刷题数',
  `correct_questions` int DEFAULT '0' COMMENT '正确题目数',
  `wrong_questions` int DEFAULT '0' COMMENT '错误题目数',
  `total_time` int DEFAULT '0' COMMENT '累计刷题时长(秒)',
  `today_questions` int DEFAULT '0' COMMENT '今日刷题数',
  `today_correct` int DEFAULT '0' COMMENT '今日正确数',
  `today_time` int DEFAULT '0' COMMENT '今日刷题时长(秒)',
  `consecutive_days` int DEFAULT '0' COMMENT '连续签到天数',
  `last_practice_date` date DEFAULT NULL COMMENT '最后刷题日期',
  `total_points` int DEFAULT '0' COMMENT '总积分',
  `achievement_points` int DEFAULT '0' COMMENT '成就点数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0未删除，1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_total_questions` (`total_questions`),
  KEY `idx_today_questions` (`today_questions`),
  KEY `idx_consecutive_days` (`consecutive_days`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户统计表';

-- 成就配置表
CREATE TABLE IF NOT EXISTS `achievement` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `achievement_code` varchar(50) NOT NULL COMMENT '成就编码',
  `name` varchar(100) NOT NULL COMMENT '成就名称',
  `icon` varchar(50) DEFAULT NULL COMMENT '成就图标',
  `description` varchar(500) DEFAULT NULL COMMENT '成就描述',
  `category` varchar(50) NOT NULL COMMENT '成就分类：practice-刷题成就，social-社交成就，special-特殊成就',
  `type` varchar(50) NOT NULL COMMENT '成就类型',
  `condition_value` int DEFAULT '0' COMMENT '触发条件值',
  `reward_points` int DEFAULT '0' COMMENT '奖励成就点数',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0未删除，1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_achievement_code` (`achievement_code`),
  KEY `idx_category` (`category`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成就配置表';

-- 用户成就表
CREATE TABLE IF NOT EXISTS `user_achievement` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `achievement_id` bigint NOT NULL COMMENT '成就ID',
  `current_progress` int DEFAULT '0' COMMENT '当前进度',
  `is_unlocked` tinyint DEFAULT '0' COMMENT '是否解锁：0未解锁，1已解锁',
  `unlock_time` datetime DEFAULT NULL COMMENT '解锁时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0未删除，1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_achievement` (`user_id`, `achievement_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_achievement_id` (`achievement_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户成就表';

-- 签到记录表
CREATE TABLE IF NOT EXISTS `checkin_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `checkin_date` date NOT NULL COMMENT '签到日期',
  `checkin_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '签到时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0未删除，1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`, `checkin_date`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_checkin_date` (`checkin_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到记录表';

-- 好友关系表
CREATE TABLE IF NOT EXISTS `friendship` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `friend_id` bigint NOT NULL COMMENT '好友ID',
  `status` tinyint DEFAULT '1' COMMENT '状态：0待确认，1已接受，2已拒绝，3已删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0未删除，1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_friend` (`user_id`, `friend_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_friend_id` (`friend_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友关系表';

-- 补丁:密码字段
ALTER TABLE `user` ADD COLUMN password VARCHAR(255) DEFAULT NULL COMMENT '密码' AFTER email;
-- 补丁:连续签到天数
ALTER TABLE checkin_record ADD COLUMN streak_days INT DEFAULT 1 COMMENT '连续签到天数' AFTER checkin_time;
-- 插入成就配置数据
INSERT INTO `achievement` (`achievement_code`, `name`, `icon`, `description`, `category`, `type`, `condition_value`, `reward_points`, `sort_order`) VALUES
('FIRST_BLOOD', '初出茅庐', '🌱', '完成第一道题目', 'practice', 'TOTAL_ANSWER', 1, 10, 1),
('PRACTICE_10', '小试牛刀', '⚔️', '累计刷题10道', 'practice', 'TOTAL_ANSWER', 10, 20, 2),
('PRACTICE_50', '初露锋芒', '🗡️', '累计刷题50道', 'practice', 'TOTAL_ANSWER', 50, 50, 3),
('PRACTICE_100', '锋芒毕露', '🗡️', '累计刷题100道', 'practice', 'TOTAL_ANSWER', 100, 100, 4),
('PRACTICE_500', '刷题达人', '📚', '累计刷题500道', 'practice', 'TOTAL_ANSWER', 500, 200, 5),
('PRACTICE_1000', '题海战术', '🌊', '累计刷题1000道', 'practice', 'TOTAL_ANSWER', 1000, 500, 6),
('PERFECT_SCORE', '百分百', '💯', '单次刷题正确率达到100%', 'practice', 'PERFECT_SCORE', 100, 50, 7),
('CONSECUTIVE_10', '连胜王者', '👑', '连续答对10道题', 'practice', 'CONSECUTIVE_CORRECT', 10, 100, 8),
('FAVORITE_10', '收藏家', '⭐', '收藏10道题目', 'social', 'TOTAL_FAVORITE', 10, 30, 9),
('NO_WRONG', '错题终结者', '✅', '错题本清空', 'practice', 'NO_WRONG_ANSWERS', 0, 50, 10),
('MORNING_BIRD', '早起鸟', '🌅', '早上6-8点刷题', 'special', 'MORNING_PRACTICE', 1, 20, 11),
('NIGHT_OWL', '夜猫子', '🦉', '晚上22-24点刷题', 'special', 'NIGHT_PRACTICE', 1, 20, 12),
('CHECKIN_7', '坚持不懈', '💪', '连续签到7天', 'special', 'CONSECUTIVE_CHECKIN', 7, 50, 13),
('CHECKIN_30', '月度之星', '🌟', '连续签到30天', 'special', 'CONSECUTIVE_CHECKIN', 30, 200, 14),
('ALL_CATEGORIES', '全能选手', '🏆', '完成所有分类的题目', 'practice', 'ALL_CATEGORIES', 10, 300, 15),
('JAVA_MASTER', 'Java大师', '☕', '完成Java所有题目', 'practice', 'CATEGORY_COMPLETE', 500, 100, 16);

-- 插入测试用户数据
INSERT IGNORE INTO `user` (`id`, `openid`, `nickname`, `avatar_url`, `gender`, `status`) VALUES
(1, 'openid_001', '张三', 'https://example.com/avatar1.jpg', 1, 1),
(2, 'openid_002', '李四', 'https://example.com/avatar2.jpg', 1, 1),
(3, 'openid_003', '王五', 'https://example.com/avatar3.jpg', 2, 1),
(4, 'openid_004', '赵六', 'https://example.com/avatar4.jpg', 1, 1),
(5, 'openid_005', '钱七', 'https://example.com/avatar5.jpg', 2, 1),
(6, 'openid_006', '孙八', 'https://example.com/avatar6.jpg', 1, 1),
(7, 'openid_007', '周九', 'https://example.com/avatar7.jpg', 2, 1),
(8, 'openid_008', '吴十', 'https://example.com/avatar8.jpg', 1, 1);

-- 插入用户统计数据
INSERT IGNORE INTO `user_stats` (`user_id`, `total_questions`, `correct_questions`, `wrong_questions`, `total_time`, `today_questions`, `today_correct`, `today_time`, `consecutive_days`, `last_practice_date`, `total_points`, `achievement_points`) VALUES
(1, 150, 120, 30, 18000, 15, 12, 1800, 7, CURDATE(), 500, 100),
(2, 200, 160, 40, 24000, 20, 16, 2400, 5, CURDATE(), 600, 120),
(3, 100, 80, 20, 12000, 10, 8, 1200, 3, CURDATE(), 400, 80),
(4, 300, 240, 60, 36000, 30, 24, 3600, 10, CURDATE(), 800, 150),
(5, 80, 64, 16, 9600, 8, 6, 960, 2, CURDATE(), 300, 60),
(6, 250, 200, 50, 30000, 25, 20, 3000, 8, CURDATE(), 700, 130),
(7, 120, 96, 24, 14400, 12, 10, 1440, 4, CURDATE(), 450, 90),
(8, 180, 144, 36, 21600, 18, 14, 2160, 6, CURDATE(), 550, 110);

-- 插入好友关系数据（用户1的好友）
INSERT IGNORE INTO `friendship` (`user_id`, `friend_id`, `status`) VALUES
(1, 2, 1),
(1, 3, 1),
(1, 4, 1),
(1, 5, 1),
(2, 1, 1),
(3, 1, 1),
(4, 1, 1),
(5, 1, 1);

-- 意见反馈表(旧环境手动建表,依据 Feedback 实体反推补录)
CREATE TABLE IF NOT EXISTS `feedback` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `nickname` varchar(64) DEFAULT NULL COMMENT '用户昵称(冗余)',
  `type` varchar(20) DEFAULT NULL COMMENT '反馈类型',
  `content` text COMMENT '反馈内容',
  `contact` varchar(100) DEFAULT NULL COMMENT '联系方式',
  `images` text COMMENT '反馈图片URL,多个逗号分隔',
  `status` tinyint DEFAULT '0' COMMENT '状态：0待处理，1已回复',
  `reply` text COMMENT '管理员回复内容',
  `reply_time` datetime DEFAULT NULL COMMENT '回复时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='意见反馈表';
