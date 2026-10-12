-- 28: 游戏开发分组补齐（对齐面试鸭：游戏服务端开发）
-- 幂等：仅当分类不存在时插入
INSERT INTO category (name, icon, description, sort, deleted)
SELECT '游戏服务端开发', 'game_server', '游戏服务器架构、网络同步与帧同步', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = '游戏服务端开发' AND deleted = 0) t);

INSERT INTO category_group_rel (category_id, group_id)
SELECT c.id, g.id FROM category c, category_group g
WHERE c.name = '游戏服务端开发' AND c.deleted = 0
  AND g.group_key = 'game' AND g.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM category_group_rel r WHERE r.category_id = c.id AND r.group_id = g.id);
