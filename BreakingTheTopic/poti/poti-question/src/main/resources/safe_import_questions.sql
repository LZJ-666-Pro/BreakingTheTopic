-- =============================================
-- 安全导入脚本 - 带冲突检查
-- =============================================

-- 步骤1：检查分类是否存在，不存在则插入
INSERT IGNORE INTO `category` (`id`, `name`, `parent_id`, `sort`, `icon`, `icon_url`, `description`) VALUES
(1, 'Java', 0, 1, 'java', 'https://img.icons8.com/color/96/java-coffee-cup-logo.png', 'Java编程语言相关题目'),
(2, 'Python', 0, 2, 'python', 'https://img.icons8.com/color/96/python.png', 'Python编程语言相关题目'),
(3, 'MySQL', 0, 3, 'mysql', 'https://img.icons8.com/color/96/mysql-logo.png', 'MySQL数据库相关题目'),
(4, 'Redis', 0, 4, 'redis', 'https://img.icons8.com/color/96/redis.png', 'Redis缓存相关题目'),
(5, 'Spring', 0, 5, 'spring', 'https://img.icons8.com/color/96/spring-logo.png', 'Spring框架相关题目'),
(6, 'MQ', 0, 6, 'mq', 'https://img.icons8.com/color/96/message-group.png', '消息队列相关题目'),
(7, '算法', 0, 7, 'algorithm', 'https://img.icons8.com/color/96/algorithm.png', '算法与数据结构题目'),
(8, '计算机网络', 0, 8, 'network', 'https://img.icons8.com/color/96/network.png', '计算机网络相关题目'),
(9, '操作系统', 0, 9, 'os', 'https://img.icons8.com/color/96/operating-system.png', '操作系统相关题目'),
(10, '设计模式', 0, 10, 'design', 'https://img.icons8.com/color/96/design.png', '设计模式相关题目');

-- 步骤2：插入题目（不指定ID，让数据库自动生成）
-- 注意：这里只插入不重复的题目，通过content字段判断
-- 如果题目内容已存在，则跳过

-- Java题目示例（前10道）
INSERT INTO `question` (`category_id`, `title`, `content`, `type`, `difficulty`, `options`, `answer`, `analysis`, `tags`, `status`)
SELECT 1, 'Java中哪个关键字用于定义类？', 'Java中哪个关键字用于定义类？', 1, 1, '["A. class", "B. struct", "C. define", "D. type"]', 'A', 'class关键字用于定义类，是Java中最基本的关键字之一。', 'Java,基础,关键字', 1
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `question` WHERE `content` = 'Java中哪个关键字用于定义类？' AND `deleted` = 0
);

INSERT INTO `question` (`category_id`, `title`, `content`, `type`, `difficulty`, `options`, `answer`, `analysis`, `tags`, `status`)
SELECT 1, 'Java中main方法的正确签名是？', 'Java中main方法的正确签名是？', 1, 1, '["A. public static void main(String args)", "B. public static void main(String[] args)", "C. public void main(String[] args)", "D. static void main(String[] args)"]', 'B', 'main方法必须是public static void，参数必须是String数组类型。', 'Java,基础,main方法', 1
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `question` WHERE `content` = 'Java中main方法的正确签名是？' AND `deleted` = 0
);

INSERT INTO `question` (`category_id`, `title`, `content`, `type`, `difficulty`, `options`, `answer`, `analysis`, `tags`, `status`)
SELECT 1, 'Java中哪个不是基本数据类型？', 'Java中哪个不是基本数据类型？', 1, 1, '["A. int", "B. boolean", "C. String", "D. char"]', 'C', 'String是引用类型，不是基本数据类型。', 'Java,基础,数据类型', 1
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `question` WHERE `content` = 'Java中哪个不是基本数据类型？' AND `deleted` = 0
);

-- 注意：完整的去重导入脚本会很长，建议使用以下方法之一：
-- 1. 使用原始的init_questions.sql（自动生成新ID，不会冲突）
-- 2. 使用Excel导入功能（更灵活）
-- 3. 先清空question表再导入（会删除现有数据，谨慎使用）
