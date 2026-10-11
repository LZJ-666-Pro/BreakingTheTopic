-- ============================================================
-- 20 号固化脚本：移动开发细分分类（对照面试鸭移动开发标签）
-- Android 系列 8 个 + 语言/跨平台 4 个 + RN/H5/鸿蒙 3 个 + Git 系列 5 个
-- 幂等：重复执行不会产生重复数据
-- ============================================================

-- ---------- 1. 新增分类（不存在时插入） ----------
INSERT INTO category (name, icon, icon_url, description, sort, deleted)
SELECT * FROM (SELECT
  'Android 基础' AS name, 'android' AS icon, '/images/questionLogo/android.png' AS icon_url,
  'Android 入门基础：四大组件、生命周期、布局与常用控件' AS description,
  300 AS sort, 0 AS deleted) t
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'Android 基础' AND deleted = 0);
INSERT INTO category (name, icon, icon_url, description, sort, deleted)
SELECT * FROM (SELECT 'Android 进阶', 'android', '/images/questionLogo/android.png',
  'Android 进阶知识：性能机制、底层原理与高级特性', 301, 0) t
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'Android 进阶' AND deleted = 0);
INSERT INTO category (name, icon, icon_url, description, sort, deleted)
SELECT * FROM (SELECT 'Android UI', 'android', '/images/questionLogo/android.png',
  'Android 界面开发：View 体系、自定义控件、动画与 Material Design', 302, 0) t
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'Android UI' AND deleted = 0);
INSERT INTO category (name, icon, icon_url, description, sort, deleted)
SELECT * FROM (SELECT 'Android 库', 'android', '/images/questionLogo/android.png',
  'Android 常用三方库：Jetpack、OkHttp、Retrofit、Glide 等', 303, 0) t
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'Android 库' AND deleted = 0);
INSERT INTO category (name, icon, icon_url, description, sort, deleted)
SELECT * FROM (SELECT 'Android 性能优化', 'android', '/images/questionLogo/android.png',
  'Android 性能优化：启动优化、内存优化、卡顿治理与包体积', 304, 0) t
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'Android 性能优化' AND deleted = 0);
INSERT INTO category (name, icon, icon_url, description, sort, deleted)
SELECT * FROM (SELECT 'Android 原理', 'android', '/images/questionLogo/android.png',
  'Android 底层原理：Binder、Handler、AMS/WMS 与系统启动流程', 305, 0) t
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'Android 原理' AND deleted = 0);
INSERT INTO category (name, icon, icon_url, description, sort, deleted)
SELECT * FROM (SELECT 'Android 数据存储', 'android', '/images/questionLogo/android.png',
  'Android 数据存储：SharedPreferences、Room、文件与 ContentProvider', 306, 0) t
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'Android 数据存储' AND deleted = 0);
INSERT INTO category (name, icon, icon_url, description, sort, deleted)
SELECT * FROM (SELECT 'Android 网络通信', 'android', '/images/questionLogo/android.png',
  'Android 网络通信：HTTP/HTTPS、OkHttp 原理、长连接与弱网优化', 307, 0) t
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'Android 网络通信' AND deleted = 0);

INSERT INTO category (name, icon, icon_url, description, sort, deleted)
SELECT * FROM (SELECT 'Swift' AS name, 'swift' AS icon, '/images/questionLogo/swift.png' AS icon_url,
  'Swift 语言：语法特性、内存管理、协议与面向协议编程', 308, 0) t
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'Swift' AND deleted = 0);
INSERT INTO category (name, icon, icon_url, description, sort, deleted)
SELECT * FROM (SELECT 'Kotlin' AS name, 'kotlin' AS icon, '/images/questionLogo/kotlin.png' AS icon_url,
  'Kotlin 语言：空安全、协程、扩展函数与 DSL', 309, 0) t
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'Kotlin' AND deleted = 0);
INSERT INTO category (name, icon, icon_url, description, sort, deleted)
SELECT * FROM (SELECT 'Flutter' AS name, 'flutter' AS icon, '/images/questionLogo/flutter.png' AS icon_url,
  'Flutter 跨端开发：Widget 体系、Dart 语言、渲染原理与状态管理', 310, 0) t
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'Flutter' AND deleted = 0);
INSERT INTO category (name, icon, icon_url, description, sort, deleted)
SELECT * FROM (SELECT 'Dart' AS name, 'dart' AS icon, '/images/questionLogo/dart.png' AS icon_url,
  'Dart 语言：异步模型、Isolate、集合与泛型', 311, 0) t
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'Dart' AND deleted = 0);

INSERT INTO category (name, icon, icon_url, description, sort, deleted)
SELECT * FROM (SELECT 'React Native', 'reactnative', '/images/questionLogo/reactnative.png',
  'React Native 跨端开发：组件体系、Bridge 原理与热更新', 312, 0) t
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'React Native' AND deleted = 0);
INSERT INTO category (name, icon, icon_url, description, sort, deleted)
SELECT * FROM (SELECT 'H5 网站开发', 'h5', '/images/questionLogo/h5.png',
  'H5 移动端网页开发：适配方案、Hybrid 混合开发与 JSSDK', 313, 0) t
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'H5 网站开发' AND deleted = 0);
INSERT INTO category (name, icon, icon_url, description, sort, deleted)
SELECT * FROM (SELECT '鸿蒙开发', 'harmonyos', '/images/questionLogo/harmonyos.png',
  'HarmonyOS 开发：ArkTS 语言、Stage 模型、UI 与分布式能力', 314, 0) t
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = '鸿蒙开发' AND deleted = 0);

INSERT INTO category (name, icon, icon_url, description, sort, deleted)
SELECT * FROM (SELECT 'Git 基础', 'git', '/images/questionLogo/git.png',
  'Git 基础：仓库操作、提交回退、分支与标签', 315, 0) t
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'Git 基础' AND deleted = 0);
INSERT INTO category (name, icon, icon_url, description, sort, deleted)
SELECT * FROM (SELECT 'Git 进阶', 'git', '/images/questionLogo/git.png',
  'Git 进阶：rebase、cherry-pick、reflog 与钩子', 316, 0) t
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'Git 进阶' AND deleted = 0);
INSERT INTO category (name, icon, icon_url, description, sort, deleted)
SELECT * FROM (SELECT 'Git 操作', 'git', '/images/questionLogo/git.png',
  'Git 常用操作场景：合并冲突、暂存、回滚与补丁', 317, 0) t
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'Git 操作' AND deleted = 0);
INSERT INTO category (name, icon, icon_url, description, sort, deleted)
SELECT * FROM (SELECT 'Git 概念', 'git', '/images/questionLogo/git.png',
  'Git 核心概念：工作区/暂存区/版本库、对象模型与引用', 318, 0) t
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'Git 概念' AND deleted = 0);
INSERT INTO category (name, icon, icon_url, description, sort, deleted)
SELECT * FROM (SELECT 'Git 协作应用', 'git', '/images/questionLogo/git.png',
  'Git 团队协作：分支模型、Code Review、PR/MR 与多人协作规范', 319, 0) t
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = 'Git 协作应用' AND deleted = 0);

-- ---------- 2. 绑定分组：移动开发（mobile） ----------
INSERT INTO category_group_rel (category_id, group_id)
SELECT c.id, 9 FROM category c
WHERE c.deleted = 0 AND c.name IN (
  'Android 基础', 'Android 进阶', 'Android UI', 'Android 库',
  'Android 性能优化', 'Android 原理', 'Android 数据存储', 'Android 网络通信',
  'Swift', 'Kotlin', 'Flutter', 'Dart',
  'React Native', 'H5 网站开发', '鸿蒙开发',
  'Git 基础', 'Git 进阶', 'Git 操作', 'Git 概念', 'Git 协作应用')
AND NOT EXISTS (SELECT 1 FROM category_group_rel r WHERE r.category_id = c.id AND r.group_id = 9);

-- ---------- 3. 附加挂载：Git 系列挂「基础学科」，H5/React Native 挂「前端」 ----------
INSERT INTO category_group_rel (category_id, group_id)
SELECT c.id, 7 FROM category c
WHERE c.deleted = 0 AND c.name IN ('Git 基础', 'Git 进阶', 'Git 操作', 'Git 概念', 'Git 协作应用')
AND NOT EXISTS (SELECT 1 FROM category_group_rel r WHERE r.category_id = c.id AND r.group_id = 7);

INSERT INTO category_group_rel (category_id, group_id)
SELECT c.id, 3 FROM category c
WHERE c.deleted = 0 AND c.name IN ('H5 网站开发', 'React Native')
AND NOT EXISTS (SELECT 1 FROM category_group_rel r WHERE r.category_id = c.id AND r.group_id = 3);

-- ---------- 4. 核对 ----------
SELECT '分类数' AS item, COUNT(*) AS val FROM category
WHERE deleted = 0 AND name IN ('Android 基础', 'Android 进阶', 'Android UI', 'Android 库',
  'Android 性能优化', 'Android 原理', 'Android 数据存储', 'Android 网络通信',
  'Swift', 'Kotlin', 'Flutter', 'Dart', 'React Native', 'H5 网站开发', '鸿蒙开发',
  'Git 基础', 'Git 进阶', 'Git 操作', 'Git 概念', 'Git 协作应用');
SELECT '绑定数' AS item, COUNT(*) AS val FROM category_group_rel r
JOIN category c ON c.id = r.category_id AND c.deleted = 0
JOIN category_group g ON g.id = r.group_id AND g.deleted = 0
WHERE c.name IN ('Android 基础', 'Android 进阶', 'Android UI', 'Android 库',
  'Android 性能优化', 'Android 原理', 'Android 数据存储', 'Android 网络通信',
  'Swift', 'Kotlin', 'Flutter', 'Dart', 'React Native', 'H5 网站开发', '鸿蒙开发',
  'Git 基础', 'Git 进阶', 'Git 操作', 'Git 概念', 'Git 协作应用');
