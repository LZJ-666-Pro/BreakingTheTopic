-- ============================================================
-- 22 号固化脚本：Go / C++ 分组拆分细分分类（各 6 个，对照面试鸭）
-- 幂等：重复执行不会产生重复数据
-- ============================================================

-- ---------- 1. Go 细分（挂 go 分组） ----------
INSERT INTO category (name, icon, icon_url, description, sort, deleted)
SELECT * FROM (SELECT 'Go基础' AS name, 'go' AS icon, '/images/questionLogo/go.png' AS icon_url,
  'Go 语言基础：语法、数据类型、切片、映射与错误处理' AS description, 320 AS sort, 0 AS deleted) t
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'Go基础' AND deleted = 0);
INSERT INTO category (name, icon, icon_url, description, sort, deleted)
SELECT * FROM (SELECT 'Go标准库', 'go', '/images/questionLogo/go.png',
  'Go 标准库：fmt、io、net/http、time 与常用工具包', 321, 0) t
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'Go标准库' AND deleted = 0);
INSERT INTO category (name, icon, icon_url, description, sort, deleted)
SELECT * FROM (SELECT 'Go面向对象', 'go', '/images/questionLogo/go.png',
  'Go 面向对象：结构体、方法、接口、组合与鸭子类型', 322, 0) t
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'Go面向对象' AND deleted = 0);
INSERT INTO category (name, icon, icon_url, description, sort, deleted)
SELECT * FROM (SELECT 'Go底层原理', 'go', '/images/questionLogo/go.png',
  'Go 底层原理：调度器 GMP、内存分配、逃逸分析与运行时', 323, 0) t
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'Go底层原理' AND deleted = 0);
INSERT INTO category (name, icon, icon_url, description, sort, deleted)
SELECT * FROM (SELECT 'Go性能优化', 'go', '/images/questionLogo/go.png',
  'Go 性能优化：pprof 分析、减少分配、并发优化与 GC 调优', 324, 0) t
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'Go性能优化' AND deleted = 0);
INSERT INTO category (name, icon, icon_url, description, sort, deleted)
SELECT * FROM (SELECT 'Go并发编程', 'go', '/images/questionLogo/go.png',
  'Go 并发编程：goroutine、channel、sync 包、Context 与并发模式', 325, 0) t
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'Go并发编程' AND deleted = 0);

-- ---------- 2. C++ 细分（挂 cpp 分组） ----------
INSERT INTO category (name, icon, icon_url, description, sort, deleted)
SELECT * FROM (SELECT 'C++基础' AS name, 'cpp' AS icon, '/images/questionLogo/cpp.png' AS icon_url,
  'C++ 基础：语法、指针引用、内存管理与编译链接', 330 AS sort, 0 AS deleted) t
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'C++基础' AND deleted = 0);
INSERT INTO category (name, icon, icon_url, description, sort, deleted)
SELECT * FROM (SELECT 'C++进阶', 'cpp', '/images/questionLogo/cpp.png',
  'C++ 进阶：智能指针、移动语义、完美转发与 RAII', 331, 0) t
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'C++进阶' AND deleted = 0);
INSERT INTO category (name, icon, icon_url, description, sort, deleted)
SELECT * FROM (SELECT 'C++新特性', 'cpp', '/images/questionLogo/cpp.png',
  'C++ 新特性：C++11/14/17/20 的 lambda、auto、协程与概念', 332, 0) t
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'C++新特性' AND deleted = 0);
INSERT INTO category (name, icon, icon_url, description, sort, deleted)
SELECT * FROM (SELECT 'C++面向对象', 'cpp', '/images/questionLogo/cpp.png',
  'C++ 面向对象：封装继承多态、虚函数表、构造析构与设计', 333, 0) t
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'C++面向对象' AND deleted = 0);
INSERT INTO category (name, icon, icon_url, description, sort, deleted)
SELECT * FROM (SELECT 'C++STL', 'cpp', '/images/questionLogo/cpp.png',
  'C++ STL：容器、迭代器、算法与仿函数的实现原理', 334, 0) t
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'C++STL' AND deleted = 0);
INSERT INTO category (name, icon, icon_url, description, sort, deleted)
SELECT * FROM (SELECT 'C++并发编程', 'cpp', '/images/questionLogo/cpp.png',
  'C++ 并发编程：thread、mutex、condition_variable、atomic 与内存模型', 335, 0) t
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'C++并发编程' AND deleted = 0);

-- ---------- 3. 绑定分组 ----------
INSERT INTO category_group_rel (category_id, group_id)
SELECT c.id, (SELECT id FROM category_group WHERE group_key = 'go') FROM category c
WHERE c.deleted = 0 AND c.name IN ('Go基础', 'Go标准库', 'Go面向对象', 'Go底层原理', 'Go性能优化', 'Go并发编程')
AND NOT EXISTS (SELECT 1 FROM category_group_rel r WHERE r.category_id = c.id
  AND r.group_id = (SELECT id FROM category_group WHERE group_key = 'go'));

INSERT INTO category_group_rel (category_id, group_id)
SELECT c.id, (SELECT id FROM category_group WHERE group_key = 'cpp') FROM category c
WHERE c.deleted = 0 AND c.name IN ('C++基础', 'C++进阶', 'C++新特性', 'C++面向对象', 'C++STL', 'C++并发编程')
AND NOT EXISTS (SELECT 1 FROM category_group_rel r WHERE r.category_id = c.id
  AND r.group_id = (SELECT id FROM category_group WHERE group_key = 'cpp'));

-- Go 并发/底层原理/性能优化 同时挂后端分组
INSERT INTO category_group_rel (category_id, group_id)
SELECT c.id, (SELECT id FROM category_group WHERE group_key = 'backend') FROM category c
WHERE c.deleted = 0 AND c.name IN ('Go并发编程', 'Go底层原理', 'Go性能优化')
AND NOT EXISTS (SELECT 1 FROM category_group_rel r WHERE r.category_id = c.id
  AND r.group_id = (SELECT id FROM category_group WHERE group_key = 'backend'));

-- ---------- 4. 核对 ----------
SELECT g.name, g.group_key, COUNT(r.id) AS cats FROM category_group g
LEFT JOIN category_group_rel r ON r.group_id = g.id
WHERE g.deleted = 0 AND g.group_key IN ('go', 'cpp')
GROUP BY g.id, g.name, g.group_key;
