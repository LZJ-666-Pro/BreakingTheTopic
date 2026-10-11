-- ============================================================
-- 16. 题库分组标签（分类标签）体系
-- 依赖：02_poti_question.sql（category 表）
-- 说明：
--   category_group      题库分组标签（小程序题库页顶部筛选：热门/后端/前端/...）
--   category_group_rel  分类与分组标签的多对多关联
--   hidden 为特殊分组：挂在该分组下的分类不在题库宫格展示
-- ============================================================
USE poti_question;

CREATE TABLE IF NOT EXISTS `category_group` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(50) NOT NULL COMMENT '分组名称（页面显示）',
  `group_key` varchar(50) NOT NULL COMMENT '分组标识（程序用，如 backend）',
  `sort` int DEFAULT '0' COMMENT '排序（越小越靠前）',
  `status` tinyint DEFAULT '1' COMMENT '状态：1启用 0停用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0未删除，1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_key` (`group_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='题库分组标签表';

CREATE TABLE IF NOT EXISTS `category_group_rel` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `group_id` bigint NOT NULL COMMENT '分组标签ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cat_group` (`category_id`, `group_id`),
  KEY `idx_group_id` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='分类-分组标签关联表';

-- ------------------------------------------------------------
-- 初始分组标签（与小程序原硬编码一致）
-- ------------------------------------------------------------
INSERT INTO `category_group` (`name`, `group_key`, `sort`) VALUES
('热门', 'hot', 1),
('后端', 'backend', 2),
('前端', 'frontend', 3),
('人工智能', 'ai', 4),
('大数据', 'bigdata', 5),
('测试', 'test', 6),
('基础学科', 'basic', 7),
('面试', 'interview', 8),
('移动开发', 'mobile', 9),
('游戏开发', 'game', 10),
('运维', 'ops', 11),
('安全', 'security', 12),
('区块链', 'blockchain', 13),
('Java', 'java', 14),
('C/C++', 'cpp', 15),
('Go', 'go', 16),
('Python', 'python', 17),
('C#.NET', 'csharp', 18),
('PHP', 'php', 19),
('隐藏', 'hidden', 99)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `sort` = VALUES(`sort`);

-- ------------------------------------------------------------
-- 迁移原前端 categoryTags 硬编码映射（仅库中存在的分类）
-- ------------------------------------------------------------
INSERT INTO `category_group_rel` (`category_id`, `group_id`)
SELECT c.id, g.id FROM category c, category_group g
WHERE g.group_key = 'hot' AND c.deleted = 0 AND c.name IN
('Java','Python','MySQL','Redis','Spring','Go','JavaScript','数据结构','Vue','React','算法面试','系统设计');

INSERT INTO `category_group_rel` (`category_id`, `group_id`)
SELECT c.id, g.id FROM category c, category_group g
WHERE g.group_key = 'backend' AND c.deleted = 0 AND c.name IN
('Java','Python','Go','JavaScript','PHP','MySQL','Redis','MongoDB','PostgreSQL','Spring','MyBatis','Dubbo','Netty','MQ','Nginx','Zookeeper','微服务','Docker','Kubernetes','Node.js','数据结构','操作系统','计算机网络','计算机组成','编译原理','Web安全','密码学','Linux','Git','系统设计');

INSERT INTO `category_group_rel` (`category_id`, `group_id`)
SELECT c.id, g.id FROM category c, category_group g
WHERE g.group_key = 'frontend' AND c.deleted = 0 AND c.name IN
('JavaScript','Vue','React','Node.js','TypeScript');

INSERT INTO `category_group_rel` (`category_id`, `group_id`)
SELECT c.id, g.id FROM category c, category_group g
WHERE g.group_key = 'ai' AND c.deleted = 0 AND c.name IN
('Python','机器学习','深度学习','NLP','计算机视觉');

INSERT INTO `category_group_rel` (`category_id`, `group_id`)
SELECT c.id, g.id FROM category c, category_group g
WHERE g.group_key = 'bigdata' AND c.deleted = 0 AND c.name IN
('Hadoop','Spark','Flink','数据仓库');

INSERT INTO `category_group_rel` (`category_id`, `group_id`)
SELECT c.id, g.id FROM category c, category_group g
WHERE g.group_key = 'test' AND c.deleted = 0 AND c.name IN ('软件测试');

INSERT INTO `category_group_rel` (`category_id`, `group_id`)
SELECT c.id, g.id FROM category c, category_group g
WHERE g.group_key = 'basic' AND c.deleted = 0 AND c.name IN
('数据结构','操作系统','计算机网络','计算机组成','编译原理','高等数学','线性代数','概率论','离散数学');

INSERT INTO `category_group_rel` (`category_id`, `group_id`)
SELECT c.id, g.id FROM category c, category_group g
WHERE g.group_key = 'interview' AND c.deleted = 0 AND c.name IN
('Java','MySQL','Redis','Spring','MyBatis','Nginx','微服务','数据结构','操作系统','计算机网络','Web安全','Linux','算法面试','系统设计','HR面试','行为面试');

INSERT INTO `category_group_rel` (`category_id`, `group_id`)
SELECT c.id, g.id FROM category c, category_group g
WHERE g.group_key = 'mobile' AND c.deleted = 0 AND c.name IN ('React');

INSERT INTO `category_group_rel` (`category_id`, `group_id`)
SELECT c.id, g.id FROM category c, category_group g
WHERE g.group_key = 'game' AND c.deleted = 0 AND c.name IN ('数据结构');

INSERT INTO `category_group_rel` (`category_id`, `group_id`)
SELECT c.id, g.id FROM category c, category_group g
WHERE g.group_key = 'ops' AND c.deleted = 0 AND c.name IN
('Nginx','Zookeeper','Docker','Kubernetes','Linux');

INSERT INTO `category_group_rel` (`category_id`, `group_id`)
SELECT c.id, g.id FROM category c, category_group g
WHERE g.group_key = 'security' AND c.deleted = 0 AND c.name IN ('Web安全','密码学');

INSERT INTO `category_group_rel` (`category_id`, `group_id`)
SELECT c.id, g.id FROM category c, category_group g
WHERE g.group_key = 'blockchain' AND c.deleted = 0 AND c.name IN ('Go','密码学');

INSERT INTO `category_group_rel` (`category_id`, `group_id`)
SELECT c.id, g.id FROM category c, category_group g
WHERE g.group_key = 'java' AND c.deleted = 0 AND c.name IN
('Java','Spring','MyBatis','Dubbo','Netty');

INSERT INTO `category_group_rel` (`category_id`, `group_id`)
SELECT c.id, g.id FROM category c, category_group g
WHERE g.group_key = 'go' AND c.deleted = 0 AND c.name IN ('Go');

INSERT INTO `category_group_rel` (`category_id`, `group_id`)
SELECT c.id, g.id FROM category c, category_group g
WHERE g.group_key = 'python' AND c.deleted = 0 AND c.name IN
('Python','机器学习','深度学习','NLP','计算机视觉');

INSERT INTO `category_group_rel` (`category_id`, `group_id`)
SELECT c.id, g.id FROM category c, category_group g
WHERE g.group_key = 'php' AND c.deleted = 0 AND c.name IN ('PHP');
