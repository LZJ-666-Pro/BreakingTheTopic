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
