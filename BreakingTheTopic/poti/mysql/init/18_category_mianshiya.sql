-- =====================================================================
-- 18_category_mianshiya.sql
-- 参考面试鸭（mianshiya.com）题库体系调整分类：新增细分题库分类并挂载
-- 到现有分组；「MQ」改名「消息队列」对齐面试鸭命名。
-- 本脚本可重复执行（幂等）。
-- =====================================================================
USE poti_question;

-- ---------------------------------------------------------------------
-- 一、新增细分分类（参考面试鸭题库划分）
-- ---------------------------------------------------------------------
DROP TEMPORARY TABLE IF EXISTS tmp_ms_category;
CREATE TEMPORARY TABLE tmp_ms_category (
  name        varchar(100) COLLATE utf8mb4_0900_ai_ci,
  sort        int,
  icon        varchar(100) COLLATE utf8mb4_0900_ai_ci,
  description varchar(255) COLLATE utf8mb4_0900_ai_ci
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO tmp_ms_category (name, sort, icon, description) VALUES
  -- Java 细分（面试鸭：Java 集合/并发/虚拟机）
  ('Java 集合',    15, 'java', 'List/Set/Map 底层实现、ConcurrentHashMap、扩容机制与线程安全'),
  ('Java 并发',    16, 'java', '线程池、锁与同步机制、AQS、CAS、原子类与并发容器'),
  ('Java 虚拟机',  17, 'java', 'JVM 内存区域、垃圾回收、类加载、性能监控与调优'),
  -- Spring 生态细分（面试鸭：SpringBoot/SpringCloud）
  ('SpringBoot',   42, 'spring', '自动配置原理、嵌入式容器、配置管理与监控 Actuator'),
  ('SpringCloud',  43, 'spring', '服务注册发现、负载均衡、熔断限流、网关与配置中心'),
  -- 消息队列与搜索（面试鸭：RabbitMQ/Kafka/ElasticSearch）
  ('RabbitMQ',     32, 'rabbitmq', '消息可靠传递、死信队列、延迟消息与集群高可用'),
  ('Kafka',        33, 'kafka', '分区副本机制、消费组、顺序性、Exactly-Once 语义'),
  ('Elasticsearch', 94, 'elasticsearch', '倒排索引、分片副本、查询 DSL、集群架构与性能调优'),
  -- AI（面试鸭：AI Agent 智能体）
  ('AI Agent',     84, 'ai_agent', '智能体架构、感知决策执行、记忆机制、RAG 与工具调用'),
  -- 前端细分（面试鸭：HTML/CSS/ES6/手写/工程化/性能）
  ('前端 HTML',    74, 'html', '语义化标签、表单控件、HTML5 新特性与 SEO 基础'),
  ('前端 CSS',     75, 'css', '选择器优先级、盒模型、Flex/Grid 布局、BFC 与响应式'),
  ('ES6',          76, 'javascript', 'let/const、解构赋值、Promise、async/await、模块化与新 API'),
  ('前端手写题',   77, 'handwritten', '手写 Promise、防抖节流、深拷贝、call/apply/bind 等高频手写题'),
  ('前端工程化',   78, 'webpack', 'Webpack/Vite 构建原理、Loader 与插件、打包优化、模块化规范'),
  ('前端性能优化', 79, 'performance', '首屏优化、懒加载、缓存策略、渲染性能与监控指标'),
  -- 场景题（面试鸭：后端场景/前端场景）
  ('后端场景题',   134, 'scene', '秒杀、订单、支付、幂等、分布式锁与线上问题排查等实战场景'),
  ('前端场景题',   85, 'scene', '首屏加载、移动端适配、状态管理选型与异常监控等实战场景'),
  -- 面试周边（面试鸭：智力题/公司面经）
  ('智力题',       186, 'puzzle', '逻辑推理、概率思维、脑筋急转弯类面试智力题'),
  ('大厂面经',     187, 'company', '美团、腾讯、字节、阿里等大厂面试经验与真题复盘'),
  -- 运维（面试鸭：DevOps 运维）
  ('DevOps',       191, 'devops', 'CI/CD 流水线、容器编排、监控告警与自动化运维');

INSERT INTO category (name, sort, icon, description)
SELECT t.name, t.sort, t.icon, t.description
FROM tmp_ms_category t
WHERE NOT EXISTS (
  SELECT 1 FROM category c WHERE c.name = t.name AND c.deleted = 0
);

DROP TEMPORARY TABLE IF EXISTS tmp_ms_category;

-- ---------------------------------------------------------------------
-- 二、「MQ」改名「消息队列」（对齐面试鸭命名）
-- ---------------------------------------------------------------------
UPDATE category SET name = '消息队列'
WHERE name = 'MQ' AND deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM (
      SELECT 1 FROM category c2 WHERE c2.name = '消息队列' AND c2.deleted = 0
    ) exists_check
  );

-- ---------------------------------------------------------------------
-- 三、绑定分类到分组标签
-- ---------------------------------------------------------------------
-- Java 分组
INSERT IGNORE INTO category_group_rel (category_id, group_id)
SELECT c.id, g.id FROM category c, category_group g
WHERE g.group_key = 'java' AND g.deleted = 0 AND c.deleted = 0
  AND c.name IN ('Java 集合', 'Java 并发', 'Java 虚拟机', 'SpringBoot', 'SpringCloud');

-- 后端分组
INSERT IGNORE INTO category_group_rel (category_id, group_id)
SELECT c.id, g.id FROM category c, category_group g
WHERE g.group_key = 'backend' AND g.deleted = 0 AND c.deleted = 0
  AND c.name IN ('Java 集合', 'Java 并发', 'Java 虚拟机', 'SpringBoot', 'SpringCloud',
                 'RabbitMQ', 'Kafka', 'Elasticsearch', '后端场景题');

-- 人工智能分组
INSERT IGNORE INTO category_group_rel (category_id, group_id)
SELECT c.id, g.id FROM category c, category_group g
WHERE g.group_key = 'ai' AND g.deleted = 0 AND c.deleted = 0
  AND c.name IN ('AI Agent');

-- 前端分组
INSERT IGNORE INTO category_group_rel (category_id, group_id)
SELECT c.id, g.id FROM category c, category_group g
WHERE g.group_key = 'frontend' AND g.deleted = 0 AND c.deleted = 0
  AND c.name IN ('前端 HTML', '前端 CSS', 'ES6', '前端手写题', '前端工程化', '前端性能优化', '前端场景题');

-- 大数据分组（Kafka 兼具数据管道属性）
INSERT IGNORE INTO category_group_rel (category_id, group_id)
SELECT c.id, g.id FROM category c, category_group g
WHERE g.group_key = 'bigdata' AND g.deleted = 0 AND c.deleted = 0
  AND c.name IN ('Kafka');

-- 面试分组
INSERT IGNORE INTO category_group_rel (category_id, group_id)
SELECT c.id, g.id FROM category c, category_group g
WHERE g.group_key = 'interview' AND g.deleted = 0 AND c.deleted = 0
  AND c.name IN ('Java 并发', 'Elasticsearch', '后端场景题', '前端手写题', '智力题', '大厂面经');

-- 热门分组
INSERT IGNORE INTO category_group_rel (category_id, group_id)
SELECT c.id, g.id FROM category c, category_group g
WHERE g.group_key = 'hot' AND g.deleted = 0 AND c.deleted = 0
  AND c.name IN ('AI Agent', '大厂面经');

-- 运维分组
INSERT IGNORE INTO category_group_rel (category_id, group_id)
SELECT c.id, g.id FROM category c, category_group g
WHERE g.group_key = 'ops' AND g.deleted = 0 AND c.deleted = 0
  AND c.name IN ('DevOps');
