-- 34: 计算机网络分组补齐（对齐面试鸭：网络配置/网络协议/网络故障排查）+ 已有分类交叉挂载
INSERT INTO category (name, icon, description, sort, deleted)
SELECT '网络配置', 'net_config', '网络设备配置、VLAN/路由/防火墙配置实践', 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = '网络配置' AND deleted = 0) t);
INSERT INTO category (name, icon, description, sort, deleted)
SELECT '网络协议', 'net_protocol', 'TCP/IP、HTTP/HTTPS、DNS 等协议原理与细节', 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = '网络协议' AND deleted = 0) t);
INSERT INTO category (name, icon, description, sort, deleted)
SELECT '网络故障排查', 'net_troubleshoot', '抓包分析、连通性诊断与网络故障定位', 0, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = '网络故障排查' AND deleted = 0) t);

-- 挂载到计算机网络分组（新分类 + 交叉挂载已有分类）
INSERT INTO category_group_rel (category_id, group_id)
SELECT c.id, g.id FROM category c, category_group g
WHERE c.name IN ('网络配置', '网络协议', '网络故障排查', '网络工程师', '网络安全') AND c.deleted = 0
  AND g.group_key = 'network' AND g.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM category_group_rel r WHERE r.category_id = c.id AND r.group_id = g.id);
