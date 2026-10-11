-- ============================================================
-- 21 号固化脚本：分组标签对齐面试鸭（补 10 个缺失分组 + basic 改名）
-- 全部挂载已有分类，无新增分类/题目
-- 幂等：重复执行不会产生重复数据
-- ============================================================

-- ---------- 0. 基础学科 → 计算机基础（对齐面试鸭叫法） ----------
UPDATE category_group SET name = '计算机基础' WHERE group_key = 'basic' AND name = '基础学科';

-- ---------- 1. 新增分组（不存在时插入，sort 从 30 起） ----------
INSERT INTO category_group (name, group_key, sort, status, deleted)
SELECT * FROM (SELECT '真实面经' AS name, 'realexperience' AS group_key, 30 AS sort, 1 AS status, 0 AS deleted) t
WHERE NOT EXISTS (SELECT 1 FROM category_group WHERE group_key = 'realexperience' AND deleted = 0);
INSERT INTO category_group (name, group_key, sort, status, deleted)
SELECT * FROM (SELECT '数据库', 'database', 31, 1, 0) t
WHERE NOT EXISTS (SELECT 1 FROM category_group WHERE group_key = 'database' AND deleted = 0);
INSERT INTO category_group (name, group_key, sort, status, deleted)
SELECT * FROM (SELECT '计算机网络', 'network', 32, 1, 0) t
WHERE NOT EXISTS (SELECT 1 FROM category_group WHERE group_key = 'network' AND deleted = 0);
INSERT INTO category_group (name, group_key, sort, status, deleted)
SELECT * FROM (SELECT '操作系统', 'os', 33, 1, 0) t
WHERE NOT EXISTS (SELECT 1 FROM category_group WHERE group_key = 'os' AND deleted = 0);
INSERT INTO category_group (name, group_key, sort, status, deleted)
SELECT * FROM (SELECT '项目', 'project', 34, 1, 0) t
WHERE NOT EXISTS (SELECT 1 FROM category_group WHERE group_key = 'project' AND deleted = 0);
INSERT INTO category_group (name, group_key, sort, status, deleted)
SELECT * FROM (SELECT '编程语言', 'language', 35, 1, 0) t
WHERE NOT EXISTS (SELECT 1 FROM category_group WHERE group_key = 'language' AND deleted = 0);
INSERT INTO category_group (name, group_key, sort, status, deleted)
SELECT * FROM (SELECT 'HR面试', 'hr', 36, 1, 0) t
WHERE NOT EXISTS (SELECT 1 FROM category_group WHERE group_key = 'hr' AND deleted = 0);
INSERT INTO category_group (name, group_key, sort, status, deleted)
SELECT * FROM (SELECT '考研', 'kaoyan', 37, 1, 0) t
WHERE NOT EXISTS (SELECT 1 FROM category_group WHERE group_key = 'kaoyan' AND deleted = 0);
INSERT INTO category_group (name, group_key, sort, status, deleted)
SELECT * FROM (SELECT '校招热题', 'campus', 38, 1, 0) t
WHERE NOT EXISTS (SELECT 1 FROM category_group WHERE group_key = 'campus' AND deleted = 0);
INSERT INTO category_group (name, group_key, sort, status, deleted)
SELECT * FROM (SELECT '公司题库', 'company', 39, 1, 0) t
WHERE NOT EXISTS (SELECT 1 FROM category_group WHERE group_key = 'company' AND deleted = 0);

-- ---------- 2. 绑定已有分类 ----------
-- 真实面经：面经/软素质类
INSERT INTO category_group_rel (category_id, group_id)
SELECT c.id, (SELECT id FROM category_group WHERE group_key = 'realexperience') FROM category c
WHERE c.deleted = 0 AND c.name IN ('大厂面经', '行为面试', 'HR面试', '算法面试')
AND NOT EXISTS (SELECT 1 FROM category_group_rel r WHERE r.category_id = c.id
  AND r.group_id = (SELECT id FROM category_group WHERE group_key = 'realexperience'));

-- 数据库
INSERT INTO category_group_rel (category_id, group_id)
SELECT c.id, (SELECT id FROM category_group WHERE group_key = 'database') FROM category c
WHERE c.deleted = 0 AND c.name IN ('MySQL', 'Redis', 'MongoDB', 'PostgreSQL')
AND NOT EXISTS (SELECT 1 FROM category_group_rel r WHERE r.category_id = c.id
  AND r.group_id = (SELECT id FROM category_group WHERE group_key = 'database'));

-- 计算机网络
INSERT INTO category_group_rel (category_id, group_id)
SELECT c.id, (SELECT id FROM category_group WHERE group_key = 'network') FROM category c
WHERE c.deleted = 0 AND c.name = '计算机网络'
AND NOT EXISTS (SELECT 1 FROM category_group_rel r WHERE r.category_id = c.id
  AND r.group_id = (SELECT id FROM category_group WHERE group_key = 'network'));

-- 操作系统
INSERT INTO category_group_rel (category_id, group_id)
SELECT c.id, (SELECT id FROM category_group WHERE group_key = 'os') FROM category c
WHERE c.deleted = 0 AND c.name = '操作系统'
AND NOT EXISTS (SELECT 1 FROM category_group_rel r WHERE r.category_id = c.id
  AND r.group_id = (SELECT id FROM category_group WHERE group_key = 'os'));

-- 项目：场景题与系统设计类
INSERT INTO category_group_rel (category_id, group_id)
SELECT c.id, (SELECT id FROM category_group WHERE group_key = 'project') FROM category c
WHERE c.deleted = 0 AND c.name IN ('后端场景题', '前端场景题', '系统设计', '设计模式')
AND NOT EXISTS (SELECT 1 FROM category_group_rel r WHERE r.category_id = c.id
  AND r.group_id = (SELECT id FROM category_group WHERE group_key = 'project'));

-- 编程语言
INSERT INTO category_group_rel (category_id, group_id)
SELECT c.id, (SELECT id FROM category_group WHERE group_key = 'language') FROM category c
WHERE c.deleted = 0 AND c.name IN ('Java', 'Python', 'Go', 'C++', 'C#', 'PHP',
  'JavaScript', 'TypeScript', 'Kotlin', 'Swift', 'Dart')
AND NOT EXISTS (SELECT 1 FROM category_group_rel r WHERE r.category_id = c.id
  AND r.group_id = (SELECT id FROM category_group WHERE group_key = 'language'));

-- HR面试
INSERT INTO category_group_rel (category_id, group_id)
SELECT c.id, (SELECT id FROM category_group WHERE group_key = 'hr') FROM category c
WHERE c.deleted = 0 AND c.name IN ('HR面试', '行为面试')
AND NOT EXISTS (SELECT 1 FROM category_group_rel r WHERE r.category_id = c.id
  AND r.group_id = (SELECT id FROM category_group WHERE group_key = 'hr'));

-- 考研：数学类
INSERT INTO category_group_rel (category_id, group_id)
SELECT c.id, (SELECT id FROM category_group WHERE group_key = 'kaoyan') FROM category c
WHERE c.deleted = 0 AND c.name IN ('高等数学', '线性代数', '概率论', '离散数学')
AND NOT EXISTS (SELECT 1 FROM category_group_rel r WHERE r.category_id = c.id
  AND r.group_id = (SELECT id FROM category_group WHERE group_key = 'kaoyan'));

-- 校招热题：高频题分类
INSERT INTO category_group_rel (category_id, group_id)
SELECT c.id, (SELECT id FROM category_group WHERE group_key = 'campus') FROM category c
WHERE c.deleted = 0 AND c.name IN ('Java 集合', 'Java 并发', 'MySQL', 'Redis', '计算机网络',
  '操作系统', '算法面试', '数据结构')
AND NOT EXISTS (SELECT 1 FROM category_group_rel r WHERE r.category_id = c.id
  AND r.group_id = (SELECT id FROM category_group WHERE group_key = 'campus'));

-- 公司题库：大厂真题类
INSERT INTO category_group_rel (category_id, group_id)
SELECT c.id, (SELECT id FROM category_group WHERE group_key = 'company') FROM category c
WHERE c.deleted = 0 AND c.name IN ('大厂面经', '大厂算法真题')
AND NOT EXISTS (SELECT 1 FROM category_group_rel r WHERE r.category_id = c.id
  AND r.group_id = (SELECT id FROM category_group WHERE group_key = 'company'));

-- ---------- 3. 核对 ----------
SELECT g.name, g.group_key, COUNT(r.id) AS cats FROM category_group g
LEFT JOIN category_group_rel r ON r.group_id = g.id
WHERE g.deleted = 0 AND g.group_key IN ('realexperience','database','network','os','project',
  'language','hr','kaoyan','campus','company')
GROUP BY g.id, g.name, g.group_key ORDER BY g.sort;
