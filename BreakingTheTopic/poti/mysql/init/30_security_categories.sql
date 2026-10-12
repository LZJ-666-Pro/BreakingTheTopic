-- 30: 安全分组补齐分类（对齐面试鸭：网络安全、安全开发、渗透测试）
-- 幂等：仅当分类不存在时插入
INSERT INTO category (name, icon, description, sort, deleted)
SELECT '网络安全', 'network_sec', '网络攻击防护、协议安全与抓包分析', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = '网络安全' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT '安全开发', 'sec_dev', '安全编码规范、漏洞防护与代码审计', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = '安全开发' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT '渗透测试', 'pentest', '渗透流程、靶场实战与报告编写', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = '渗透测试' AND deleted = 0) t);

INSERT INTO category_group_rel (category_id, group_id)
SELECT c.id, g.id FROM category c, category_group g
WHERE c.name IN ('网络安全', '安全开发', '渗透测试') AND c.deleted = 0
  AND g.group_key = 'security' AND g.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM category_group_rel r WHERE r.category_id = c.id AND r.group_id = g.id);
