-- 添加options字段到interview_question_detail表
-- 用于存储面试题目的选项内容

ALTER TABLE interview_question_detail 
ADD COLUMN options TEXT COMMENT '题目选项' AFTER question_content;

-- 添加索引优化
ALTER TABLE question ADD INDEX idx_category_status (category_id, status, deleted);
ALTER TABLE practice ADD INDEX idx_user_question (user_id, question_id);
ALTER TABLE interview_record ADD INDEX idx_user_status (user_id, status);
ALTER TABLE interview_question_detail ADD INDEX idx_question_id (question_id);
