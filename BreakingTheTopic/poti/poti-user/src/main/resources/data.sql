-- 插入成就配置数据
INSERT INTO `achievement` (`achievement_code`, `name`, `icon`, `description`, `category`, `type`, `condition_value`, `reward_points`, `sort_order`) VALUES
('FIRST_BLOOD', '初出茅庐', '🌱', '完成第一道题目', 'practice', 'TOTAL_ANSWER', 1, 10, 1),
('PRACTICE_10', '小试牛刀', '⚔️', '累计刷题10道', 'practice', 'TOTAL_ANSWER', 10, 20, 2),
('PRACTICE_50', '初露锋芒', '🗡️', '累计刷题50道', 'practice', 'TOTAL_ANSWER', 50, 50, 3),
('PRACTICE_100', '锋芒毕露', '🗡️', '累计刷题100道', 'practice', 'TOTAL_ANSWER', 100, 100, 4),
('PRACTICE_500', '刷题达人', '📚', '累计刷题500道', 'practice', 'TOTAL_ANSWER', 500, 200, 5),
('PRACTICE_1000', '题海战术', '🌊', '累计刷题1000道', 'practice', 'TOTAL_ANSWER', 1000, 500, 6),
('PERFECT_SCORE', '百分百', '💯', '单次刷题正确率达到100%', 'practice', 'PERFECT_SCORE', 100, 50, 7),
('CONSECUTIVE_10', '连胜王者', '👑', '连续答对10道题', 'practice', 'CONSECUTIVE_CORRECT', 10, 100, 8),
('FAVORITE_10', '收藏家', '⭐', '收藏10道题目', 'social', 'TOTAL_FAVORITE', 10, 30, 9),
('NO_WRONG', '错题终结者', '✅', '错题本清空', 'practice', 'NO_WRONG_ANSWERS', 0, 50, 10),
('MORNING_BIRD', '早起鸟', '🌅', '早上6-8点刷题', 'special', 'MORNING_PRACTICE', 1, 20, 11),
('NIGHT_OWL', '夜猫子', '🦉', '晚上22-24点刷题', 'special', 'NIGHT_PRACTICE', 1, 20, 12),
('CHECKIN_7', '坚持不懈', '💪', '连续签到7天', 'special', 'CONSECUTIVE_CHECKIN', 7, 50, 13),
('CHECKIN_30', '月度之星', '🌟', '连续签到30天', 'special', 'CONSECUTIVE_CHECKIN', 30, 200, 14),
('ALL_CATEGORIES', '全能选手', '🏆', '完成所有分类的题目', 'practice', 'ALL_CATEGORIES', 10, 300, 15),
('JAVA_MASTER', 'Java大师', '☕', '完成Java所有题目', 'practice', 'CATEGORY_COMPLETE', 500, 100, 16);

-- 插入测试用户数据
INSERT IGNORE INTO `user` (`id`, `openid`, `nickname`, `avatar_url`, `gender`, `status`) VALUES
(1, 'openid_001', '张三', 'https://example.com/avatar1.jpg', 1, 1),
(2, 'openid_002', '李四', 'https://example.com/avatar2.jpg', 1, 1),
(3, 'openid_003', '王五', 'https://example.com/avatar3.jpg', 2, 1),
(4, 'openid_004', '赵六', 'https://example.com/avatar4.jpg', 1, 1),
(5, 'openid_005', '钱七', 'https://example.com/avatar5.jpg', 2, 1),
(6, 'openid_006', '孙八', 'https://example.com/avatar6.jpg', 1, 1),
(7, 'openid_007', '周九', 'https://example.com/avatar7.jpg', 2, 1),
(8, 'openid_008', '吴十', 'https://example.com/avatar8.jpg', 1, 1);

-- 插入用户统计数据
INSERT IGNORE INTO `user_stats` (`user_id`, `total_questions`, `correct_questions`, `wrong_questions`, `total_time`, `today_questions`, `today_correct`, `today_time`, `consecutive_days`, `last_practice_date`, `total_points`, `achievement_points`) VALUES
(1, 150, 120, 30, 18000, 15, 12, 1800, 7, CURDATE(), 500, 100),
(2, 200, 160, 40, 24000, 20, 16, 2400, 5, CURDATE(), 600, 120),
(3, 100, 80, 20, 12000, 10, 8, 1200, 3, CURDATE(), 400, 80),
(4, 300, 240, 60, 36000, 30, 24, 3600, 10, CURDATE(), 800, 150),
(5, 80, 64, 16, 9600, 8, 6, 960, 2, CURDATE(), 300, 60),
(6, 250, 200, 50, 30000, 25, 20, 3000, 8, CURDATE(), 700, 130),
(7, 120, 96, 24, 14400, 12, 10, 1440, 4, CURDATE(), 450, 90),
(8, 180, 144, 36, 21600, 18, 14, 2160, 6, CURDATE(), 550, 110);

-- 插入好友关系数据（用户1的好友）
INSERT IGNORE INTO `friendship` (`user_id`, `friend_id`, `status`) VALUES
(1, 2, 1),
(1, 3, 1),
(1, 4, 1),
(1, 5, 1),
(2, 1, 1),
(3, 1, 1),
(4, 1, 1),
(5, 1, 1);
