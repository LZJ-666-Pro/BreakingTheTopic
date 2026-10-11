-- =====================================================================
-- 17_category_backfill.sql
-- 补齐题库缺失的分类数据：原 category 表缺少 C++、C# 分类，导致小程序
-- 题库页「C/C++」「C#.NET」分组标签下无对应数据。每个分组补充 1 个分类。
-- 本脚本可重复执行（幂等）。
-- =====================================================================
USE poti_question;

-- ---------------------------------------------------------------------
-- 一、新增缺失的分类
-- ---------------------------------------------------------------------
INSERT INTO category (name, sort, icon, description)
SELECT * FROM (SELECT 'C++' AS name, 14 AS sort, 'cpp' AS icon, 'C++ 入门语法与基础特性' AS description) t
WHERE NOT EXISTS (SELECT 1 FROM category c WHERE c.name = 'C++' AND c.deleted = 0);

INSERT INTO category (name, sort, icon, description)
SELECT * FROM (SELECT 'C#' AS name, 160 AS sort, 'csharp' AS icon, 'C# 语言基础' AS description) t
WHERE NOT EXISTS (SELECT 1 FROM category c WHERE c.name = 'C#' AND c.deleted = 0);

-- ---------------------------------------------------------------------
-- 二、绑定分类到分组标签
-- （category_group_rel 有 uk_cat_group 唯一键，INSERT IGNORE 保证幂等）
-- ---------------------------------------------------------------------
-- C/C++ 分组
INSERT IGNORE INTO category_group_rel (category_id, group_id)
SELECT c.id, g.id FROM category c, category_group g
WHERE g.group_key = 'cpp' AND g.deleted = 0 AND c.deleted = 0
  AND c.name IN ('C++');

-- C#.NET 分组
INSERT IGNORE INTO category_group_rel (category_id, group_id)
SELECT c.id, g.id FROM category c, category_group g
WHERE g.group_key = 'csharp' AND g.deleted = 0 AND c.deleted = 0
  AND c.name IN ('C#');
