-- ============================================================
-- 内容管理：题解（题解审核）/ 文章 / 公告
-- 题解表在 poti_question 库（关联题目），文章/公告表在 poti_admin 库
-- ============================================================

-- ------------------------------------------------------------
-- 1. 题解表（poti_question 库）
-- ------------------------------------------------------------
USE `poti_question`;

CREATE TABLE IF NOT EXISTS solution (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  question_id BIGINT NOT NULL COMMENT '关联题目ID',
  user_id BIGINT NOT NULL DEFAULT 0 COMMENT '提交用户ID，0=官方',
  author_name VARCHAR(50) NOT NULL DEFAULT '官方' COMMENT '作者昵称（冗余）',
  title VARCHAR(200) NOT NULL COMMENT '题解标题',
  content MEDIUMTEXT NOT NULL COMMENT '题解内容（Markdown）',
  is_official TINYINT NOT NULL DEFAULT 0 COMMENT '1官方题解 0用户提交',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态：0待审核 1已通过 2已拒绝',
  reject_reason VARCHAR(200) NOT NULL DEFAULT '' COMMENT '拒绝原因',
  quality TINYINT NOT NULL DEFAULT 0 COMMENT '1优质题解',
  pinned TINYINT NOT NULL DEFAULT 0 COMMENT '1置顶题解',
  view_count INT NOT NULL DEFAULT 0 COMMENT '阅读量',
  like_count INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  star_count INT NOT NULL DEFAULT 0 COMMENT '收藏数',
  report_count INT NOT NULL DEFAULT 0 COMMENT '被举报次数',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_question (question_id, status),
  KEY idx_status (status),
  KEY idx_report (report_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目题解表';

-- 示例：前两道题的官方题解（已通过）
INSERT INTO solution (question_id, user_id, author_name, title, content, is_official, status, quality, pinned, view_count, like_count, star_count)
SELECT id, 0, '官方', '官方题解：核心考点与思路拆解',
'## 思路\n\n1. 先审题，明确输入输出与边界条件；\n2. 从最直观的解法入手，再考虑优化；\n3. 注意常见易错点（空值、越界、重复元素）。\n\n## 参考解析\n\n结合选项逐项排除，重点关注概念定义与实际应用的差异。\n\n> 建议先自己做一遍再看解析，效果更好。',
1, 1, 1, 1, 0, 0, 0
FROM question WHERE deleted = 0 AND status = 1 ORDER BY id LIMIT 1;

INSERT INTO solution (question_id, user_id, author_name, title, content, is_official, status, view_count, like_count, star_count)
SELECT id, 0, '官方', '官方题解：快速记忆口诀',
'## 口诀记忆\n\n把知识点拆成 3 个关键词，分别对应「是什么、为什么、怎么用」。\n\n## 易错点\n\n- 混淆相近概念的定义范围；\n- 忽略题目中的否定词。\n\n祝刷题顺利！',
1, 1, 0, 0, 0
FROM question WHERE deleted = 0 AND status = 1 ORDER BY id LIMIT 1 OFFSET 1;

-- 示例：一条待审核的用户题解
INSERT INTO solution (question_id, user_id, author_name, title, content, is_official, status)
SELECT id, 1, '面试鸭8384', '我的个人理解版本',
'## 我的思路\n\n这道题我用另一种角度理解：先抓主干，再看修饰。\n\n## 总结\n\n多刷几遍同类题就能形成肌肉记忆。',
0, 0
FROM question WHERE deleted = 0 AND status = 1 ORDER BY id LIMIT 1;

-- ------------------------------------------------------------
-- 2. 文章表（poti_admin 库）
-- ------------------------------------------------------------
USE `poti_admin`;

CREATE TABLE IF NOT EXISTS article (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(200) NOT NULL COMMENT '文章标题',
  category VARCHAR(50) NOT NULL DEFAULT '' COMMENT '分类：学习路线/面试经验/刷题技巧/面经/技术科普',
  cover VARCHAR(255) NOT NULL DEFAULT '' COMMENT '封面图URL',
  tags VARCHAR(200) NOT NULL DEFAULT '' COMMENT '标签，逗号分隔',
  summary VARCHAR(500) NOT NULL DEFAULT '' COMMENT '摘要',
  content MEDIUMTEXT NOT NULL COMMENT '正文（Markdown）',
  author_name VARCHAR(50) NOT NULL DEFAULT '官方' COMMENT '作者',
  pinned TINYINT NOT NULL DEFAULT 0 COMMENT '1置顶',
  recommended TINYINT NOT NULL DEFAULT 0 COMMENT '1推荐',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '发布状态：0草稿 1已发布 2下架',
  view_count INT NOT NULL DEFAULT 0 COMMENT '阅读量',
  like_count INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  comment_count INT NOT NULL DEFAULT 0 COMMENT '评论数',
  publish_time DATETIME NULL COMMENT '发布时间',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_status (status),
  KEY idx_category (category),
  KEY idx_pinned (pinned)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章表';

-- 示例文章
INSERT INTO article (title, category, tags, summary, content, author_name, pinned, recommended, status, view_count, like_count, comment_count, publish_time) VALUES
('2026 版 Java 后端学习路线（从入门到offer）', '学习路线', 'Java,学习路线,校招',
'一份覆盖 Java 基础、并发、JVM、Spring、MySQL、Redis、分布式与项目实战的完整学习路线，附各阶段刷题建议。',
'## 阶段一：Java 基础\n\n- 语法、集合、泛型、IO\n- 配套刷「Java」分类基础题\n\n## 阶段二：并发与 JVM\n\n- 线程池、锁、AQS\n- JVM 内存模型与 GC\n\n## 阶段三：框架与中间件\n\n- Spring / SpringBoot / MyBatis\n- MySQL 索引与事务、Redis 缓存\n\n## 阶段四：项目与面试\n\n- 整合一个有亮点的项目\n- 每天保持 10 题节奏，配合错题本复盘',
'官方', 1, 1, 1, 0, 0, 0, NOW()),
('大厂面经分享：三轮技术面都问了什么', '面试经验', '面经,面试,后端',
'一位拿到大厂 offer 的同学复盘三轮技术面：算法手撕、八股追问、项目深挖，以及每轮的应对策略。',
'## 一面：基础 + 算法\n\n手撕链表反转、讲讲 HashMap 原理。\n\n## 二面：深挖项目\n\n项目难点、量化结果、为什么这么设计。\n\n## 三面：综合素质\n\n技术选型权衡、学习方法、职业规划。\n\n## 建议\n\n面试前把项目难点整理成「背景-方案-结果」三段式。',
'官方', 0, 1, 1, 0, 0, 0, NOW()),
('刷题技巧：如何用错题本高效提分', '刷题技巧', '刷题,错题本,方法论',
'别只顾着刷新题！讲讲错题本的正确用法：错因分类、间隔重刷、错题组卷。',
'## 1. 错因分类\n\n概念不清 / 思路错误 / 粗心大意，分别用不同颜色标记。\n\n## 2. 间隔重刷\n\n第 1、3、7 天各重做一遍，做对两次才移出错题本。\n\n## 3. 考前组卷\n\n用收藏 + 错题组合一套模拟卷，控制 90 分钟完成。',
'官方', 0, 0, 1, 0, 0, 0, NOW());

-- ------------------------------------------------------------
-- 3. 公告表（poti_admin 库）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notice (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(200) NOT NULL COMMENT '公告标题',
  content TEXT NOT NULL COMMENT '公告内容',
  display_type TINYINT NOT NULL DEFAULT 0 COMMENT '展示方式：0弹窗 1顶部横幅 2消息中心',
  target_type TINYINT NOT NULL DEFAULT 0 COMMENT '目标用户：0全部用户 1会员用户 2指定用户',
  target_user_ids VARCHAR(500) NOT NULL DEFAULT '' COMMENT '指定用户ID，逗号分隔（target_type=2时生效）',
  force_read TINYINT NOT NULL DEFAULT 0 COMMENT '1强制阅读（不可关闭）',
  closable TINYINT NOT NULL DEFAULT 1 COMMENT '1允许用户关闭',
  start_time DATETIME NULL COMMENT '生效时间（空=立即）',
  end_time DATETIME NULL COMMENT '失效时间（空=永久）',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  read_count INT NOT NULL DEFAULT 0 COMMENT '阅读人数',
  click_count INT NOT NULL DEFAULT 0 COMMENT '点击次数',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_status (status),
  KEY idx_start (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统公告表';

-- 示例公告
INSERT INTO notice (title, content, display_type, target_type, force_read, closable, start_time) VALUES
('欢迎使用破题 2.0', '题库全新升级：新增刷题专题与每日一题功能，祝面试顺利！', 0, 0, 0, 1, NOW()),
('系统维护通知', '本周日凌晨 2:00-4:00 进行系统维护，期间小程序可能短暂无法访问，请提前安排学习计划。', 2, 0, 0, 1, DATE_ADD(NOW(), INTERVAL 1 DAY));
