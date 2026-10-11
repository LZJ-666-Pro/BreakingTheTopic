-- =====================================================================
-- 19_algorithm_group.sql
-- 参考 mianshiya.com 面试鸭算法板块：新增「算法」分组标签与算法细分
-- 分类（回溯/Trie/图/动态规划等 19 个），并将「算法」「数据结构」
-- 「智力题」挂入算法分组。本脚本可重复执行（幂等）。
-- =====================================================================
USE poti_question;

-- ---------------------------------------------------------------------
-- 一、新增「算法」分组标签（sort=4 排在 前端 与 人工智能 之间）
-- ---------------------------------------------------------------------
INSERT INTO category_group (name, group_key, sort, status)
SELECT * FROM (SELECT '算法' AS name, 'algorithm' AS group_key, 4 AS sort, 1 AS status) t
WHERE NOT EXISTS (SELECT 1 FROM category_group g WHERE g.group_key = 'algorithm' AND g.deleted = 0);

-- ---------------------------------------------------------------------
-- 二、新增算法细分分类
-- ---------------------------------------------------------------------
DROP TEMPORARY TABLE IF EXISTS tmp_algo_category;
CREATE TEMPORARY TABLE tmp_algo_category (
  name        varchar(100) COLLATE utf8mb4_0900_ai_ci,
  sort        int,
  icon        varchar(100) COLLATE utf8mb4_0900_ai_ci,
  description varchar(255) COLLATE utf8mb4_0900_ai_ci
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO tmp_algo_category (name, sort, icon, description) VALUES
  ('双指针',      201, 'twopointers',  '对撞指针、快慢指针、同向扫描等双指针技巧'),
  ('滑动窗口',    202, 'slidingwindow', '定长/变长滑动窗口、窗口内统计与单调性优化'),
  ('二分法',      203, 'binarysearch', '二分查找、二分答案、旋转数组与边界处理'),
  ('排序',        204, 'sort',         '快排、归并、堆排等经典排序算法与应用'),
  ('链表',        205, 'linkedlist',   '单双链表操作、反转合并、环检测与虚拟头节点'),
  ('栈',          206, 'stack',        '单调栈、表达式求值、括号匹配与栈模拟'),
  ('树',          207, 'tree',         '二叉树遍历、BST、递归分治与树的构造'),
  ('图',          208, 'graph',        'DFS/BFS、拓扑排序、最短路、最小生成树'),
  ('回溯',        209, 'backtrack',    '子集排列组合、剪枝优化与模板化回溯写法'),
  ('贪心',        210, 'greedy',       '区间调度、分配问题与贪心策略正确性证明'),
  ('动态规划',    211, 'dp',           '线性DP、背包问题、状态压缩与经典DP模型'),
  ('前缀和',      212, 'prefixsum',    '一维/二维前缀和、差分数组与区间统计技巧'),
  ('差分',        213, 'delta',        '差分数组构造、区间增减与还原应用'),
  ('位运算',      214, 'bitwise',      '异或性质、位计数、状态压缩与位技巧'),
  ('Trie',        215, 'trie',         '前缀树实现、单词查找、前缀统计与应用'),
  ('并查集',      216, 'unionfind',    '连通分量、路径压缩、按秩合并与带权并查集'),
  ('字符串',      217, 'string',       'KMP、Manacher、字符串哈希与模式匹配'),
  ('数学',        218, 'math',         '快速幂、GCD、质数筛、组合数学与数论基础'),
  ('大厂算法真题', 219, 'trophy',      '字节、阿里、腾讯等大厂高频算法真题精讲');

INSERT INTO category (name, sort, icon, description)
SELECT t.name, t.sort, t.icon, t.description
FROM tmp_algo_category t
WHERE NOT EXISTS (
  SELECT 1 FROM category c WHERE c.name = t.name AND c.deleted = 0
);

DROP TEMPORARY TABLE IF EXISTS tmp_algo_category;

-- ---------------------------------------------------------------------
-- 三、绑定分类到「算法」分组
-- ---------------------------------------------------------------------
-- 新增的算法细分分类
INSERT IGNORE INTO category_group_rel (category_id, group_id)
SELECT c.id, g.id FROM category c, category_group g
WHERE g.group_key = 'algorithm' AND g.deleted = 0 AND c.deleted = 0
  AND c.name IN ('双指针', '滑动窗口', '二分法', '排序', '链表', '栈', '树', '图',
                 '回溯', '贪心', '动态规划', '前缀和', '差分', '位运算', 'Trie',
                 '并查集', '字符串', '数学', '大厂算法真题');

-- 已有分类补挂算法分组：大厂算法真题也挂面试分组
INSERT IGNORE INTO category_group_rel (category_id, group_id)
SELECT c.id, g.id FROM category c, category_group g
WHERE g.group_key = 'interview' AND g.deleted = 0 AND c.deleted = 0
  AND c.name IN ('大厂算法真题');

-- 「算法」「数据结构」「智力题」补挂算法分组
INSERT IGNORE INTO category_group_rel (category_id, group_id)
SELECT c.id, g.id FROM category c, category_group g
WHERE g.group_key = 'algorithm' AND g.deleted = 0 AND c.deleted = 0
  AND c.name IN ('算法', '数据结构', '智力题');
