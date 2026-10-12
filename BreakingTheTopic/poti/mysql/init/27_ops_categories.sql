-- 27: 运维分组补齐分类（对齐面试鸭题库结构：13 个运维专属分类）
-- 幂等：仅当分类不存在时插入
-- 依赖：16_category_group.sql 已执行（category_group/category_group_rel 表已存在）

-- 1. 新增分类（图标 key 见注释）
INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'IT 运维工程师', 'it_ops', 'IT 运维基础、故障处理与运维规范', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'IT 运维工程师' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'DBA 数据库运维', 'dba', '数据库运维、备份恢复与性能调优', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'DBA 数据库运维' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT '网络工程师', 'network_eng', '网络架构、路由交换与故障排查', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = '网络工程师' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT '系统运维', 'sys_ops', '系统部署、日常巡检与变更管理', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = '系统运维' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'Shell', 'shell', 'Shell 语法、文本三剑客与运维脚本', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'Shell' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT '脚本编写', 'scripting', 'Python/Bash 运维脚本开发与自动化', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = '脚本编写' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT '应用服务器', 'app_server', 'Nginx/Tomcat 等应用服务器配置与调优', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = '应用服务器' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'CI/CD', 'cicd', '持续集成与持续交付流水线实践', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'CI/CD' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'IaC', 'iac', '基础设施即代码、Terraform 与 Ansible', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'IaC' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT '云服务', 'cloud_svc', '云计算基础、云厂商产品与上云实践', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = '云服务' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'Windows 系统', 'windows_sys', 'Windows Server 管理与运维', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'Windows 系统' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT '系统管理', 'sys_admin', '用户权限、服务管理与系统配置', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = '系统管理' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT '系统监控', 'sys_monitor', '监控体系、Prometheus 与告警治理', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = '系统监控' AND deleted = 0) t);

-- 2. 全部挂载到运维分组
INSERT INTO category_group_rel (category_id, group_id)
SELECT c.id, g.id FROM category c, category_group g
WHERE c.name IN ('IT 运维工程师', 'DBA 数据库运维', '网络工程师', '系统运维', 'Shell',
                 '脚本编写', '应用服务器', 'CI/CD', 'IaC', '云服务',
                 'Windows 系统', '系统管理', '系统监控') AND c.deleted = 0
  AND g.group_key = 'ops' AND g.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM category_group_rel r
    WHERE r.category_id = c.id AND r.group_id = g.id
  );
