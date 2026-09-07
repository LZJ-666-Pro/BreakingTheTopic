-- 清空现有题目数据（仅在第一次执行时需要）
-- DELETE FROM `question`;

-- 插入Java题目（ID 1-10），使用INSERT IGNORE避免重复插入
INSERT IGNORE INTO `question` (`id`, `category_id`, `title`, `content`, `type`, `difficulty`, `options`, `answer`, `analysis`, `tags`, `status`) VALUES
(1, 1, 'Java中哪个关键字用于定义类？', 'Java中哪个关键字用于定义类？', 1, 1, '["class","struct","define","object"]', 'A', 'class是Java中定义类的关键字。', 'Java,基础', 1),
(2, 1, 'Java中main方法的正确签名是？', 'Java中main方法的正确签名是？', 1, 1, '["public void main(String[] args)","public static void main(String[] args)","static void main(String[] args)","public static int main(String[] args)"]', 'B', 'main方法必须是public static void，参数为String数组。', 'Java,基础', 1),
(3, 1, 'Java中String是不可变的，这意味着？', 'Java中String是不可变的，这意味着？', 1, 1, '["String对象不能被创建","String对象的值不能被修改","String对象不能被引用","String对象不能被比较"]', 'B', 'String是不可变对象，一旦创建其值就不能被修改。', 'Java,基础', 1),
(4, 1, 'Java中哪个集合不允许重复元素？', 'Java中哪个集合不允许重复元素？', 1, 1, '["ArrayList","LinkedList","HashSet","HashMap"]', 'C', 'HashSet是基于哈希表实现的Set集合，不允许存储重复元素。', 'Java,集合', 1),
(5, 1, 'Java中final关键字可以修饰？', 'Java中final关键字可以修饰？', 1, 2, '["只有变量","只有方法","只有类","变量、方法和类"]', 'D', 'final可以修饰变量（常量）、方法（不可重写）和类（不可继承）。', 'Java,基础', 1),
(6, 1, 'Java中接口和抽象类的区别是？', 'Java中接口和抽象类的区别是？', 1, 2, '["接口可以有构造方法","抽象类可以多继承","一个类可以实现多个接口","接口可以有成员变量"]', 'C', 'Java支持单继承多实现，一个类可以实现多个接口。', 'Java,面向对象', 1),
(7, 1, 'Java中哪个关键字用于抛出异常？', 'Java中哪个关键字用于抛出异常？', 1, 2, '["catch","throw","throws","try"]', 'B', 'throw用于手动抛出异常对象，throws用于声明方法可能抛出的异常。', 'Java,异常', 1),
(8, 1, 'Java中synchronized关键字可以修饰？', 'Java中synchronized关键字可以修饰？', 1, 2, '["只有方法","只有代码块","方法和代码块","只有变量"]', 'C', 'synchronized可以修饰方法和代码块，用于实现线程同步。', 'Java,多线程', 1),
(9, 1, 'Java中volatile关键字的作用是？', 'Java中volatile关键字的作用是？', 1, 3, '["保证原子性","保证可见性","保证有序性","以上都是"]', 'B', 'volatile保证变量的可见性，但不保证原子性。', 'Java,多线程', 1),
(10, 1, 'Java中HashMap的默认负载因子是？', 'Java中HashMap的默认负载因子是？', 1, 3, '["0.5","0.75","1.0","0.25"]', 'B', 'HashMap的默认负载因子是0.75，这是平衡时间和空间成本的结果。', 'Java,集合', 1),

-- 插入Python题目（ID 11-20）
(11, 2, 'Python中用于定义函数的关键字是？', 'Python中用于定义函数的关键字是？', 1, 1, '["function","def","func","define"]', 'B', 'Python使用def关键字定义函数。', 'Python,基础', 1),
(12, 2, 'Python中列表和元组的主要区别是？', 'Python中列表和元组的主要区别是？', 1, 1, '["列表可变，元组不可变","列表不可变，元组可变","都是可变的","都是不可变的"]', 'A', '列表是可变序列，元组是不可变序列。', 'Python,基础', 1),
(13, 2, 'Python中哪个不是基本数据类型？', 'Python中哪个不是基本数据类型？', 1, 1, '["int","str","list","char"]', 'D', 'Python没有char类型，单个字符也是str类型。', 'Python,基础', 1),
(14, 2, 'Python中如何创建空字典？', 'Python中如何创建空字典？', 1, 1, '["{}","[]","()","set()"]', 'A', '{}创建空字典，[]创建空列表，()创建空元组。', 'Python,基础', 1),
(15, 2, 'Python中列表推导式的语法是？', 'Python中列表推导式的语法是？', 1, 2, '["[x for x in iterable]","{x for x in iterable}","(x for x in iterable)","<x for x in iterable>"]', 'A', '列表推导式使用方括号[]，返回列表。', 'Python,高级', 1),
(16, 2, 'Python中*args和**kwargs的作用是？', 'Python中*args和**kwargs的作用是？', 1, 2, '["定义变量","接收可变参数","定义常量","定义类"]', 'B', '*args接收可变位置参数，**kwargs接收可变关键字参数。', 'Python,函数', 1),
(17, 2, 'Python中装饰器的作用是？', 'Python中装饰器的作用是？', 1, 2, '["装饰字符串","修改函数行为","创建对象","定义变量"]', 'B', '装饰器用于在不修改函数代码的情况下扩展函数功能。', 'Python,高级', 1),
(18, 2, 'Python中GIL是什么？', 'Python中GIL是什么？', 1, 3, '["全局解释器锁","全局导入锁","全局迭代锁","全局索引锁"]', 'A', 'GIL是全局解释器锁，确保同一时刻只有一个线程执行Python字节码。', 'Python,多线程', 1),
(19, 2, 'Python中生成器和列表的区别是？', 'Python中生成器和列表的区别是？', 1, 3, '["生成器占用更多内存","生成器是惰性求值","列表是惰性求值","没有区别"]', 'B', '生成器是惰性求值的，只在需要时生成值，节省内存。', 'Python,高级', 1),
(20, 2, 'Python中with语句的作用是？', 'Python中with语句的作用是？', 1, 2, '["定义上下文","资源管理","异常处理","循环控制"]', 'B', 'with语句用于资源管理，确保资源正确释放。', 'Python,基础', 1),

-- 插入MySQL题目（ID 21-30）
(21, 3, 'MySQL中主键的特点是？', 'MySQL中主键的特点是？', 1, 1, '["可以为空","可以重复","唯一且非空","只能有一个字段"]', 'C', '主键必须唯一且非空，但可以由多个字段组成复合主键。', 'MySQL,基础', 1),
(22, 3, 'MySQL中VARCHAR和CHAR的区别是？', 'MySQL中VARCHAR和CHAR的区别是？', 1, 1, '["VARCHAR固定长度","CHAR可变长度","VARCHAR可变长度","没有区别"]', 'C', 'VARCHAR是可变长度字符串，CHAR是固定长度字符串。', 'MySQL,基础', 1),
(23, 3, 'MySQL中哪个存储引擎支持事务？', 'MySQL中哪个存储引擎支持事务？', 1, 1, '["MyISAM","InnoDB","MEMORY","ARCHIVE"]', 'B', 'InnoDB支持事务、行级锁和外键，MyISAM不支持事务。', 'MySQL,存储引擎', 1),
(24, 3, 'MySQL中索引的作用是？', 'MySQL中索引的作用是？', 1, 1, '["增加存储空间","提高查询速度","降低查询速度","没有作用"]', 'B', '索引可以大大提高查询速度，但会增加存储空间和降低写入速度。', 'MySQL,索引', 1),
(25, 3, 'MySQL中事务的ACID特性是指？', 'MySQL中事务的ACID特性是指？', 1, 2, '["原子性、一致性、隔离性、持久性","准确性、一致性、隔离性、持久性","原子性、连续性、隔离性、持久性","原子性、一致性、集成性、持久性"]', 'A', 'ACID指原子性、一致性、隔离性、持久性。', 'MySQL,事务', 1),
(26, 3, 'MySQL中LEFT JOIN返回？', 'MySQL中LEFT JOIN返回？', 1, 2, '["左表所有记录","右表所有记录","两表匹配记录","两表所有记录"]', 'A', 'LEFT JOIN返回左表所有记录，右表没有匹配则为NULL。', 'MySQL,连接', 1),
(27, 3, 'MySQL中EXPLAIN的作用是？', 'MySQL中EXPLAIN的作用是？', 1, 2, '["解释SQL语法","分析查询执行计划","解释表结构","解释索引"]', 'B', 'EXPLAIN用于分析SQL查询的执行计划。', 'MySQL,优化', 1),
(28, 3, 'MySQL中如何避免SQL注入？', 'MySQL中如何避免SQL注入？', 1, 2, '["使用Statement","使用PreparedStatement","使用字符串拼接","使用文件读取"]', 'B', 'PreparedStatement使用参数化查询，可以有效防止SQL注入。', 'MySQL,安全', 1),
(29, 3, 'MySQL中MVCC的作用是？', 'MySQL中MVCC的作用是？', 1, 3, '["提高写入性能","提高并发性能","提高查询速度","减少存储空间"]', 'B', 'MVCC多版本并发控制，提高数据库并发性能。', 'MySQL,高级', 1),
(30, 3, 'MySQL中B+树索引的特点是？', 'MySQL中B+树索引的特点是？', 1, 3, '["非叶子节点存储数据","叶子节点不存储数据","叶子节点存储数据且有序","所有节点存储数据"]', 'C', 'B+树非叶子节点只存储键值，叶子节点存储数据且通过指针连接。', 'MySQL,索引', 1),

-- 插入Redis题目（ID 31-40）
(31, 4, 'Redis默认端口号是？', 'Redis默认端口号是？', 1, 1, '["3306","6379","8080","27017"]', 'B', 'Redis默认端口是6379。', 'Redis,基础', 1),
(32, 4, 'Redis支持的数据类型不包括？', 'Redis支持的数据类型不包括？', 1, 1, '["String","List","Table","Set"]', 'C', 'Redis不支持Table类型，支持String、List、Set、Hash、ZSet等。', 'Redis,基础', 1),
(33, 4, 'Redis中SET命令的作用是？', 'Redis中SET命令的作用是？', 1, 1, '["设置过期时间","存储字符串","删除键","获取值"]', 'B', 'SET命令用于存储字符串类型的值。', 'Redis,基础', 1),
(34, 4, 'Redis持久化的方式有？', 'Redis持久化的方式有？', 1, 1, '["只有RDB","只有AOF","RDB和AOF","没有持久化"]', 'C', 'Redis支持RDB快照和AOF日志两种持久化方式。', 'Redis,持久化', 1),
(35, 4, 'Redis中缓存穿透是指？', 'Redis中缓存穿透是指？', 1, 2, '["缓存和数据库都没有数据","缓存没有数据库有","缓存有数据库没有","缓存过期"]', 'A', '缓存穿透是指查询缓存和数据库都不存在的数据。', 'Redis,缓存', 1),
(36, 4, 'Redis中缓存击穿是指？', 'Redis中缓存击穿是指？', 1, 2, '["大量key同时过期","热点key过期","缓存穿透","缓存雪崩"]', 'B', '缓存击穿是指热点key过期，大量请求直接访问数据库。', 'Redis,缓存', 1),
(37, 4, 'Redis中缓存雪崩是指？', 'Redis中缓存雪崩是指？', 1, 2, '["单个key过期","大量key同时过期","缓存穿透","缓存击穿"]', 'B', '缓存雪崩是指大量key同时过期，导致请求直接访问数据库。', 'Redis,缓存', 1),
(38, 4, 'Redis集群的实现方式是？', 'Redis集群的实现方式是？', 1, 2, '["主从复制","哨兵模式","Cluster集群","以上都是"]', 'D', 'Redis可以通过主从复制、哨兵模式和Cluster集群实现高可用。', 'Redis,集群', 1),
(39, 4, 'Redis中分布式锁的实现方式是？', 'Redis中分布式锁的实现方式是？', 1, 3, '["SETNX","SET","GET","DEL"]', 'A', 'SETNX可以用于实现分布式锁，但建议使用SET命令带NX参数。', 'Redis,分布式', 1),
(40, 4, 'Redis中跳表用于哪种数据类型？', 'Redis中跳表用于哪种数据类型？', 1, 3, '["String","List","Set","ZSet"]', 'D', 'Redis的ZSet（有序集合）使用跳表作为底层实现。', 'Redis,高级', 1),

-- 插入Spring题目（ID 41-50）
(41, 5, 'Spring框架的核心特性是？', 'Spring框架的核心特性是？', 1, 1, '["AOP","IOC","MVC","以上都是"]', 'D', 'Spring框架的核心特性包括IOC控制反转和AOP面向切面编程。', 'Spring,基础', 1),
(42, 5, 'Spring中@Bean注解的作用是？', 'Spring中@Bean注解的作用是？', 1, 1, '["定义配置类","定义Bean","注入依赖","定义切面"]', 'B', '@Bean注解用于方法上，声明该方法返回一个由Spring管理的Bean。', 'Spring,基础', 1),
(43, 5, 'Spring中@Autowired的作用是？', 'Spring中@Autowired的作用是？', 1, 1, '["定义Bean","自动装配","定义配置","定义切面"]', 'B', '@Autowired用于自动装配Bean的依赖。', 'Spring,基础', 1),
(44, 5, 'Spring中@ComponentScan的作用是？', 'Spring中@ComponentScan的作用是？', 1, 1, '["定义Bean","扫描组件","注入依赖","定义配置"]', 'B', '@ComponentScan用于指定Spring扫描组件的包路径。', 'Spring,基础', 1),
(45, 5, 'Spring AOP中通知类型不包括？', 'Spring AOP中通知类型不包括？', 1, 2, '["Before","After","Around","Inside"]', 'D', 'Spring AOP支持Before、After、Around、AfterReturning、AfterThrowing通知。', 'Spring,AOP', 1),
(46, 5, 'Spring事务的传播行为默认是？', 'Spring事务的传播行为默认是？', 1, 2, '["REQUIRES_NEW","REQUIRED","SUPPORTS","MANDATORY"]', 'B', 'Spring事务默认传播行为是REQUIRED，如果存在事务则加入，否则新建。', 'Spring,事务', 1),
(47, 5, 'Spring中@PathVariable的作用是？', 'Spring中@PathVariable的作用是？', 1, 2, '["获取请求参数","获取路径变量","获取请求头","获取Cookie"]', 'B', '@PathVariable用于获取URL路径中的变量值。', 'Spring,MVC', 1),
(48, 5, 'Spring Boot自动配置的原理是？', 'Spring Boot自动配置的原理是？', 1, 2, '["@EnableAutoConfiguration","@SpringBootApplication","@ComponentScan","@Configuration"]', 'A', '@EnableAutoConfiguration根据类路径下的依赖自动配置Spring应用。', 'Spring Boot,基础', 1),
(49, 5, 'Spring Cloud中服务注册发现的组件是？', 'Spring Cloud中服务注册发现的组件是？', 1, 3, '["Gateway","Nacos","Ribbon","Hystrix"]', 'B', 'Nacos可以作为服务注册中心和配置中心。', 'Spring Cloud,微服务', 1),
(50, 5, 'Spring中循环依赖的解决方式是？', 'Spring中循环依赖的解决方式是？', 1, 3, '["三级缓存","二级缓存","一级缓存","无法解决"]', 'A', 'Spring使用三级缓存解决单例Bean的循环依赖问题。', 'Spring,高级', 1),

-- 插入MQ题目（ID 51-60）
(51, 6, '消息队列的主要作用是？', '消息队列的主要作用是？', 1, 1, '["数据存储","异步解耦","数据计算","数据展示"]', 'B', '消息队列主要用于异步处理、应用解耦和流量削峰。', 'MQ,基础', 1),
(52, 6, 'RabbitMQ默认端口号是？', 'RabbitMQ默认端口号是？', 1, 1, '["5672","6379","3306","8080"]', 'A', 'RabbitMQ默认端口是5672，管理界面端口是15672。', 'RabbitMQ,基础', 1),
(53, 6, 'Kafka中消息存储的位置是？', 'Kafka中消息存储的位置是？', 1, 1, '["内存","磁盘","数据库","缓存"]', 'B', 'Kafka将消息持久化存储在磁盘上，保证消息不丢失。', 'Kafka,基础', 1),
(54, 6, '消息队列如何保证消息不丢失？', '消息队列如何保证消息不丢失？', 1, 1, '["持久化","确认机制","事务","以上都是"]', 'D', '消息队列通过持久化、确认机制和事务保证消息不丢失。', 'MQ,可靠性', 1),
(55, 6, 'RabbitMQ中Exchange的类型不包括？', 'RabbitMQ中Exchange的类型不包括？', 1, 2, '["Direct","Topic","Fanout","Queue"]', 'D', 'Exchange类型包括Direct、Topic、Fanout、Headers，Queue不是Exchange类型。', 'RabbitMQ,高级', 1),
(56, 6, 'Kafka中Consumer Group的作用是？', 'Kafka中Consumer Group的作用是？', 1, 2, '["提高写入性能","提高消费性能","提高存储性能","提高网络性能"]', 'B', 'Consumer Group允许多个消费者并行消费，提高消费性能。', 'Kafka,高级', 1),
(57, 6, '消息队列如何保证消息顺序？', '消息队列如何保证消息顺序？', 1, 2, '["多队列","单队列单消费者","分区","以上都是"]', 'B', '单队列单消费者可以保证消息顺序，但会降低并发性能。', 'MQ,高级', 1),
(58, 6, 'RocketMQ的事务消息原理是？', 'RocketMQ的事务消息原理是？', 1, 3, '["两阶段提交","三阶段提交","TCC","本地消息表"]', 'A', 'RocketMQ事务消息采用两阶段提交实现分布式事务。', 'RocketMQ,高级', 1),
(59, 6, 'Kafka中ISR是指？', 'Kafka中ISR是指？', 1, 3, '["同步副本集合","异步副本集合","领导者副本","追随者副本"]', 'A', 'ISR是In-Sync Replicas，指与Leader保持同步的副本集合。', 'Kafka,高级', 1),
(60, 6, '消息队列如何实现延迟消息？', '消息队列如何实现延迟消息？', 1, 3, '["定时任务","延迟队列","延迟发送","以上都是"]', 'B', '消息队列通过延迟队列或定时任务实现延迟消息。', 'MQ,高级', 1);

-- 更新practice_record表中的question_id，将旧ID映射到新ID（仅需要执行一次）
-- Java题目：3001-3010 -> 1-10
-- UPDATE `practice_record` SET `question_id` = `question_id` - 3000 WHERE `question_id` BETWEEN 3001 AND 3010;
-- Redis题目：4001-4010 -> 31-40
-- UPDATE `practice_record` SET `question_id` = `question_id` - 3970 WHERE `question_id` BETWEEN 4001 AND 4010;
