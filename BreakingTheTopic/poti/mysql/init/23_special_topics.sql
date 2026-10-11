-- ============================================================
-- 23_special_topics.sql  刷题专题数据：覆盖项目全部分类方向
-- 幂等：专题按 name 查重，挂题按 (topic_id, question_id) 防重
-- 选题规则：简单→中等→困难，同难度按 id 升序，确定性选题
-- ============================================================

-- ---------- 专题1：面试必刷 100 题（已有，补描述与排序） ----------
UPDATE special_topic SET description = ' HR、行为、算法高频面试题一次刷透，面试前必过一遍。', sort = 1, estimated_days = 14
WHERE name = '面试必刷 100 题' AND deleted = 0 AND (description IS NULL OR description = '');

-- ---------- 专题2：MySQL 高频考点突击（已有10题，补到30） ----------
SET @tid = (SELECT id FROM special_topic WHERE name = 'MySQL 高频考点突击' AND deleted = 0 LIMIT 1);
INSERT INTO special_question (topic_id, question_id, sort)
SELECT @tid, t.id, ROW_NUMBER() OVER (ORDER BY FIELD(t.difficulty, 1, 2, 3), t.id) + 10
FROM (
    SELECT q.* FROM question q
    WHERE q.deleted = 0 AND q.status = 1 AND q.category_id = (SELECT id FROM category WHERE deleted = 0 AND name = 'MySQL' LIMIT 1)
      AND q.id NOT IN (SELECT question_id FROM special_question WHERE topic_id = @tid)
    ORDER BY FIELD(q.difficulty, 1, 2, 3), q.id LIMIT 20
) t;

-- ---------- 专题3：算法入门：从零开始刷题（已有10题，补到40） ----------
SET @tid = (SELECT id FROM special_topic WHERE name = '算法入门：从零开始刷题' AND deleted = 0 LIMIT 1);
INSERT INTO special_question (topic_id, question_id, sort)
SELECT @tid, t.id, ROW_NUMBER() OVER (ORDER BY FIELD(t.difficulty, 1, 2, 3), t.id) + 10
FROM (
    SELECT q.* FROM question q
    WHERE q.deleted = 0 AND q.status = 1 AND q.category_id IN (SELECT id FROM category WHERE deleted = 0 AND name IN ('双指针', '二分法', '滑动窗口', '链表', '栈', '排序'))
      AND q.id NOT IN (SELECT question_id FROM special_question WHERE topic_id = @tid)
    ORDER BY FIELD(q.difficulty, 1, 2, 3), q.id LIMIT 30
) t;
UPDATE special_topic SET estimated_days = 10, sort = 3 WHERE id = @tid;

-- ---------- 专题4：数据结构与算法进阶 ----------
INSERT INTO special_topic (name, cover, description, difficulty, estimated_days, unlock_type, sort, status)
SELECT '数据结构与算法进阶', '', '树、图、回溯、动态规划、Trie……大厂算法面试的硬骨头，逐个击破。', 3, 12, 0, 4, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM special_topic WHERE name = '数据结构与算法进阶' AND deleted = 0);
SET @tid = (SELECT id FROM special_topic WHERE name = '数据结构与算法进阶' AND deleted = 0 LIMIT 1);
INSERT INTO special_question (topic_id, question_id, sort)
SELECT @tid, t.id, ROW_NUMBER() OVER (ORDER BY FIELD(t.difficulty, 1, 2, 3), t.id)
FROM (
    SELECT q.* FROM question q
    WHERE q.deleted = 0 AND q.status = 1 AND q.category_id IN (SELECT id FROM category WHERE deleted = 0 AND name IN ('树', '图', '回溯', '贪心', '动态规划', '前缀和', '差分', '位运算', 'Trie', '并查集', '字符串', '数学'))
      AND q.id NOT IN (SELECT question_id FROM special_question WHERE topic_id = @tid)
    ORDER BY FIELD(q.difficulty, 1, 2, 3), q.id LIMIT 40
) t;

-- ---------- 专题5：Redis 缓存攻坚 ----------
INSERT INTO special_topic (name, cover, description, difficulty, estimated_days, unlock_type, sort, status)
SELECT 'Redis 缓存攻坚', '', '缓存穿透、雪崩、持久化、集群……Redis 面试八股一次刷透。', 2, 8, 0, 5, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM special_topic WHERE name = 'Redis 缓存攻坚' AND deleted = 0);
SET @tid = (SELECT id FROM special_topic WHERE name = 'Redis 缓存攻坚' AND deleted = 0 LIMIT 1);
INSERT INTO special_question (topic_id, question_id, sort)
SELECT @tid, t.id, ROW_NUMBER() OVER (ORDER BY FIELD(t.difficulty, 1, 2, 3), t.id)
FROM (
    SELECT q.* FROM question q
    WHERE q.deleted = 0 AND q.status = 1 AND q.category_id = (SELECT id FROM category WHERE deleted = 0 AND name = 'Redis' LIMIT 1)
      AND q.id NOT IN (SELECT question_id FROM special_question WHERE topic_id = @tid)
    ORDER BY FIELD(q.difficulty, 1, 2, 3), q.id LIMIT 30
) t;

-- ---------- 专题6：计算机网络通关手册 ----------
INSERT INTO special_topic (name, cover, description, difficulty, estimated_days, unlock_type, sort, status)
SELECT '计算机网络通关手册', '', 'TCP/IP、HTTP/HTTPS、三次握手四次挥手——网络面试从不缺席。', 2, 8, 0, 6, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM special_topic WHERE name = '计算机网络通关手册' AND deleted = 0);
SET @tid = (SELECT id FROM special_topic WHERE name = '计算机网络通关手册' AND deleted = 0 LIMIT 1);
INSERT INTO special_question (topic_id, question_id, sort)
SELECT @tid, t.id, ROW_NUMBER() OVER (ORDER BY FIELD(t.difficulty, 1, 2, 3), t.id)
FROM (
    SELECT q.* FROM question q
    WHERE q.deleted = 0 AND q.status = 1 AND q.category_id = (SELECT id FROM category WHERE deleted = 0 AND name = '计算机网络' LIMIT 1)
      AND q.id NOT IN (SELECT question_id FROM special_question WHERE topic_id = @tid)
    ORDER BY FIELD(q.difficulty, 1, 2, 3), q.id LIMIT 30
) t;

-- ---------- 专题7：操作系统核心考点 ----------
INSERT INTO special_topic (name, cover, description, difficulty, estimated_days, unlock_type, sort, status)
SELECT '操作系统核心考点', '', '进程线程、内存管理、死锁、IO 多路复用——操作系统面试高频合集。', 2, 8, 0, 7, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM special_topic WHERE name = '操作系统核心考点' AND deleted = 0);
SET @tid = (SELECT id FROM special_topic WHERE name = '操作系统核心考点' AND deleted = 0 LIMIT 1);
INSERT INTO special_question (topic_id, question_id, sort)
SELECT @tid, t.id, ROW_NUMBER() OVER (ORDER BY FIELD(t.difficulty, 1, 2, 3), t.id)
FROM (
    SELECT q.* FROM question q
    WHERE q.deleted = 0 AND q.status = 1 AND q.category_id = (SELECT id FROM category WHERE deleted = 0 AND name = '操作系统' LIMIT 1)
      AND q.id NOT IN (SELECT question_id FROM special_question WHERE topic_id = @tid)
    ORDER BY FIELD(q.difficulty, 1, 2, 3), q.id LIMIT 30
) t;

-- ---------- 专题8：Java 基础强化 ----------
INSERT INTO special_topic (name, cover, description, difficulty, estimated_days, unlock_type, sort, status)
SELECT 'Java 基础强化', '', '语法、面向对象、集合框架——Java 面试的地基，打得牢才站得稳。', 1, 8, 0, 8, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM special_topic WHERE name = 'Java 基础强化' AND deleted = 0);
SET @tid = (SELECT id FROM special_topic WHERE name = 'Java 基础强化' AND deleted = 0 LIMIT 1);
INSERT INTO special_question (topic_id, question_id, sort)
SELECT @tid, t.id, ROW_NUMBER() OVER (ORDER BY FIELD(t.difficulty, 1, 2, 3), t.id)
FROM (
    SELECT q.* FROM question q
    WHERE q.deleted = 0 AND q.status = 1 AND q.category_id IN (SELECT id FROM category WHERE deleted = 0 AND name IN ('Java', 'Java 集合'))
      AND q.id NOT IN (SELECT question_id FROM special_question WHERE topic_id = @tid)
    ORDER BY FIELD(q.difficulty, 1, 2, 3), q.id LIMIT 40
) t;

-- ---------- 专题9：Java 并发编程专项 ----------
INSERT INTO special_topic (name, cover, description, difficulty, estimated_days, unlock_type, sort, status)
SELECT 'Java 并发编程专项', '', '线程池、锁、AQS、并发容器——区分初级与高级工程师的分水岭。', 3, 8, 0, 9, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM special_topic WHERE name = 'Java 并发编程专项' AND deleted = 0);
SET @tid = (SELECT id FROM special_topic WHERE name = 'Java 并发编程专项' AND deleted = 0 LIMIT 1);
INSERT INTO special_question (topic_id, question_id, sort)
SELECT @tid, t.id, ROW_NUMBER() OVER (ORDER BY FIELD(t.difficulty, 1, 2, 3), t.id)
FROM (
    SELECT q.* FROM question q
    WHERE q.deleted = 0 AND q.status = 1 AND q.category_id = (SELECT id FROM category WHERE deleted = 0 AND name = 'Java 并发' LIMIT 1)
      AND q.id NOT IN (SELECT question_id FROM special_question WHERE topic_id = @tid)
    ORDER BY FIELD(q.difficulty, 1, 2, 3), q.id LIMIT 30
) t;

-- ---------- 专题10：JVM 虚拟机攻坚 ----------
INSERT INTO special_topic (name, cover, description, difficulty, estimated_days, unlock_type, sort, status)
SELECT 'JVM 虚拟机攻坚', '', '内存模型、垃圾回收、类加载、调优实战——JVM 面试深度专项。', 3, 8, 0, 10, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM special_topic WHERE name = 'JVM 虚拟机攻坚' AND deleted = 0);
SET @tid = (SELECT id FROM special_topic WHERE name = 'JVM 虚拟机攻坚' AND deleted = 0 LIMIT 1);
INSERT INTO special_question (topic_id, question_id, sort)
SELECT @tid, t.id, ROW_NUMBER() OVER (ORDER BY FIELD(t.difficulty, 1, 2, 3), t.id)
FROM (
    SELECT q.* FROM question q
    WHERE q.deleted = 0 AND q.status = 1 AND q.category_id = (SELECT id FROM category WHERE deleted = 0 AND name = 'Java 虚拟机' LIMIT 1)
      AND q.id NOT IN (SELECT question_id FROM special_question WHERE topic_id = @tid)
    ORDER BY FIELD(q.difficulty, 1, 2, 3), q.id LIMIT 30
) t;

-- ---------- 专题11：Spring 全家桶实战 ----------
INSERT INTO special_topic (name, cover, description, difficulty, estimated_days, unlock_type, sort, status)
SELECT 'Spring 全家桶实战', '', 'IOC、AOP、事务、Boot 自动装配、Cloud 微服务组件全覆盖。', 2, 10, 0, 11, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM special_topic WHERE name = 'Spring 全家桶实战' AND deleted = 0);
SET @tid = (SELECT id FROM special_topic WHERE name = 'Spring 全家桶实战' AND deleted = 0 LIMIT 1);
INSERT INTO special_question (topic_id, question_id, sort)
SELECT @tid, t.id, ROW_NUMBER() OVER (ORDER BY FIELD(t.difficulty, 1, 2, 3), t.id)
FROM (
    SELECT q.* FROM question q
    WHERE q.deleted = 0 AND q.status = 1 AND q.category_id IN (SELECT id FROM category WHERE deleted = 0 AND name IN ('Spring', 'SpringBoot', 'SpringCloud', 'MyBatis'))
      AND q.id NOT IN (SELECT question_id FROM special_question WHERE topic_id = @tid)
    ORDER BY FIELD(q.difficulty, 1, 2, 3), q.id LIMIT 40
) t;

-- ---------- 专题12：消息队列突击 ----------
INSERT INTO special_topic (name, cover, description, difficulty, estimated_days, unlock_type, sort, status)
SELECT '消息队列突击', '', '消息丢失、重复消费、顺序消息——RabbitMQ 与 Kafka 面试高频问题。', 2, 8, 0, 12, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM special_topic WHERE name = '消息队列突击' AND deleted = 0);
SET @tid = (SELECT id FROM special_topic WHERE name = '消息队列突击' AND deleted = 0 LIMIT 1);
INSERT INTO special_question (topic_id, question_id, sort)
SELECT @tid, t.id, ROW_NUMBER() OVER (ORDER BY FIELD(t.difficulty, 1, 2, 3), t.id)
FROM (
    SELECT q.* FROM question q
    WHERE q.deleted = 0 AND q.status = 1 AND q.category_id IN (SELECT id FROM category WHERE deleted = 0 AND name IN ('RabbitMQ', 'Kafka', '消息队列'))
      AND q.id NOT IN (SELECT question_id FROM special_question WHERE topic_id = @tid)
    ORDER BY FIELD(q.difficulty, 1, 2, 3), q.id LIMIT 40
) t;

-- ---------- 专题13：微服务与云原生 ----------
INSERT INTO special_topic (name, cover, description, difficulty, estimated_days, unlock_type, sort, status)
SELECT '微服务与云原生', '', '注册中心、网关、熔断限流、容器编排——分布式面试必备知识。', 3, 10, 0, 13, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM special_topic WHERE name = '微服务与云原生' AND deleted = 0);
SET @tid = (SELECT id FROM special_topic WHERE name = '微服务与云原生' AND deleted = 0 LIMIT 1);
INSERT INTO special_question (topic_id, question_id, sort)
SELECT @tid, t.id, ROW_NUMBER() OVER (ORDER BY FIELD(t.difficulty, 1, 2, 3), t.id)
FROM (
    SELECT q.* FROM question q
    WHERE q.deleted = 0 AND q.status = 1 AND q.category_id IN (SELECT id FROM category WHERE deleted = 0 AND name IN ('微服务', 'Dubbo', 'Netty', 'Zookeeper', 'Docker', 'Kubernetes', 'Nginx'))
      AND q.id NOT IN (SELECT question_id FROM special_question WHERE topic_id = @tid)
    ORDER BY FIELD(q.difficulty, 1, 2, 3), q.id LIMIT 40
) t;

-- ---------- 专题14：搜索与大数据 ----------
INSERT INTO special_topic (name, cover, description, difficulty, estimated_days, unlock_type, sort, status)
SELECT '搜索与大数据', '', 'Elasticsearch 倒排索引与 Hadoop/Spark/Flink 生态一网打尽。', 3, 10, 0, 14, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM special_topic WHERE name = '搜索与大数据' AND deleted = 0);
SET @tid = (SELECT id FROM special_topic WHERE name = '搜索与大数据' AND deleted = 0 LIMIT 1);
INSERT INTO special_question (topic_id, question_id, sort)
SELECT @tid, t.id, ROW_NUMBER() OVER (ORDER BY FIELD(t.difficulty, 1, 2, 3), t.id)
FROM (
    SELECT q.* FROM question q
    WHERE q.deleted = 0 AND q.status = 1 AND q.category_id IN (SELECT id FROM category WHERE deleted = 0 AND name IN ('Elasticsearch', 'Hadoop', 'Spark', 'Flink', '数据仓库'))
      AND q.id NOT IN (SELECT question_id FROM special_question WHERE topic_id = @tid)
    ORDER BY FIELD(q.difficulty, 1, 2, 3), q.id LIMIT 40
) t;

-- ---------- 专题15：前端基础三件套 ----------
INSERT INTO special_topic (name, cover, description, difficulty, estimated_days, unlock_type, sort, status)
SELECT '前端基础三件套', '', 'HTML 语义化、CSS 布局、JavaScript 核心机制——前端面试第一关。', 1, 8, 0, 15, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM special_topic WHERE name = '前端基础三件套' AND deleted = 0);
SET @tid = (SELECT id FROM special_topic WHERE name = '前端基础三件套' AND deleted = 0 LIMIT 1);
INSERT INTO special_question (topic_id, question_id, sort)
SELECT @tid, t.id, ROW_NUMBER() OVER (ORDER BY FIELD(t.difficulty, 1, 2, 3), t.id)
FROM (
    SELECT q.* FROM question q
    WHERE q.deleted = 0 AND q.status = 1 AND q.category_id IN (SELECT id FROM category WHERE deleted = 0 AND name IN ('前端 HTML', '前端 CSS', 'JavaScript'))
      AND q.id NOT IN (SELECT question_id FROM special_question WHERE topic_id = @tid)
    ORDER BY FIELD(q.difficulty, 1, 2, 3), q.id LIMIT 40
) t;

-- ---------- 专题16：ES6 与 TypeScript 现代前端 ----------
INSERT INTO special_topic (name, cover, description, difficulty, estimated_days, unlock_type, sort, status)
SELECT 'ES6 与 TypeScript 现代前端', '', 'let/const、Promise、泛型、类型体操——现代 JS 面试必修课。', 2, 7, 0, 16, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM special_topic WHERE name = 'ES6 与 TypeScript 现代前端' AND deleted = 0);
SET @tid = (SELECT id FROM special_topic WHERE name = 'ES6 与 TypeScript 现代前端' AND deleted = 0 LIMIT 1);
INSERT INTO special_question (topic_id, question_id, sort)
SELECT @tid, t.id, ROW_NUMBER() OVER (ORDER BY FIELD(t.difficulty, 1, 2, 3), t.id)
FROM (
    SELECT q.* FROM question q
    WHERE q.deleted = 0 AND q.status = 1 AND q.category_id IN (SELECT id FROM category WHERE deleted = 0 AND name IN ('ES6', 'TypeScript'))
      AND q.id NOT IN (SELECT question_id FROM special_question WHERE topic_id = @tid)
    ORDER BY FIELD(q.difficulty, 1, 2, 3), q.id LIMIT 40
) t;

-- ---------- 专题17：前端框架双雄 ----------
INSERT INTO special_topic (name, cover, description, difficulty, estimated_days, unlock_type, sort, status)
SELECT '前端框架双雄', '', '响应式原理、diff 算法、生命周期、Hooks——Vue 与 React 面试全解。', 2, 10, 0, 17, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM special_topic WHERE name = '前端框架双雄' AND deleted = 0);
SET @tid = (SELECT id FROM special_topic WHERE name = '前端框架双雄' AND deleted = 0 LIMIT 1);
INSERT INTO special_question (topic_id, question_id, sort)
SELECT @tid, t.id, ROW_NUMBER() OVER (ORDER BY FIELD(t.difficulty, 1, 2, 3), t.id)
FROM (
    SELECT q.* FROM question q
    WHERE q.deleted = 0 AND q.status = 1 AND q.category_id IN (SELECT id FROM category WHERE deleted = 0 AND name IN ('Vue', 'React', 'Node.js'))
      AND q.id NOT IN (SELECT question_id FROM special_question WHERE topic_id = @tid)
    ORDER BY FIELD(q.difficulty, 1, 2, 3), q.id LIMIT 40
) t;

-- ---------- 专题18：前端进阶：手写与工程化 ----------
INSERT INTO special_topic (name, cover, description, difficulty, estimated_days, unlock_type, sort, status)
SELECT '前端进阶：手写与工程化', '', '防抖节流、深拷贝、Promise 封装、构建工具与性能优化实战。', 3, 10, 0, 18, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM special_topic WHERE name = '前端进阶：手写与工程化' AND deleted = 0);
SET @tid = (SELECT id FROM special_topic WHERE name = '前端进阶：手写与工程化' AND deleted = 0 LIMIT 1);
INSERT INTO special_question (topic_id, question_id, sort)
SELECT @tid, t.id, ROW_NUMBER() OVER (ORDER BY FIELD(t.difficulty, 1, 2, 3), t.id)
FROM (
    SELECT q.* FROM question q
    WHERE q.deleted = 0 AND q.status = 1 AND q.category_id IN (SELECT id FROM category WHERE deleted = 0 AND name IN ('前端手写题', '前端工程化', '前端性能优化', '前端场景题'))
      AND q.id NOT IN (SELECT question_id FROM special_question WHERE topic_id = @tid)
    ORDER BY FIELD(q.difficulty, 1, 2, 3), q.id LIMIT 40
) t;

-- ---------- 专题19：Go 语言面试直通车 ----------
INSERT INTO special_topic (name, cover, description, difficulty, estimated_days, unlock_type, sort, status)
SELECT 'Go 语言面试直通车', '', 'goroutine、channel、GC、调度器——Go 高频面试题精选。', 2, 10, 0, 19, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM special_topic WHERE name = 'Go 语言面试直通车' AND deleted = 0);
SET @tid = (SELECT id FROM special_topic WHERE name = 'Go 语言面试直通车' AND deleted = 0 LIMIT 1);
INSERT INTO special_question (topic_id, question_id, sort)
SELECT @tid, t.id, ROW_NUMBER() OVER (ORDER BY FIELD(t.difficulty, 1, 2, 3), t.id)
FROM (
    SELECT q.* FROM question q
    WHERE q.deleted = 0 AND q.status = 1 AND q.category_id IN (SELECT id FROM category WHERE deleted = 0 AND name LIKE 'Go%')
      AND q.id NOT IN (SELECT question_id FROM special_question WHERE topic_id = @tid)
    ORDER BY FIELD(q.difficulty, 1, 2, 3), q.id LIMIT 40
) t;

-- ---------- 专题20：C++ 核心考点精炼 ----------
INSERT INTO special_topic (name, cover, description, difficulty, estimated_days, unlock_type, sort, status)
SELECT 'C++ 核心考点精炼', '', '内存管理、STL、多态、并发——C++ 面试深度精选。', 3, 10, 0, 20, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM special_topic WHERE name = 'C++ 核心考点精炼' AND deleted = 0);
SET @tid = (SELECT id FROM special_topic WHERE name = 'C++ 核心考点精炼' AND deleted = 0 LIMIT 1);
INSERT INTO special_question (topic_id, question_id, sort)
SELECT @tid, t.id, ROW_NUMBER() OVER (ORDER BY FIELD(t.difficulty, 1, 2, 3), t.id)
FROM (
    SELECT q.* FROM question q
    WHERE q.deleted = 0 AND q.status = 1 AND q.category_id IN (SELECT id FROM category WHERE deleted = 0 AND name LIKE 'C++%')
      AND q.id NOT IN (SELECT question_id FROM special_question WHERE topic_id = @tid)
    ORDER BY FIELD(q.difficulty, 1, 2, 3), q.id LIMIT 40
) t;

-- ---------- 专题21：Python 面试精选 ----------
INSERT INTO special_topic (name, cover, description, difficulty, estimated_days, unlock_type, sort, status)
SELECT 'Python 面试精选', '', 'GIL、装饰器、生成器、元类——Python 面试高频考点。', 2, 8, 0, 21, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM special_topic WHERE name = 'Python 面试精选' AND deleted = 0);
SET @tid = (SELECT id FROM special_topic WHERE name = 'Python 面试精选' AND deleted = 0 LIMIT 1);
INSERT INTO special_question (topic_id, question_id, sort)
SELECT @tid, t.id, ROW_NUMBER() OVER (ORDER BY FIELD(t.difficulty, 1, 2, 3), t.id)
FROM (
    SELECT q.* FROM question q
    WHERE q.deleted = 0 AND q.status = 1 AND q.category_id IN (SELECT id FROM category WHERE deleted = 0 AND name IN ('Python', 'Python代码分析', 'Python手写代码'))
      AND q.id NOT IN (SELECT question_id FROM special_question WHERE topic_id = @tid)
    ORDER BY FIELD(q.difficulty, 1, 2, 3), q.id LIMIT 40
) t;

-- ---------- 专题22：移动开发精选 ----------
INSERT INTO special_topic (name, cover, description, difficulty, estimated_days, unlock_type, sort, status)
SELECT '移动开发精选', '', 'Android 四大组件、生命周期、跨平台方案——移动端面试一站式准备。', 2, 10, 0, 22, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM special_topic WHERE name = '移动开发精选' AND deleted = 0);
SET @tid = (SELECT id FROM special_topic WHERE name = '移动开发精选' AND deleted = 0 LIMIT 1);
INSERT INTO special_question (topic_id, question_id, sort)
SELECT @tid, t.id, ROW_NUMBER() OVER (ORDER BY FIELD(t.difficulty, 1, 2, 3), t.id)
FROM (
    SELECT q.* FROM question q
    WHERE q.deleted = 0 AND q.status = 1 AND q.category_id IN (SELECT id FROM category WHERE deleted = 0 AND (name LIKE 'Android%' OR name IN ('Swift', 'Kotlin', 'Flutter', 'Dart', 'React Native', 'H5 网站开发', '鸿蒙开发')))
      AND q.id NOT IN (SELECT question_id FROM special_question WHERE topic_id = @tid)
    ORDER BY FIELD(q.difficulty, 1, 2, 3), q.id LIMIT 40
) t;

-- ---------- 专题23：场景与系统设计实战 ----------
INSERT INTO special_topic (name, cover, description, difficulty, estimated_days, unlock_type, sort, status)
SELECT '场景与系统设计实战', '', '秒杀系统、短链服务、限流方案——高级工程师面试的场景题集。', 3, 10, 0, 23, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM special_topic WHERE name = '场景与系统设计实战' AND deleted = 0);
SET @tid = (SELECT id FROM special_topic WHERE name = '场景与系统设计实战' AND deleted = 0 LIMIT 1);
INSERT INTO special_question (topic_id, question_id, sort)
SELECT @tid, t.id, ROW_NUMBER() OVER (ORDER BY FIELD(t.difficulty, 1, 2, 3), t.id)
FROM (
    SELECT q.* FROM question q
    WHERE q.deleted = 0 AND q.status = 1 AND q.category_id IN (SELECT id FROM category WHERE deleted = 0 AND name IN ('后端场景题', '系统设计', '设计模式'))
      AND q.id NOT IN (SELECT question_id FROM special_question WHERE topic_id = @tid)
    ORDER BY FIELD(q.difficulty, 1, 2, 3), q.id LIMIT 40
) t;

-- ---------- 专题24：AI 大模型面试指南 ----------
INSERT INTO special_topic (name, cover, description, difficulty, estimated_days, unlock_type, sort, status)
SELECT 'AI 大模型面试指南', '', '机器学习、深度学习、NLP、Agent——AI 岗位面试知识全景。', 3, 10, 0, 24, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM special_topic WHERE name = 'AI 大模型面试指南' AND deleted = 0);
SET @tid = (SELECT id FROM special_topic WHERE name = 'AI 大模型面试指南' AND deleted = 0 LIMIT 1);
INSERT INTO special_question (topic_id, question_id, sort)
SELECT @tid, t.id, ROW_NUMBER() OVER (ORDER BY FIELD(t.difficulty, 1, 2, 3), t.id)
FROM (
    SELECT q.* FROM question q
    WHERE q.deleted = 0 AND q.status = 1 AND q.category_id IN (SELECT id FROM category WHERE deleted = 0 AND name IN ('机器学习', '深度学习', 'NLP', '计算机视觉', 'AI Agent'))
      AND q.id NOT IN (SELECT question_id FROM special_question WHERE topic_id = @tid)
    ORDER BY FIELD(q.difficulty, 1, 2, 3), q.id LIMIT 40
) t;

-- ---------- 专题25：HR 与行为面试通关 ----------
INSERT INTO special_topic (name, cover, description, difficulty, estimated_days, unlock_type, sort, status)
SELECT 'HR 与行为面试通关', '', '自我介绍、缺点应答、薪资谈判、无领导小组——软性面试全攻略。', 1, 7, 0, 25, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM special_topic WHERE name = 'HR 与行为面试通关' AND deleted = 0);
SET @tid = (SELECT id FROM special_topic WHERE name = 'HR 与行为面试通关' AND deleted = 0 LIMIT 1);
INSERT INTO special_question (topic_id, question_id, sort)
SELECT @tid, t.id, ROW_NUMBER() OVER (ORDER BY FIELD(t.difficulty, 1, 2, 3), t.id)
FROM (
    SELECT q.* FROM question q
    WHERE q.deleted = 0 AND q.status = 1 AND q.category_id IN (SELECT id FROM category WHERE deleted = 0 AND name IN ('HR面试', '行为面试'))
      AND q.id NOT IN (SELECT question_id FROM special_question WHERE topic_id = @tid)
    ORDER BY FIELD(q.difficulty, 1, 2, 3), q.id LIMIT 40
) t;

-- ---------- 专题26：大厂面经与智力挑战 ----------
INSERT INTO special_topic (name, cover, description, difficulty, estimated_days, unlock_type, sort, status)
SELECT '大厂面经与智力挑战', '', '大厂真实面经还原 + 经典智力题，面经看多了套路自然熟。', 2, 7, 0, 26, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM special_topic WHERE name = '大厂面经与智力挑战' AND deleted = 0);
SET @tid = (SELECT id FROM special_topic WHERE name = '大厂面经与智力挑战' AND deleted = 0 LIMIT 1);
INSERT INTO special_question (topic_id, question_id, sort)
SELECT @tid, t.id, ROW_NUMBER() OVER (ORDER BY FIELD(t.difficulty, 1, 2, 3), t.id)
FROM (
    SELECT q.* FROM question q
    WHERE q.deleted = 0 AND q.status = 1 AND q.category_id IN (SELECT id FROM category WHERE deleted = 0 AND name IN ('大厂面经', '智力题'))
      AND q.id NOT IN (SELECT question_id FROM special_question WHERE topic_id = @tid)
    ORDER BY FIELD(q.difficulty, 1, 2, 3), q.id LIMIT 40
) t;

-- ---------- 专题27：测试与运维基础 ----------
INSERT INTO special_topic (name, cover, description, difficulty, estimated_days, unlock_type, sort, status)
SELECT '测试与运维基础', '', '测试方法、Linux 命令、Git 操作、CI/CD——工程化能力必备。', 1, 8, 0, 27, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM special_topic WHERE name = '测试与运维基础' AND deleted = 0);
SET @tid = (SELECT id FROM special_topic WHERE name = '测试与运维基础' AND deleted = 0 LIMIT 1);
INSERT INTO special_question (topic_id, question_id, sort)
SELECT @tid, t.id, ROW_NUMBER() OVER (ORDER BY FIELD(t.difficulty, 1, 2, 3), t.id)
FROM (
    SELECT q.* FROM question q
    WHERE q.deleted = 0 AND q.status = 1 AND q.category_id IN (SELECT id FROM category WHERE deleted = 0 AND (name IN ('软件测试', 'Linux', 'DevOps', 'Web安全', '密码学') OR name LIKE 'Git%'))
      AND q.id NOT IN (SELECT question_id FROM special_question WHERE topic_id = @tid)
    ORDER BY FIELD(q.difficulty, 1, 2, 3), q.id LIMIT 40
) t;

-- ---------- 专题28：计算机基础考研 ----------
INSERT INTO special_topic (name, cover, description, difficulty, estimated_days, unlock_type, sort, status)
SELECT '计算机基础考研', '', '组成原理、编译原理、数据结构 + 数学三件套——考研复试双保险。', 2, 12, 0, 28, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM special_topic WHERE name = '计算机基础考研' AND deleted = 0);
SET @tid = (SELECT id FROM special_topic WHERE name = '计算机基础考研' AND deleted = 0 LIMIT 1);
INSERT INTO special_question (topic_id, question_id, sort)
SELECT @tid, t.id, ROW_NUMBER() OVER (ORDER BY FIELD(t.difficulty, 1, 2, 3), t.id)
FROM (
    SELECT q.* FROM question q
    WHERE q.deleted = 0 AND q.status = 1 AND q.category_id IN (SELECT id FROM category WHERE deleted = 0 AND name IN ('计算机组成', '编译原理', '数据结构', '高等数学', '线性代数', '概率论', '离散数学'))
      AND q.id NOT IN (SELECT question_id FROM special_question WHERE topic_id = @tid)
    ORDER BY FIELD(q.difficulty, 1, 2, 3), q.id LIMIT 40
) t;

-- ---------- 验证 ----------
SELECT t.id, t.name, t.difficulty, t.estimated_days, COUNT(sq.id) AS question_count
FROM special_topic t LEFT JOIN special_question sq ON sq.topic_id = t.id
WHERE t.deleted = 0 GROUP BY t.id, t.name, t.difficulty, t.estimated_days ORDER BY t.sort;
