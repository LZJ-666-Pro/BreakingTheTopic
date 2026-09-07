-- 添加 icon_url 和 description 字段到 category 表
ALTER TABLE category ADD COLUMN icon_url VARCHAR(500) DEFAULT NULL COMMENT '图标图片URL' AFTER icon;
ALTER TABLE category ADD COLUMN description VARCHAR(500) DEFAULT NULL COMMENT '分类描述' AFTER icon_url;

-- 更新现有分类的图标URL（示例使用网络图片，可以替换为自己的图片地址）
UPDATE category SET icon_url = 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/java/java-original.svg' WHERE name = 'Java';
UPDATE category SET icon_url = 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/python/python-original.svg' WHERE name = 'Python';
UPDATE category SET icon_url = 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/mysql/mysql-original.svg' WHERE name = 'MySQL';
UPDATE category SET icon_url = 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/redis/redis-original.svg' WHERE name = 'Redis';
UPDATE category SET icon_url = 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/spring/spring-original.svg' WHERE name = 'Spring';
UPDATE category SET icon_url = 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/rabbitmq/rabbitmq-original.svg' WHERE name = 'MQ' OR name = '消息队列';
UPDATE category SET icon_url = 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/docker/docker-original.svg' WHERE name = 'Docker';
UPDATE category SET icon_url = 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/kubernetes/kubernetes-plain.svg' WHERE name = 'Kubernetes';
UPDATE category SET icon_url = 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/linux/linux-original.svg' WHERE name = 'Linux';

-- 添加新分类示例
-- INSERT INTO category (name, icon, icon_url, sort, deleted) VALUES ('Docker', '🐳', 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/docker/docker-original.svg', 7, 0);
-- INSERT INTO category (name, icon, icon_url, sort, deleted) VALUES ('Kubernetes', '☸️', 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/kubernetes/kubernetes-plain.svg', 8, 0);
