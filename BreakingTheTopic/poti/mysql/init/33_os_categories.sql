-- 33: 操作系统分组补齐（对齐面试鸭：系统故障排查）+ 运维系列分类交叉挂载
-- 幂等：仅当分类不存在时插入
INSERT INTO category (name, icon, description, sort, deleted)
SELECT '系统故障排查', 'sys_troubleshoot', '故障定位思路、日志分析与排查工具', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = '系统故障排查' AND deleted = 0) t);

INSERT INTO category_group_rel (category_id, group_id)
SELECT c.id, g.id FROM category c, category_group g
WHERE c.name = '系统故障排查' AND c.deleted = 0
  AND g.group_key = 'os' AND g.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM category_group_rel r WHERE r.category_id = c.id AND r.group_id = g.id);

-- 运维系列分类交叉挂载到操作系统分组（面试鸭结构如此）
INSERT INTO category_group_rel (category_id, group_id)
SELECT c.id, g.id FROM category c, category_group g
WHERE c.name IN ('系统运维', 'Shell', 'Linux', 'Windows 系统', '系统管理', '系统监控') AND c.deleted = 0
  AND g.group_key = 'os' AND g.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM category_group_rel r WHERE r.category_id = c.id AND r.group_id = g.id);
