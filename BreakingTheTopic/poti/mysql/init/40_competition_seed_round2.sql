-- ============================================================
-- 40_competition_seed_round2.sql  竞赛种子数据（第二批）
-- 7 场竞赛覆盖三种状态：进行中 2 / 报名中 3 / 已结束 2，
-- 时间全部使用 NOW()/CURDATE() 相对表达式——任何时间重跑，
-- 状态推导（报名中=未结束未报名 / 进行中=未结束已报名 / 已结束=过期）自动正确。
-- 幂等：按 title 去重，重复执行不产生重复数据。
-- ============================================================

USE poti_practice;

-- ① 进行中：周末 Java 并发编程限时赛（今天 09:00 开赛，窗口至明天）
INSERT INTO competition (title, description, category_id, question_count, duration_minutes, start_time, end_time, score_per_question, status)
SELECT '周末 Java 并发编程限时赛',
       '并发专项大练兵：20 道 Java 并发高频题（线程池、锁机制、J.U.C、并发容器），40 分钟限时作答，检验你的并发功底。',
       198, 20, 40, CONCAT(CURDATE(), ' 09:00:00'), DATE_ADD(CONCAT(CURDATE(), ' 23:59:59'), INTERVAL 1 DAY), 5, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM competition WHERE title = '周末 Java 并发编程限时赛');

-- ② 进行中：Redis 缓存专项突击赛
INSERT INTO competition (title, description, category_id, question_count, duration_minutes, start_time, end_time, score_per_question, status)
SELECT 'Redis 缓存专项突击赛',
       '缓存专题速答：25 道 Redis 核心题（数据结构、持久化、缓存穿透/击穿/雪崩、分布式锁），30 分钟限时作答。',
       4, 25, 30, CONCAT(CURDATE(), ' 00:00:00'), DATE_ADD(CONCAT(CURDATE(), ' 23:59:59'), INTERVAL 1 DAY), 4, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM competition WHERE title = 'Redis 缓存专项突击赛');

-- ③ 报名中：算法基础十月联测（6 天后开赛）
INSERT INTO competition (title, description, category_id, question_count, duration_minutes, start_time, end_time, score_per_question, status)
SELECT '算法基础十月联测',
       '月度算法大联测：30 道算法基础题（排序、二分、双指针、滑动窗口、简单 DP），60 分钟限时作答，联测排名实时更新。',
       7, 30, 60, DATE_ADD(NOW(), INTERVAL 6 DAY), DATE_ADD(NOW(), INTERVAL 8 DAY), 3, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM competition WHERE title = '算法基础十月联测');

-- ④ 报名中：React 前端进阶挑战赛（5 天后开赛）
INSERT INTO competition (title, description, category_id, question_count, duration_minutes, start_time, end_time, score_per_question, status)
SELECT 'React 前端进阶挑战赛',
       'React 进阶专场：20 道 React 核心题（Hooks 原理、状态管理、性能优化、Fiber 架构），35 分钟限时作答。',
       71, 20, 35, DATE_ADD(NOW(), INTERVAL 5 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY), 5, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM competition WHERE title = 'React 前端进阶挑战赛');

-- ⑤ 报名中：MySQL 索引与调优争霸赛（9 天后开赛）
INSERT INTO competition (title, description, category_id, question_count, duration_minutes, start_time, end_time, score_per_question, status)
SELECT 'MySQL 索引与调优争霸赛',
       '数据库性能专题：25 道 MySQL 调优题（索引原理、执行计划、事务隔离、锁与死锁排查），40 分钟限时作答。',
       3, 25, 40, DATE_ADD(NOW(), INTERVAL 9 DAY), DATE_ADD(NOW(), INTERVAL 11 DAY), 4, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM competition WHERE title = 'MySQL 索引与调优争霸赛');

-- ⑥ 已结束：HTTP 协议与网络速答赛（4 天前结束）
INSERT INTO competition (title, description, category_id, question_count, duration_minutes, start_time, end_time, score_per_question, status)
SELECT 'HTTP 协议与网络速答赛',
       '网络基础速答：25 道 HTTP/HTTPS、TCP/IP、DNS 相关高频题，25 分钟限时作答，考验网络知识的条件反射。',
       8, 25, 25, DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY), 4, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM competition WHERE title = 'HTTP 协议与网络速答赛');

-- ⑦ 已结束：Git 团队协作实战赛（2 天前结束）
INSERT INTO competition (title, description, category_id, question_count, duration_minutes, start_time, end_time, score_per_question, status)
SELECT 'Git 团队协作实战赛',
       '版本控制实战：20 道 Git 核心题（分支策略、rebase/merge、冲突处理、协作工作流），30 分钟限时作答。',
       112, 20, 30, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), 5, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM competition WHERE title = 'Git 团队协作实战赛');

-- ============================================================
-- 竞赛混合题型种子（第二批）：7 个新分类 × 填空(type=3)×2 + 解答(type=4)×2。
-- 填空 answer 支持 | 多候选（标准化比对）；解答 answer 为要点关键词（≤50字，AI 判分参考+降级关键词匹配）。
-- 幂等：按 title 去重。
-- ============================================================
USE poti_question;

-- ===== Java 并发（198） =====
INSERT INTO question (category_id, title, content, type, difficulty, options, answer, analysis, tags, status)
SELECT 198, '【竞赛专项】线程池的核心参数中，用于限制线程池中同时活跃线程数量上限的参数是？', NULL, 3, 2, NULL,
       'maximumPoolSize', 'ThreadPoolExecutor 的 maximumPoolSize 表示线程池允许创建的最大线程数；核心线程数为 corePoolSize，二者共同决定并发能力。', 'Java并发', 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM question WHERE title = '【竞赛专项】线程池的核心参数中，用于限制线程池中同时活跃线程数量上限的参数是？');

INSERT INTO question (category_id, title, content, type, difficulty, options, answer, analysis, tags, status)
SELECT 198, '【竞赛专项】ReentrantReadWriteLock 中，获取读锁的方法名是？', NULL, 3, 2, NULL,
       'readLock', 'ReentrantReadWriteLock 通过 readLock() 获取读锁（共享）、writeLock() 获取写锁（独占），读写互斥、读读并行。', 'Java并发', 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM question WHERE title = '【竞赛专项】ReentrantReadWriteLock 中，获取读锁的方法名是？');

INSERT INTO question (category_id, title, content, type, difficulty, options, answer, analysis, tags, status)
SELECT 198, '【竞赛专项】为什么阿里巴巴 Java 开发手册禁止使用 Executors 创建线程池？请说明原因并给出推荐做法。', NULL, 4, 3, NULL,
       '无界队列堆积OOM 无界线程耗尽资源 手动newThreadPoolExecutor定参',
       'FixedThreadPool 与 SingleThreadExecutor 使用无界 LinkedBlockingQueue，任务堆积会导致 OOM；CachedThreadPool 最大线程数为 Integer.MAX_VALUE，会无限创建线程耗尽资源。推荐手动 new ThreadPoolExecutor 并明确核心线程数、队列容量、拒绝策略等参数。',
       'Java并发,线程池', 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM question WHERE title = '【竞赛专项】为什么阿里巴巴 Java 开发手册禁止使用 Executors 创建线程池？请说明原因并给出推荐做法。');

INSERT INTO question (category_id, title, content, type, difficulty, options, answer, analysis, tags, status)
SELECT 198, '【竞赛专项】volatile 关键字能保证线程安全吗？请说明它保证了什么、不能保证什么。', NULL, 4, 3, NULL,
       '保证可见性与有序性禁止指令重排 不保证原子性i++仍不安全 需AtomicInteger或加锁',
       'volatile 通过内存屏障保证变量的可见性（写立即刷回主存）与有序性（禁止指令重排），但不保证复合操作的原子性，i++ 这类操作仍会出现丢失更新，需要 AtomicInteger、synchronized 或 Lock 来保证。',
       'Java并发,volatile', 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM question WHERE title = '【竞赛专项】volatile 关键字能保证线程安全吗？请说明它保证了什么、不能保证什么。');

-- ===== Redis（4） =====
INSERT INTO question (category_id, title, content, type, difficulty, options, answer, analysis, tags, status)
SELECT 4, '【竞赛专项】Redis 单机默认拥有 16 个数据库，默认使用的是第几号数据库？', NULL, 3, 1, NULL,
       '0', 'Redis 默认配置 databases 16，客户端默认连接 db 0，可通过 SELECT n 切换；集群模式下仅支持 db 0。', 'Redis', 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM question WHERE title = '【竞赛专项】Redis 单机默认拥有 16 个数据库，默认使用的是第几号数据库？');

INSERT INTO question (category_id, title, content, type, difficulty, options, answer, analysis, tags, status)
SELECT 4, '【竞赛专项】为解决缓存穿透，可将不存在的 key 预先存入哪一种过滤器中快速拦截？', NULL, 3, 2, NULL,
       '布隆|bloom|Bloom', '布隆过滤器以极小内存代价判断「一定不存在或可能存在」，将全量合法 key 预载后，不存在的请求直接拦截，避免打到数据库。', 'Redis,缓存穿透', 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM question WHERE title = '【竞赛专项】为解决缓存穿透，可将不存在的 key 预先存入哪一种过滤器中快速拦截？');

INSERT INTO question (category_id, title, content, type, difficulty, options, answer, analysis, tags, status)
SELECT 4, '【竞赛专项】什么是缓存雪崩？请给出至少三种应对方案。', NULL, 4, 3, NULL,
       '大量key同时过期或Redis宕机 过期时间加随机值 多级缓存 熔断限流降级 集群高可用',
       '缓存雪崩指大量 key 同一时刻失效或 Redis 实例宕机，请求全部打到数据库导致崩溃。应对：①过期时间加随机值打散；②多级缓存（本地+分布式）；③热点数据永不过期+后台异步刷新；④熔断限流兜底；⑤哨兵/集群保证高可用。',
       'Redis,缓存雪崩', 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM question WHERE title = '【竞赛专项】什么是缓存雪崩？请给出至少三种应对方案。');

INSERT INTO question (category_id, title, content, type, difficulty, options, answer, analysis, tags, status)
SELECT 4, '【竞赛专项】使用 SETNX 实现分布式锁有什么问题？如何改进？', NULL, 4, 3, NULL,
       '锁误删与无法续期 释放前校验持有者 Lua原子释放 续期看门狗或Redisson',
       '裸 SETNX 的问题：①锁过期但业务未完成会被其他客户端抢锁；②释放时可能误删他人的锁。改进：SET key value NX EX 唯一标识加过期；释放用 Lua 脚本校验持有者后原子删除；业务超时需要续期（看门狗），或直接使用 Redisson 封装。',
       'Redis,分布式锁', 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM question WHERE title = '【竞赛专项】使用 SETNX 实现分布式锁有什么问题？如何改进？');

-- ===== 算法（7） =====
INSERT INTO question (category_id, title, content, type, difficulty, options, answer, analysis, tags, status)
SELECT 7, '【竞赛专项】快速排序的平均时间复杂度是？', NULL, 3, 1, NULL,
       'O(nlogn)|O(n log n)|nlogn', '快速排序平均 O(nlogn)，每次划分近似对半共 logn 层、每层 O(n)；最坏（已排序+固定基准）退化为 O(n²)。', '算法,排序', 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM question WHERE title = '【竞赛专项】快速排序的平均时间复杂度是？');

INSERT INTO question (category_id, title, content, type, difficulty, options, answer, analysis, tags, status)
SELECT 7, '【竞赛专项】二分查找要求数组必须满足哪一前提条件？', NULL, 3, 1, NULL,
       '有序|单调|已排序', '二分查找依赖有序性（单调性）每次将搜索区间折半；无序数组无法判断中点与目标的大小关系。', '算法,二分', 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM question WHERE title = '【竞赛专项】二分查找要求数组必须满足哪一前提条件？');

INSERT INTO question (category_id, title, content, type, difficulty, options, answer, analysis, tags, status)
SELECT 7, '【竞赛专项】给定一个整数数组和目标值，如何求出数组中和为目标值的两个数的下标？请说明思路与复杂度。', NULL, 4, 2, NULL,
       '哈希表存值到下标 遍历查target减当前值是否存在 O(n)时间O(n)空间',
       '两数之和经典解法：遍历数组，用哈希表记录「值→下标」；对每个元素 x 先查询 target-x 是否已在表中，存在即返回两下标，否则存入 x。一次遍历完成，时间 O(n)、空间 O(n)；暴力双循环为 O(n²)。',
       '算法,哈希表', 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM question WHERE title = '【竞赛专项】给定一个整数数组和目标值，如何求出数组中和为目标值的两个数的下标？请说明思路与复杂度。');

INSERT INTO question (category_id, title, content, type, difficulty, options, answer, analysis, tags, status)
SELECT 7, '【竞赛专项】如何反转一个单向链表？请描述迭代法的思路与复杂度。', NULL, 4, 2, NULL,
       '三指针prev cur next逐节点翻转 最终prev为新头 O(n)时间O(1)空间',
       '迭代反转：维护 prev（已反转部分头）与 cur（当前节点），每轮先用 next 暂存 cur.next，再把 cur.next 指向 prev，随后 prev=cur、cur=next；循环结束时 prev 即新头节点。时间 O(n)、空间 O(1)。递归法亦可但空间 O(n)。',
       '算法,链表', 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM question WHERE title = '【竞赛专项】如何反转一个单向链表？请描述迭代法的思路与复杂度。');

-- ===== React（71） =====
INSERT INTO question (category_id, title, content, type, difficulty, options, answer, analysis, tags, status)
SELECT 71, '【竞赛专项】useEffect 的第二个依赖参数传入什么值时，副作用仅在组件挂载后执行一次？', NULL, 3, 1, NULL,
       '空数组|[]', 'useEffect 第二参传 [] 时与组件生命周期挂钩：仅挂载后执行一次、卸载时执行清理函数；传依赖数组则在依赖变化时执行。', 'React,Hooks', 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM question WHERE title = '【竞赛专项】useEffect 的第二个依赖参数传入什么值时，副作用仅在组件挂载后执行一次？');

INSERT INTO question (category_id, title, content, type, difficulty, options, answer, analysis, tags, status)
SELECT 71, '【竞赛专项】状态钩子 useState 的返回值是一个数组，包含当前 state 和一个什么函数？', NULL, 3, 1, NULL,
       '更新|set|setter|修改', 'useState 返回 [state, setState]；setState 触发组件重渲染并更新 state，函数式更新 setState(prev => prev+1) 可避免闭包旧值问题。', 'React,Hooks', 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM question WHERE title = '【竞赛专项】状态钩子 useState 的返回值是一个数组，包含当前 state 和一个什么函数？');

INSERT INTO question (category_id, title, content, type, difficulty, options, answer, analysis, tags, status)
SELECT 71, '【竞赛专项】useMemo 和 useCallback 有什么区别？分别在什么场景使用？', NULL, 4, 3, NULL,
       'useMemo缓存计算值 useCallback缓存函数引用 依赖变化重建 用于子组件memo优化',
       'useMemo 缓存「计算结果值」，useCallback 缓存「函数引用」，二者都以依赖数组控制重建时机。场景：useMemo 用于昂贵计算或派生数据；useCallback 用于向被 memo 包裹的子组件传函数、避免其无意义重渲染，或作为 useEffect 依赖避免重复触发。',
       'React,性能优化', 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM question WHERE title = '【竞赛专项】useMemo 和 useCallback 有什么区别？分别在什么场景使用？');

INSERT INTO question (category_id, title, content, type, difficulty, options, answer, analysis, tags, status)
SELECT 71, '【竞赛专项】React 列表渲染中 key 的作用是什么？为什么不建议用数组索引作为 key？', NULL, 4, 3, NULL,
       '标识元素辅助diff复用 索引在插入删除时错位复用导致状态错乱 应使用稳定唯一id',
       'key 是虚拟 DOM diff 时元素的唯一标识，相同 key 可复用真实 DOM 与组件状态。用索引作 key 时，插入/删除会使后续元素 key 全部前移，React 错误复用节点，导致输入框内容错乱、动画异常等状态问题；应使用业务上的稳定唯一 id。',
       'React,虚拟DOM', 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM question WHERE title = '【竞赛专项】React 列表渲染中 key 的作用是什么？为什么不建议用数组索引作为 key？');

-- ===== MySQL（3） =====
INSERT INTO question (category_id, title, content, type, difficulty, options, answer, analysis, tags, status)
SELECT 3, '【竞赛专项】InnoDB 存储引擎默认的事务隔离级别是？', NULL, 3, 1, NULL,
       'REPEATABLE READ|可重复读|RR', 'InnoDB 默认 REPEATABLE READ（可重复读），通过 MVCC+间隙锁在该级别下基本避免了幻读；SQL 标准默认为 READ UNCOMMITTED 之外的序列化则少用。', 'MySQL,事务', 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM question WHERE title = '【竞赛专项】InnoDB 存储引擎默认的事务隔离级别是？');

INSERT INTO question (category_id, title, content, type, difficulty, options, answer, analysis, tags, status)
SELECT 3, '【竞赛专项】SQL 优化中，使用哪条命令可以查看语句的执行计划？', NULL, 3, 1, NULL,
       'EXPLAIN|explain|执行计划', 'EXPLAIN 输出执行计划：type（访问类型）、key（实际用到的索引）、rows（预估扫描行数）、Extra（Using filesort/index 等提示），是 SQL 优化的第一步。', 'MySQL,优化', 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM question WHERE title = '【竞赛专项】SQL 优化中，使用哪条命令可以查看语句的执行计划？');

INSERT INTO question (category_id, title, content, type, difficulty, options, answer, analysis, tags, status)
SELECT 3, '【竞赛专项】列举至少四种常见的索引失效场景。', NULL, 4, 3, NULL,
       '最左前缀缺失 列上函数运算 隐式类型转换 like前百分号 or含非索引列',
       '常见失效场景：①联合索引违反最左前缀；②索引列参与函数/计算（如 YEAR(create_time)）；③隐式类型转换（字符串列传数字）；④LIKE 以 % 开头；⑤OR 连接了非索引列；⑥不满足覆盖时的回传代价评估（优化器认为全表更快）。规避方式即按最左原则书写、避免列上计算、统一类型。',
       'MySQL,索引', 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM question WHERE title = '【竞赛专项】列举至少四种常见的索引失效场景。');

INSERT INTO question (category_id, title, content, type, difficulty, options, answer, analysis, tags, status)
SELECT 3, '【竞赛专项】一条 SQL 执行很慢，你会如何排查和优化？请描述完整思路。', NULL, 4, 3, NULL,
       'EXPLAIN定位扫描行数与索引 补索引或改写SQL 深分页优化 读写分离',
       '排查路径：①EXPLAIN 看 type/key/rows/Extra，确认是否走索引、是否全表扫描或 filesort；②核对 WHERE/ORDER BY 列是否建立合适索引；③改写 SQL（避免 SELECT *、拆分复杂 JOIN）；④深分页用延迟关联或游标；⑤数据量大时考虑分库分表、读写分离或引入缓存。',
       'MySQL,SQL优化', 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM question WHERE title = '【竞赛专项】一条 SQL 执行很慢，你会如何排查和优化？请描述完整思路。');

-- ===== 计算机网络（8） =====
INSERT INTO question (category_id, title, content, type, difficulty, options, answer, analysis, tags, status)
SELECT 8, '【竞赛专项】TCP 建立连接的三次握手中，第二个握手报文携带的标志位是？', NULL, 3, 2, NULL,
       'SYN+ACK|SYN,ACK|SYNACK', '三次握手：客户端发 SYN；服务端回 SYN+ACK（同时确认并同步序列号）；客户端再回 ACK。后两次确认双向通信能力。', '计算机网络,TCP', 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM question WHERE title = '【竞赛专项】TCP 建立连接的三次握手中，第二个握手报文携带的标志位是？');

INSERT INTO question (category_id, title, content, type, difficulty, options, answer, analysis, tags, status)
SELECT 8, '【竞赛专项】HTTPS 协议默认使用的端口号是？', NULL, 3, 1, NULL,
       '443', 'HTTP 默认 80，HTTPS 默认 443（HTTP over TLS/SSL），浏览器地址栏以 https:// 与证书锁标识区分。', '计算机网络,HTTPS', 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM question WHERE title = '【竞赛专项】HTTPS 协议默认使用的端口号是？');

INSERT INTO question (category_id, title, content, type, difficulty, options, answer, analysis, tags, status)
SELECT 8, '【竞赛专项】TCP 和 UDP 的主要区别有哪些？各自适合什么场景？', NULL, 4, 2, NULL,
       'TCP可靠连接字节流有序 UDP无连接不可靠报文 TCP网页文件传输 UDP直播DNS游戏',
       'TCP 面向连接（三次握手）、可靠传输（确认重传、有序、流量/拥塞控制），开销大延迟高；UDP 无连接、尽最大努力交付、报文边界保留、开销小。TCP 适合网页、文件传输、邮件等要完整的场景；UDP 适合直播、语音视频、DNS、游戏等实时性优先、允许丢包的场景。',
       '计算机网络,TCP,UDP', 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM question WHERE title = '【竞赛专项】TCP 和 UDP 的主要区别有哪些？各自适合什么场景？');

INSERT INTO question (category_id, title, content, type, difficulty, options, answer, analysis, tags, status)
SELECT 8, '【竞赛专项】简述从浏览器输入 URL 到页面渲染展示的完整过程。', NULL, 4, 3, NULL,
       'DNS解析 TCP握手 发HTTP请求 解析HTML建DOM渲染树 布局绘制展示',
       '完整链路：①DNS 解析域名得到 IP（缓存→递归→根/顶级/权威）；②TCP 三次握手（HTTPS 再加 TLS 握手）；③发送 HTTP 请求，服务端处理并返回响应；④浏览器解析 HTML 构建 DOM 树、CSS 构建 CSSOM，合成渲染树；⑤布局（计算几何位置）、分层绘制、合成显示。过程中还涉及重定向、缓存、懒加载等细节。',
       '计算机网络,浏览器,面试高频', 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM question WHERE title = '【竞赛专项】简述从浏览器输入 URL 到页面渲染展示的完整过程。');

-- ===== Git（112） =====
INSERT INTO question (category_id, title, content, type, difficulty, options, answer, analysis, tags, status)
SELECT 112, '【竞赛专项】git pull 命令等价于先执行 git fetch，再执行 git 什么命令？', NULL, 3, 1, NULL,
       'merge|合并', 'git pull = git fetch + git merge（默认）；配置 pull.rebase=true 后变为 fetch + rebase，可保持线性历史。', 'Git', 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM question WHERE title = '【竞赛专项】git pull 命令等价于先执行 git fetch，再执行 git 什么命令？');

INSERT INTO question (category_id, title, content, type, difficulty, options, answer, analysis, tags, status)
SELECT 112, '【竞赛专项】撤销工作区中某个文件的本地修改，git checkout 命令中文件名前需要加什么符号？', NULL, 3, 2, NULL,
       '--|双横线|两个横线', 'git checkout -- <file> 用暂存区/版本区内容覆盖工作区；-- 用于分隔分支名与路径，避免文件名与分支名同名时的歧义。', 'Git', 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM question WHERE title = '【竞赛专项】撤销工作区中某个文件的本地修改，git checkout 命令中文件名前需要加什么符号？');

INSERT INTO question (category_id, title, content, type, difficulty, options, answer, analysis, tags, status)
SELECT 112, '【竞赛专项】git merge 和 git rebase 有什么区别？各适合什么场景？', NULL, 4, 3, NULL,
       'merge保留历史合并提交 rebase线性重放提交 同步merge 分支整理rebase',
       'merge 将两条分支合并并产生合并提交，保留真实分支拓扑，适合公共分支同步；rebase 把当前分支的提交摘下在目标分支顶端「重放」，历史线性干净，适合个人 feature 分支合并前整理。注意：已推送到公共分支的提交禁止 rebase，会打乱他人历史。',
       'Git,分支管理', 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM question WHERE title = '【竞赛专项】git merge 和 git rebase 有什么区别？各适合什么场景？');

INSERT INTO question (category_id, title, content, type, difficulty, options, answer, analysis, tags, status)
SELECT 112, '【竞赛专项】代码合并出现冲突时，如何解决？请描述完整操作流程。', NULL, 4, 2, NULL,
       '打开冲突文件处理标记区 手动保留正确内容 git add后commit完成合并',
       '冲突解决流程：①git status/编辑器定位冲突文件；②文件中 <<<<<<< 到 >>>>>>> 之间是双方改动，手动决定保留内容并删除标记；③git add <file> 标记冲突已解决；④git commit（merge 场景）或 git rebase --continue（rebase 场景）完成。可借助 IDE 三方合并工具降低误删风险。',
       'Git,冲突处理', 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM question WHERE title = '【竞赛专项】代码合并出现冲突时，如何解决？请描述完整操作流程。');

