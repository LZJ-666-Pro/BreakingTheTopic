-- 24: 区块链分组补齐分类（对齐面试鸭题库结构：Solidity / Web3 开发 / 区块链开发 / 智能合约开发）
-- 幂等：仅当分类不存在时插入
-- 依赖：16_category_group.sql 已执行（category_group/category_group_rel 表已存在）

-- 1. 新增分类（图标 key: solidity/web3/blockchain/contract）
INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'Solidity', 'solidity', 'Solidity 语法、合约编写、ERC 标准与安全', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'Solidity' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT 'Web3 开发', 'web3', 'Web3 生态、去中心化应用与钱包交互开发', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = 'Web3 开发' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT '区块链开发', 'blockchain', '区块链原理、共识机制与 DApp 开发', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = '区块链开发' AND deleted = 0) t);

INSERT INTO category (name, icon, description, sort, deleted)
SELECT '智能合约开发', 'contract', '智能合约设计、Solidity 合约编写、安全与升级模式', 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM category WHERE name = '智能合约开发' AND deleted = 0) t);

-- 2. 全部挂载到区块链分组
INSERT INTO category_group_rel (category_id, group_id)
SELECT c.id, g.id FROM category c, category_group g
WHERE c.name IN ('Solidity', 'Web3 开发', '区块链开发', '智能合约开发') AND c.deleted = 0
  AND g.group_key = 'blockchain' AND g.deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM category_group_rel r
    WHERE r.category_id = c.id AND r.group_id = g.id
  );
