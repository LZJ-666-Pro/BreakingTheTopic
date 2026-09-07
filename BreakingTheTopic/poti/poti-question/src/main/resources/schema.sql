-- 题目分类表
-- 注意：ID 1-6 必须与 data.sql 中题目的 category_id 保持一致
INSERT IGNORE INTO `category` (`id`, `name`, `parent_id`, `sort`, `icon`, `description`) VALUES
-- 原有分类（与题目数据匹配）
(1, 'Java', 0, 1, 'java', 'Java后端开发'),
(2, 'Python', 0, 2, 'python', 'Python开发与数据科学'),
(3, 'MySQL', 0, 3, 'mysql', 'MySQL数据库'),
(4, 'Redis', 0, 4, 'redis', 'Redis缓存'),
(5, 'Spring', 0, 5, 'spring', 'Spring框架全家桶'),
(6, '消息队列', 0, 6, 'mq', 'RabbitMQ/Kafka/RocketMQ'),

-- 编程语言（新增）
(10, 'C/C++', 0, 10, 'cpp', 'C/C++系统开发'),
(11, 'Go', 0, 11, 'go', 'Go语言开发'),
(12, 'JavaScript', 0, 12, 'js', 'JavaScript前端开发'),
(13, 'PHP', 0, 13, 'php', 'PHP后端开发'),

-- 计算机基础
(20, '数据结构', 0, 20, 'ds', '数据结构与算法基础'),
(21, '操作系统', 0, 21, 'os', '操作系统原理'),
(22, '计算机网络', 0, 22, 'network', '计算机网络协议'),
(23, '计算机组成', 0, 23, 'arch', '计算机组成原理'),
(24, '编译原理', 0, 24, 'compiler', '编译器原理'),

-- 数据库（新增）
(30, 'MongoDB', 0, 30, 'mongodb', 'MongoDB文档数据库'),
(31, 'PostgreSQL', 0, 31, 'pgsql', 'PostgreSQL数据库'),

-- 后端框架（新增）
(40, 'MyBatis', 0, 40, 'mybatis', 'MyBatis持久层框架'),
(41, 'Dubbo', 0, 41, 'dubbo', 'Dubbo分布式框架'),
(42, 'Netty', 0, 42, 'netty', 'Netty网络框架'),

-- 中间件（新增）
(50, 'Nginx', 0, 50, 'nginx', 'Nginx反向代理'),
(51, 'Zookeeper', 0, 51, 'zk', 'Zookeeper分布式协调'),

-- 微服务
(60, '微服务', 0, 60, 'microservice', 'Spring Cloud微服务'),
(61, 'Docker', 0, 61, 'docker', 'Docker容器技术'),
(62, 'Kubernetes', 0, 62, 'k8s', 'K8s容器编排'),

-- 前端技术
(70, 'Vue', 0, 70, 'vue', 'Vue.js框架'),
(71, 'React', 0, 71, 'react', 'React框架'),
(72, 'Node.js', 0, 72, 'node', 'Node.js后端开发'),
(73, 'TypeScript', 0, 73, 'ts', 'TypeScript类型系统'),

-- 人工智能
(80, '机器学习', 0, 80, 'ml', '机器学习算法'),
(81, '深度学习', 0, 81, 'dl', '神经网络与深度学习'),
(82, 'NLP', 0, 82, 'nlp', '自然语言处理'),
(83, '计算机视觉', 0, 83, 'cv', '图像识别与处理'),

-- 大数据
(90, 'Hadoop', 0, 90, 'hadoop', 'Hadoop大数据平台'),
(91, 'Spark', 0, 91, 'spark', 'Spark计算引擎'),
(92, 'Flink', 0, 92, 'flink', 'Flink流处理'),
(93, '数据仓库', 0, 93, 'dw', '数据仓库建设'),

-- 安全
(100, 'Web安全', 0, 100, 'security', 'Web安全攻防'),
(101, '密码学', 0, 101, 'crypto', '密码学原理'),

-- 测试运维
(110, '软件测试', 0, 110, 'test', '软件测试方法'),
(111, 'Linux', 0, 111, 'linux', 'Linux系统运维'),
(112, 'Git', 0, 112, 'git', 'Git版本控制'),

-- 学校课程
(120, '高等数学', 0, 120, 'math', '高等数学'),
(121, '线性代数', 0, 121, 'la', '线性代数'),
(122, '概率论', 0, 122, 'prob', '概率论与数理统计'),
(123, '离散数学', 0, 123, 'dm', '离散数学'),

-- 求职面试
(130, '算法面试', 0, 130, 'algo', 'LeetCode算法题'),
(131, '系统设计', 0, 131, 'sysdesign', '系统设计面试'),
(132, 'HR面试', 0, 132, 'hr', 'HR面试技巧'),
(133, '行为面试', 0, 133, 'behavior', '行为面试问题');


