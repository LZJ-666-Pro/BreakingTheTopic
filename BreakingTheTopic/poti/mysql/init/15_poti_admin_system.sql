-- ============================================================
-- 系统设置：权限管理（角色）/ 操作日志 / 系统配置预置项
-- 均在 poti_admin 库
-- ============================================================
USE `poti_admin`;

-- 1. 角色表（permissions 为菜单路由 index，逗号分隔；空字符串=全部权限）
CREATE TABLE IF NOT EXISTS sys_role (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL COMMENT '角色名称',
  code VARCHAR(50) NOT NULL COMMENT '角色编码，super=超级管理员',
  permissions TEXT NULL COMMENT '菜单权限（路由index逗号分隔，空=全部）',
  remark VARCHAR(200) NOT NULL DEFAULT '' COMMENT '备注',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0禁用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='后台角色表';

-- 2. 管理员表补充角色关联（MySQL 8 不支持 IF NOT EXISTS，通过存储过程判断）
DROP PROCEDURE IF EXISTS add_role_id;
DELIMITER $$
CREATE PROCEDURE add_role_id()
BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'sys_admin' AND column_name = 'role_id') THEN
    ALTER TABLE sys_admin ADD COLUMN role_id BIGINT NULL COMMENT '关联角色ID' AFTER role;
  END IF;
END$$
DELIMITER ;
CALL add_role_id();
DROP PROCEDURE IF EXISTS add_role_id;

-- 预置角色（幂等：按 code 去重）
INSERT INTO sys_role (name, code, permissions, remark, status)
SELECT '超级管理员', 'super', '', '拥有系统全部权限', 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE code = 'super');
INSERT INTO sys_role (name, code, permissions, remark, status)
SELECT '内容运营', 'content_ops', '/dashboard,/content/solution-review,/content/article,/content/notice,/special/manage,/special/daily,/stats', '负责题解审核、文章公告与专题每日一题运营', 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE code = 'content_ops');
INSERT INTO sys_role (name, code, permissions, remark, status)
SELECT '题目审核员', 'question_reviewer', '/dashboard,/question/list,/question/category,/question/tag,/special/manage,/special/daily', '负责题目、分类与专题维护', 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE code = 'question_reviewer');
INSERT INTO sys_role (name, code, permissions, remark, status)
SELECT '题库管理员', 'question_manager', '/dashboard,/question/list,/question/add,/question/category,/question/tag,/special/manage,/special/daily,/ai-tasks', '负责题库建设、批量出题与AI生成任务管理', 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE code = 'question_manager');
INSERT INTO sys_role (name, code, permissions, remark, status)
SELECT '用户客服', 'customer_service', '/dashboard,/user/list,/user/feedback', '负责用户咨询、反馈处理与举报跟进', 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE code = 'customer_service');
INSERT INTO sys_role (name, code, permissions, remark, status)
SELECT '数据分析员', 'data_analyst', '/dashboard,/stats', '负责平台运营数据统计与查看', 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE code = 'data_analyst');
INSERT INTO sys_role (name, code, permissions, remark, status)
SELECT '系统运维', 'sys_ops', '/dashboard,/settings/config,/settings/logs', '负责系统配置维护与操作日志审计（无权限管理）', 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE code = 'sys_ops');

-- 内置超管账号关联超级管理员角色
UPDATE sys_admin SET role_id = (SELECT id FROM sys_role WHERE code = 'super' AND deleted = 0 LIMIT 1) WHERE username = 'admin' AND (role_id IS NULL OR role_id = 0);

-- 3. 操作日志表
CREATE TABLE IF NOT EXISTS sys_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  admin_id BIGINT NOT NULL DEFAULT 0 COMMENT '操作人ID',
  admin_name VARCHAR(50) NOT NULL DEFAULT '未知' COMMENT '操作人昵称',
  module VARCHAR(50) NOT NULL DEFAULT '' COMMENT '操作模块（题目管理/用户管理等）',
  action VARCHAR(50) NOT NULL DEFAULT '' COMMENT '动作（新增/更新/删除）',
  target VARCHAR(200) NOT NULL DEFAULT '' COMMENT '操作对象（接口路径）',
  detail VARCHAR(1000) NOT NULL DEFAULT '' COMMENT '请求参数摘要',
  result TINYINT NOT NULL DEFAULT 1 COMMENT '1成功 0失败',
  ip VARCHAR(50) NOT NULL DEFAULT '' COMMENT '操作IP',
  cost_ms INT NOT NULL DEFAULT 0 COMMENT '耗时(毫秒)',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_admin (admin_id),
  KEY idx_module (module),
  KEY idx_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='后台操作日志表';

-- 4. 系统配置预置项（key-value，sys_config 已有表）
INSERT INTO sys_config (config_key, config_value, config_name, description, status)
SELECT 'site_name', '破题', '网站名称', '平台名称，展示在小程序与后台', 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'site_name');
INSERT INTO sys_config (config_key, config_value, config_name, description, status)
SELECT 'site_logo', '/images/log.png', '网站Logo', '平台Logo图片地址', 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'site_logo');
INSERT INTO sys_config (config_key, config_value, config_name, description, status)
SELECT 'icp_number', '', '备案号', 'ICP备案号，展示在关于页面', 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'icp_number');
INSERT INTO sys_config (config_key, config_value, config_name, description, status)
SELECT 'contact_email', '', '联系邮箱', '官方联系方式', 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'contact_email');
INSERT INTO sys_config (config_key, config_value, config_name, description, status)
SELECT 'register_enabled', 'true', '开放注册', '关闭后小程序端无法注册新用户', 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'register_enabled');
INSERT INTO sys_config (config_key, config_value, config_name, description, status)
SELECT 'login_captcha_enabled', 'true', '登录验证码开关', '控制后台登录是否需要验证码', 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'login_captcha_enabled');
INSERT INTO sys_config (config_key, config_value, config_name, description, status)
SELECT 'daily_question_time', '00:00', '每日一题更新时间', '每天自动切换每日一题的时间点', 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'daily_question_time');
INSERT INTO sys_config (config_key, config_value, config_name, description, status)
SELECT 'default_page_size', '10', '默认分页条数', '列表页默认每页条数', 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'default_page_size');
INSERT INTO sys_config (config_key, config_value, config_name, description, status)
SELECT 'upload_max_size', '10', '上传大小限制(MB)', '图片/文件上传大小上限', 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'upload_max_size');
INSERT INTO sys_config (config_key, config_value, config_name, description, status)
SELECT 'competition_enabled', 'false', '竞赛功能开关', '关闭后隐藏小程序竞赛入口', 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'competition_enabled');
INSERT INTO sys_config (config_key, config_value, config_name, description, status)
SELECT 'discussion_enabled', 'true', '讨论区开关', '关闭后题目详情隐藏讨论区', 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'discussion_enabled');
INSERT INTO sys_config (config_key, config_value, config_name, description, status)
SELECT 'checkin_reward_enabled', 'true', '签到奖励开关', '关闭后签到不发放奖励', 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'checkin_reward_enabled');
INSERT INTO sys_config (config_key, config_value, config_name, description, status)
SELECT 'vip_price', '1.99', '会员价格(元/月)', '月卡价格，小程序会员中心实时读取', 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'vip_price');
INSERT INTO sys_config (config_key, config_value, config_name, description, status)
SELECT 'vip_price_quarter', '4.99', '季卡价格(元)', '会员季卡价格，小程序会员中心实时读取', 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'vip_price_quarter');
INSERT INTO sys_config (config_key, config_value, config_name, description, status)
SELECT 'vip_price_year', '9.99', '年卡价格(元)', '会员年卡价格，小程序会员中心实时读取', 1 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'vip_price_year');
