-- 32: PHP 分组补齐分类（对齐面试鸭：4 个 PHP 专属分类）+ 设计模式/消息队列交叉挂载
-- 幂等：仅当分类不存在时插入
INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'PHP 基础', 'php_basic', 'PHP 语法、数组与字符串处理', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'PHP 基础' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'PHP 面向对象', 'php_oop', '类与对象、Trait 与魔术方法', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'PHP 面向对象' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'PHP 框架', 'php_framework', 'Laravel/ThinkPHP 框架原理与实践', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'PHP 框架' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'PHP 应用场景', 'php_scene', 'Web 开发、接口设计与性能优化场景', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'PHP 应用场景' AND deleted = 0) t);

INSERT INTO category_group_rel (category_id, group_id)
SELECT c.id, g.id FROM category c, category_group g
WHERE c.name IN ('PHP 基础', 'PHP 面向对象', 'PHP 框架', 'PHP 应用场景') AND c.deleted = 0
  AND g.group_key = 'php' AND g.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM category_group_rel r WHERE r.category_id = c.id AND r.group_id = g.id);

-- 设计模式 / 消息队列 交叉挂载到 PHP 分组（面试鸭结构如此）
INSERT INTO category_group_rel (category_id, group_id)
SELECT c.id, g.id FROM category c, category_group g
WHERE c.name IN ('设计模式', '消息队列') AND c.deleted = 0
  AND g.group_key = 'php' AND g.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM category_group_rel r WHERE r.category_id = c.id AND r.group_id = g.id);
