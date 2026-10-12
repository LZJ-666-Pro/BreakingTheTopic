-- 35: 项目分组补齐（对齐面试鸭：19 个项目类分类）
INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'AI 零代码应用生成平台项目', 'proj_zero_code', '零代码生成 Web 应用的 AI 平台项目面试', 0, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'AI 零代码应用生成平台项目' AND deleted = 0) t);
INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'AI 超级智能体项目', 'proj_agent', '自主规划与工具调用的 AI 智能体项目面试', 0, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'AI 超级智能体项目' AND deleted = 0) t);
INSERT INTO category (name, icon, description, sort, deleted)
SELECT '智能协同云图库项目', 'proj_cloud_album', '团队协同素材管理与云图库项目面试', 0, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = '智能协同云图库项目' AND deleted = 0) t);
INSERT INTO category (name, icon, description, sort, deleted)
SELECT '面试刷题平台项目', 'proj_interview_platform', '面试刷题平台类项目面试（题库/刷题/统计）', 0, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = '面试刷题平台项目' AND deleted = 0) t);
INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'AI 答题平台项目', 'proj_ai_quiz', 'AI 驱动的答题平台项目面试', 0, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'AI 答题平台项目' AND deleted = 0) t);
INSERT INTO category (name, icon, description, sort, deleted)
SELECT '代码生成器平台项目', 'proj_codegen', '代码生成器平台项目面试', 0, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = '代码生成器平台项目' AND deleted = 0) t);
INSERT INTO category (name, icon, description, sort, deleted)
SELECT '伙伴匹配项目', 'proj_partner_match', '基于标签匹配的伙伴匹配项目面试', 0, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = '伙伴匹配项目' AND deleted = 0) t);
INSERT INTO category (name, icon, description, sort, deleted)
SELECT '智能 BI 项目', 'proj_bi', 'AI 智能数据分析与图表生成 BI 项目面试', 0, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = '智能 BI 项目' AND deleted = 0) t);
INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'API 开放平台项目', 'proj_api_open', 'API 开放平台项目面试（开放接口/计费/网关）', 0, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'API 开放平台项目' AND deleted = 0) t);
INSERT INTO category (name, icon, description, sort, deleted)
SELECT '聚合搜索项目', 'proj_agg_search', '聚合搜索项目面试（多数据源整合检索）', 0, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = '聚合搜索项目' AND deleted = 0) t);
INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'OJ 判题项目', 'proj_oj', '在线判题 OJ 项目面试（沙箱/判题机）', 0, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'OJ 判题项目' AND deleted = 0) t);
INSERT INTO category (name, icon, description, sort, deleted)
SELECT '手写 RPC 框架项目', 'proj_rpc', '手写 RPC 框架项目面试（注册中心/序列化/负载均衡）', 0, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = '手写 RPC 框架项目' AND deleted = 0) t);
INSERT INTO category (name, icon, description, sort, deleted)
SELECT '用户中心项目', 'proj_user_center', '用户中心项目面试（注册登录/会话管理）', 0, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = '用户中心项目' AND deleted = 0) t);
INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'GitHub 文档翻译工具项目', 'proj_gh_translate', 'GitHub 文档翻译工具项目面试', 0, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'GitHub 文档翻译工具项目' AND deleted = 0) t);
INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'AI 爆款文章创作器项目', 'proj_ai_writer', 'AI 爆款文章生成工具项目面试', 0, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'AI 爆款文章创作器项目' AND deleted = 0) t);
INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'AI 万能视频下载总结器项目', 'proj_video_sum', '视频下载与内容总结 AI 工具项目面试', 0, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'AI 万能视频下载总结器项目' AND deleted = 0) t);
INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'AI 大模型评测平台项目', 'proj_llm_eval', '大模型评测平台项目面试（基准/指标/对比）', 0, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'AI 大模型评测平台项目' AND deleted = 0) t);
INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'AI 热点监控工具项目', 'proj_hot_monitor', '热点趋势监控工具项目面试（爬虫/推送）', 0, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'AI 热点监控工具项目' AND deleted = 0) t);
INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'AI 网关平台项目', 'proj_gateway', 'AI 网关平台项目面试（转发/鉴权/限流计费）', 0, 0 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'AI 网关平台项目' AND deleted = 0) t);

INSERT INTO category_group_rel (category_id, group_id)
SELECT c.id, g.id FROM category c, category_group g
WHERE g.group_key = 'project' AND g.deleted = 0 AND c.deleted = 0
  AND c.name IN ('AI 零代码应用生成平台项目', 'AI 超级智能体项目', '智能协同云图库项目', '面试刷题平台项目', 'AI 答题平台项目', '代码生成器平台项目', '伙伴匹配项目', '智能 BI 项目', 'API 开放平台项目', '聚合搜索项目', 'OJ 判题项目', '手写 RPC 框架项目', '用户中心项目', 'GitHub 文档翻译工具项目', 'AI 爆款文章创作器项目', 'AI 万能视频下载总结器项目', 'AI 大模型评测平台项目', 'AI 热点监控工具项目', 'AI 网关平台项目')
  AND NOT EXISTS (SELECT 1 FROM category_group_rel r WHERE r.category_id = c.id AND r.group_id = g.id);
