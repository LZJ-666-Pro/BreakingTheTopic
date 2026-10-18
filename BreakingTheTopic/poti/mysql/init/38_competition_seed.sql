-- 38_competition_seed.sql
-- 竞赛中心种子数据：7 场真实竞赛，覆盖 进行中 / 即将开始 / 已结束 三种状态
-- 时间使用 NOW()/CURDATE() 相对表达式，新环境重跑时状态自动正确
USE poti_practice;

INSERT INTO competition
  (title, description, category_id, question_count, duration_minutes, start_time, end_time, score_per_question, status, created_by, create_time, update_time, deleted)
VALUES
  -- 进行中
  ('每周算法周赛 · 第 42 期',
   '每周例行算法专项赛：15 道精选算法题，45 分钟限时作答，比拼正确率与速度，榜单前 3 名登上周榜，欢迎挑战。',
   130, 15, 45, DATE_SUB(NOW(), INTERVAL 21 HOUR), DATE_ADD(CURDATE(), INTERVAL 86399 SECOND), 5, 1, 1, NOW(), NOW(), 0),
  ('十月 JavaScript 专项速答赛',
   '前端基础大练兵：25 道 JavaScript 高频题，20 分钟极速作答，考验基础功扎实程度，刷完即出分。',
   12, 25, 20, CURDATE(), DATE_ADD(DATE_ADD(CURDATE(), INTERVAL 2 DAY), INTERVAL 86399 SECOND), 4, 1, 1, NOW(), NOW(), 0),
  -- 即将开始
  ('Linux 运维技能挑战赛',
   '面向后端与运维方向：30 道 Linux 实战题，40 分钟完成，覆盖常用命令、Shell 脚本与故障排查场景。',
   111, 30, 40, DATE_ADD(DATE_ADD(CURDATE(), INTERVAL 4 DAY), INTERVAL 19 HOUR), DATE_ADD(DATE_ADD(CURDATE(), INTERVAL 6 DAY), INTERVAL 86399 SECOND), 3, 1, 1, NOW(), NOW(), 0),
  ('AI 大模型知识竞答（十月场）',
   '紧跟 AI 浪潮：20 道机器学习与大模型通识题，30 分钟作答，检验你对 Transformer、训练调参与推理部署的理解。',
   80, 20, 30, DATE_ADD(DATE_ADD(CURDATE(), INTERVAL 8 DAY), INTERVAL 10 HOUR), DATE_ADD(DATE_ADD(CURDATE(), INTERVAL 9 DAY), INTERVAL 22 HOUR), 5, 1, 1, NOW(), NOW(), 0),
  ('全栈系统设计争霸赛（11 月场）',
   '高阶进阶赛：15 道系统设计题，60 分钟深度作答，涵盖高并发、缓存、消息队列与分布式架构选型，巅峰对决等你来。',
   131, 15, 60, DATE_ADD(CURDATE(), INTERVAL 15 DAY), DATE_ADD(DATE_ADD(CURDATE(), INTERVAL 21 DAY), INTERVAL 86399 SECOND), 6, 1, 1, NOW(), NOW(), 0),
  -- 已结束
  ('国庆七天刷题打卡赛',
   '七天假期不间断：Vue 生态 20 道题每日打卡，完成全部 7 天打卡还可获得专属徽章，本期已圆满结束。',
   70, 20, 30, DATE_SUB(CURDATE(), INTERVAL 16 DAY), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 10 DAY), INTERVAL 86399 SECOND), 5, 1, 1, NOW(), NOW(), 0),
  ('九月新生 Python 入门赛',
   '面向新同学：20 道 Python 基础题，25 分钟作答，从语法到集合零门槛参与，本期已结束，可查看往期榜单。',
   2, 20, 25, DATE_SUB(CURDATE(), INTERVAL 37 DAY), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 32 DAY), INTERVAL 86399 SECOND), 5, 1, 1, NOW(), NOW(), 0);
