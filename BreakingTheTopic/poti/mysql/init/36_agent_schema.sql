-- ============================================================
-- 36_agent_schema.sql
-- AI 助手（poti-agent）独立库：会话 / 消息 / 工具调度日志
-- ============================================================

CREATE DATABASE IF NOT EXISTS `poti_agent` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `poti_agent`;

-- 会话表：一个用户多个会话，P0 不做手动删除，7 天外消息由定时清理（后续）
CREATE TABLE IF NOT EXISTS `agent_session` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `user_id` BIGINT NOT NULL COMMENT '所属用户ID',
  `title` VARCHAR(100) NOT NULL DEFAULT '新对话' COMMENT '会话标题（首条提问截断）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后活跃时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`, `update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI助手会话表';

-- 消息表：sources/actions/tool_trace 以 JSON 文本存储，解析失败视为无该字段
CREATE TABLE IF NOT EXISTS `agent_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `session_id` BIGINT NOT NULL COMMENT '会话ID',
  `user_id` BIGINT NOT NULL COMMENT '所属用户ID（冗余，供限额统计）',
  `role` VARCHAR(16) NOT NULL COMMENT '角色：user / assistant',
  `content` MEDIUMTEXT COMMENT '消息内容',
  `sources` TEXT COMMENT '引用标注 JSON 数组：["学习统计","错题分布"]',
  `actions` TEXT COMMENT '动作卡片 JSON 数组：[{"type":"PRACTICE_CATEGORY","categoryId":12,"label":"..."}]',
  `tool_trace` TEXT COMMENT '本次回答的工具调用轨迹 JSON：[{"tool":"get_checkin_status","costMs":120}]',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_session` (`session_id`, `id`),
  KEY `idx_user_time` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI助手消息表';

-- 工具调度日志表：LLM 每次执行业务工具记录一条，用于溯源与成本观测
CREATE TABLE IF NOT EXISTS `agent_tool_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '触发用户',
  `session_id` BIGINT DEFAULT NULL COMMENT '会话ID',
  `tool_name` VARCHAR(64) NOT NULL COMMENT '工具名',
  `params` TEXT COMMENT '工具入参 JSON',
  `cost_ms` INT NOT NULL DEFAULT 0 COMMENT '执行耗时毫秒',
  `round_no` INT NOT NULL DEFAULT 1 COMMENT '第几轮工具调用',
  `success` TINYINT NOT NULL DEFAULT 1 COMMENT '是否成功：1是 0否',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_time` (`user_id`, `create_time`),
  KEY `idx_tool` (`tool_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI助手工具调度日志表';

-- LLM 内部对话记忆表（LangChain4j ChatMemoryStore 落库，存序列化后的完整对话含工具消息，
-- 与面向用户的 agent_message 分离：重启不丢上下文，用户侧历史仍读 agent_message）
CREATE TABLE IF NOT EXISTS `agent_chat_memory` (
  `memory_id` VARCHAR(64) NOT NULL COMMENT '记忆ID（=会话ID字符串）',
  `messages` MEDIUMTEXT COMMENT 'LangChain4j 序列化消息列表 JSON',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`memory_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI助手LLM内部记忆表';
