-- 26: 人工智能分组补齐分类（对齐面试鸭题库结构：10 个 AI 专属分类）
-- 幂等：仅当分类不存在时插入
-- 依赖：16_category_group.sql 已执行（category_group/category_group_rel 表已存在）

-- 1. 新增分类（图标 key 见注释）
INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'AI 大模型', 'ai_model', '主流大模型原理、训练微调与评测对比', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'AI 大模型' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'Claude Code', 'claude_code', 'Claude Code 使用技巧、工作流与实战场景', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'Claude Code' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT '强化学习', 'rl', '强化学习基础、MDP、Q-Learning 与策略梯度', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = '强化学习' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'AI 应用', 'ai_app', 'AI 产品应用场景、落地案例与效果评估', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'AI 应用' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'Transformer', 'transformer', 'Transformer 架构、注意力机制与位置编码', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'Transformer' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'LangChain', 'langchain', 'LangChain 框架、Chain 编排与 Agent 开发', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'LangChain' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'Prompt 提示词工程', 'prompt_eng', '提示词设计原则、技巧与优化方法', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'Prompt 提示词工程' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'RAG 检索增强生成', 'rag', 'RAG 架构、向量检索与知识库问答', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'RAG 检索增强生成' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'Python 代码分析', 'python_analysis', 'Python 代码阅读分析、坑点与输出预测', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'Python 代码分析' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'Python 手写代码', 'python_handwrite', 'Python 手写实现题、算法与工程代码', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'Python 手写代码' AND deleted = 0) t);

-- 2. 全部挂载到人工智能分组
INSERT INTO category_group_rel (category_id, group_id)
SELECT c.id, g.id FROM category c, category_group g
WHERE c.name IN ('AI 大模型', 'Claude Code', '强化学习', 'AI 应用', 'Transformer',
                 'LangChain', 'Prompt 提示词工程', 'RAG 检索增强生成',
                 'Python 代码分析', 'Python 手写代码') AND c.deleted = 0
  AND g.group_key = 'ai' AND g.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM category_group_rel r
    WHERE r.category_id = c.id AND r.group_id = g.id
  );

-- 3. Python 两个分类同时挂载到 Python 分组（与机器学习等既有交叉挂载惯例一致）
INSERT INTO category_group_rel (category_id, group_id)
SELECT c.id, g.id FROM category c, category_group g
WHERE c.name IN ('Python 代码分析', 'Python 手写代码') AND c.deleted = 0
  AND g.group_key = 'python' AND g.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM category_group_rel r
    WHERE r.category_id = c.id AND r.group_id = g.id
  );
