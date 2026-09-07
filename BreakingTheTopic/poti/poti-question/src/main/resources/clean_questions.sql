-- =============================================
-- 清理错误格式的题目数据
-- =============================================

-- 删除选项格式错误的题目（选项不以 [ 开头的）
DELETE FROM `question` 
WHERE `options` NOT LIKE '[%' OR `options` LIKE '[A.%';

-- 删除操作系统和设计模式的题目（准备重新导入）
DELETE FROM `question` 
WHERE `category_id` IN (9, 10);

-- 查看剩余题目
SELECT COUNT(*) as total_count FROM `question`;
SELECT category_id, COUNT(*) as count FROM `question` GROUP BY category_id;
