-- 添加连续签到天数字段
ALTER TABLE checkin_record ADD COLUMN streak_days INT DEFAULT 1 COMMENT '连续签到天数' AFTER checkin_time;

-- 查看表结构
DESC checkin_record;

-- 查看数据
SELECT * FROM checkin_record;
