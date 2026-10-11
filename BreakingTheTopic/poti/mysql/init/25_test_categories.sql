-- 25: 测试分组补齐分类（对齐面试鸭题库结构：9 个测试专属分类）
-- 幂等：仅当分类不存在时插入
-- 依赖：16_category_group.sql 已执行（category_group/category_group_rel 表已存在）

-- 1. 新增分类（图标 key 见注释）
INSERT INTO category (name, icon, description, sort, deleted)
SELECT '测试基础理论', 'test_theory', '测试基础概念、测试用例设计与测试流程', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = '测试基础理论' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT '自动化测试', 'automation', '自动化测试框架、Selenium 与测试脚本开发', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = '自动化测试' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT '接口测试', 'api_test', '接口测试方法、Postman 与协议断言', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = '接口测试' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'Web 测试', 'web_test', 'Web 功能测试、兼容性与浏览器调试', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'Web 测试' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT '移动端测试', 'mobile_test', 'App 测试、专项测试与弱网场景', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = '移动端测试' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT '测试工具', 'test_tools', '常用测试工具、抓包与调试利器', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = '测试工具' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT '性能测试', 'performance_test', '性能测试指标、JMeter 与压测方案', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = '性能测试' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT '安全测试', 'security_test', '安全测试基础、漏洞类型与防护', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = '安全测试' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT '测试场景', 'test_scene', '测试场景分析、缺陷定位与测试思维', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = '测试场景' AND deleted = 0) t);

-- 2. 全部挂载到测试分组
INSERT INTO category_group_rel (category_id, group_id)
SELECT c.id, g.id FROM category c, category_group g
WHERE c.name IN ('测试基础理论', '自动化测试', '接口测试', 'Web 测试', '移动端测试',
                 '测试工具', '性能测试', '安全测试', '测试场景') AND c.deleted = 0
  AND g.group_key = 'test' AND g.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM category_group_rel r
    WHERE r.category_id = c.id AND r.group_id = g.id
  );
