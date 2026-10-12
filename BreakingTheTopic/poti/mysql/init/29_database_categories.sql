-- 29: 数据库分组补齐分类（对齐面试鸭：10 个缺失分类）+ DBA 数据库运维交叉挂载
-- 幂等：仅当分类不存在时插入
INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'Oracle 数据库', 'oracle_db', 'Oracle 体系结构、PL/SQL 与调优', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'Oracle 数据库' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT '数据备份恢复', 'backup_restore', '备份策略、恢复演练与容灾', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = '数据备份恢复' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT '数据库高可用', 'db_ha', '主从复制、故障切换与集群方案', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = '数据库高可用' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT '数据库体系结构', 'db_arch', '存储引擎、内存结构与进程架构', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = '数据库体系结构' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT '数据库性能优化', 'db_perf', '索引优化、执行计划与慢查询治理', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = '数据库性能优化' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'SQL', 'sql', 'SQL 语法、函数与查询基础', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'SQL' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'SQL 电商场景', 'sql_ecom', '电商业务 SQL 建模与查询实战', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'SQL 电商场景' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'SQL 网站场景', 'sql_web', '网站业务 SQL 设计与查询实战', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'SQL 网站场景' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'SQL 基础查询', 'sql_basic', '单表查询、过滤排序与聚合', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'SQL 基础查询' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'SQL 进阶查询', 'sql_adv', '多表连接、子查询与窗口函数', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'SQL 进阶查询' AND deleted = 0) t);

-- 全部挂载到数据库分组
INSERT INTO category_group_rel (category_id, group_id)
SELECT c.id, g.id FROM category c, category_group g
WHERE c.name IN ('Oracle 数据库', '数据备份恢复', '数据库高可用', '数据库体系结构', '数据库性能优化',
                 'SQL', 'SQL 电商场景', 'SQL 网站场景', 'SQL 基础查询', 'SQL 进阶查询') AND c.deleted = 0
  AND g.group_key = 'database' AND g.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM category_group_rel r WHERE r.category_id = c.id AND r.group_id = g.id);

-- DBA 数据库运维 交叉挂载到数据库分组（面试鸭归在数据库下）
INSERT INTO category_group_rel (category_id, group_id)
SELECT c.id, g.id FROM category c, category_group g
WHERE c.name = 'DBA 数据库运维' AND c.deleted = 0
  AND g.group_key = 'database' AND g.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM category_group_rel r WHERE r.category_id = c.id AND r.group_id = g.id);
