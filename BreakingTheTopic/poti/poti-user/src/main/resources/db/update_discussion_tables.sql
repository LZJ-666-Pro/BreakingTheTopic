-- 补丁：题目讨论表（discussion / discussion_like）
-- 背景：讨论功能的建表 SQL 此前未纳入初始化脚本，存量库缺表导致
--       「添加讨论失败」（Table 'poti_user.discussion' doesn't exist）
-- 执行方式：mysql --host=127.0.0.1 --port=3306 --user=root -p < update_discussion_tables.sql
USE poti_user;

-- 题目讨论表
CREATE TABLE IF NOT EXISTS `discussion` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `question_id` BIGINT NOT NULL COMMENT '题目ID',
    `user_id` BIGINT NOT NULL COMMENT '发表用户ID',
    `user_name` VARCHAR(100) DEFAULT NULL COMMENT '用户昵称（冗余）',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '用户头像（冗余）',
    `content` TEXT NOT NULL COMMENT '讨论内容',
    `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
    `reply_to` BIGINT DEFAULT NULL COMMENT '回复的讨论ID',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-正常 0-隐藏',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-删除',
    PRIMARY KEY (`id`),
    KEY `idx_question_id` (`question_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目讨论表';

-- 讨论点赞表
CREATE TABLE IF NOT EXISTS `discussion_like` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `discussion_id` BIGINT NOT NULL COMMENT '讨论ID',
    `user_id` BIGINT NOT NULL COMMENT '点赞用户ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-已赞 1-取消赞',
    PRIMARY KEY (`id`),
    KEY `idx_discussion_user` (`discussion_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='讨论点赞表';
