-- 31: C#.NET 分组补齐分类（对齐面试鸭：8 个 C# 细分分类）
-- 幂等：仅当分类不存在时插入
INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'C# 基础', 'csharp_basic', 'C# 语法、类型系统与基础概念', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'C# 基础' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'C# 集合', 'csharp_collection', '集合类型、泛型与 LINQ 操作', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'C# 集合' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'C# 面向对象', 'csharp_oop', '封装继承多态、接口与设计模式', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'C# 面向对象' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'C# 并发库', 'csharp_parallel', 'Task、async/await 与并行编程', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'C# 并发库' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'C# 框架', 'csharp_framework', 'ASP.NET Core、EF Core 与依赖注入', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'C# 框架' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'C# 底层原理', 'csharp_lowlevel', 'CLR、GC 与运行时机制', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'C# 底层原理' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'C# WPF', 'csharp_wpf', 'WPF 布局、数据绑定与 MVVM', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'C# WPF' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'C# .NET', 'csharp_dotnet', '.NET 运行时、跨平台与生态', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'C# .NET' AND deleted = 0) t);

INSERT INTO category_group_rel (category_id, group_id)
SELECT c.id, g.id FROM category c, category_group g
WHERE c.name IN ('C# 基础', 'C# 集合', 'C# 面向对象', 'C# 并发库', 'C# 框架',
                 'C# 底层原理', 'C# WPF', 'C# .NET') AND c.deleted = 0
  AND g.group_key = 'csharp' AND g.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM category_group_rel r WHERE r.category_id = c.id AND r.group_id = g.id);
