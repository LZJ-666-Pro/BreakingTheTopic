# 🔄 破题小程序 - 混合部署指南

## 📋 部署架构

### 混合部署模式

```
┌─────────────────────────────────────────┐
│         本地宿主机            │
│                                         │
│  ┌──────────────┐                      │
│  │   MySQL 8.0  │ ← 本地运行           │
│  │   Port: 3306 │                      │
│  └──────────────┘                      │
│           ↑                             │
│           │ 连接                        │
│           │                             │
│  ┌────────┴─────────────────────┐      │
│  │   Docker 容器网络             │      │
│  │                               │      │
│  │  ┌─────────┐  ┌─────────┐   │      │
│  │  │  Redis  │  │  Nacos  │   │      │
│  │  └─────────┘  └─────────┘   │      │
│  │                               │      │
│  │  ┌─────────┐  ┌─────────┐   │      │
│  │  │Gateway  │  │  User   │   │      │
│  │  └─────────┘  └─────────┘   │      │
│  │                               │      │
│  │  ... 其他微服务 ...           │      │
│  └───────────────────────────────┘      │
└─────────────────────────────────────────┘
```

### 服务分布

| 类型 | 服务 | 运行位置 | 端口 | 说明 |
|-----|------|---------|------|------|
| **本地服务** | MySQL | 宿主机 | 3306 | 本地MySQL数据库 |
| **Docker服务** | Redis | 容器 | 6379 | 缓存服务 |
| | Nacos | 容器 | 8848 | 注册中心 |
| | Elasticsearch | 容器 | 9201 | 搜索引擎 |
| | Gateway | 容器 | 8080 | API网关 |
| | Auth | 容器 | 8100 | 认证服务 |
| | User | 容器 | 8200 | 用户服务 |
| | Question | 容器 | 8300 | 题目服务 |
| | Practice | 容器 | 8400 | 练习服务 |
| | Wrongbook | 容器 | 8500 | 错题本服务 |
| | Favorite | 容器 | 8600 | 收藏服务 |
| | Interview | 容器 | 8700 | 面试服务 |
| | Search | 容器 | 8800 | 搜索服务 |
| | Admin | 容器 | 8088 | 管理后台 |

---

## ✅ 优势分析

### 1. 数据安全性
- ✅ MySQL数据在本地，更安全可控
- ✅ 便于数据备份和恢复
- ✅ 数据不会因容器删除而丢失

### 2. 性能优势
- ✅ MySQL使用本地磁盘，性能更好
- ✅ 减少Docker网络开销
- ✅ 数据库连接更稳定

### 3. 灵活性
- ✅ 可以使用现有的MySQL实例
- ✅ 便于数据库管理和监控
- ✅ 可以使用本地数据库工具

### 4. 资源优化
- ✅ 减少Docker内存占用
- ✅ MySQL可以使用更多系统资源
- ✅ 容器资源专注于应用服务

---

## 🚀 部署步骤

### 前置要求

#### 1. 本地MySQL配置

**确保MySQL已启动**：
```powershell
# Windows服务方式
net start MySQL80

# 验证MySQL运行
mysql -u root -p
# 输入密码: 123456
```

**配置MySQL允许外部连接**：
```sql
-- 登录MySQL
mysql -u root -p

-- 查看用户权限
SELECT user, host FROM mysql.user;

-- 如果root用户只能localhost连接，需要授权
CREATE USER 'root'@'%' IDENTIFIED BY '123456';
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;

-- 或者修改现有用户
UPDATE mysql.user SET host='%' WHERE user='root' AND host='localhost';
FLUSH PRIVILEGES;
```

**修改MySQL配置文件**（my.ini）：
```ini
[mysqld]
# 允许外部连接
bind-address = 0.0.0.0

# 或只允许本地和Docker网络
bind-address = 127.0.0.1,172.17.0.1
```

**重启MySQL**：
```powershell
net stop MySQL80
net start MySQL80
```

#### 2. 防火墙配置

**Windows防火墙开放MySQL端口**：
```powershell
# 以管理员身份运行
netsh advfirewall firewall add rule name="MySQL" dir=in action=allow protocol=tcp localport=3306
```

#### 3. Docker环境

**验证Docker安装**：
```powershell
docker --version
docker-compose --version
```

---

### 快速部署

#### 方法1：使用部署脚本（推荐）

```powershell
# 进入项目目录
cd d:\study\BreakingTheTopic\poti

# 运行混合部署脚本
.\deploy-hybrid.bat
```

**脚本会自动**：
1. ✅ 检查MySQL连接
2. ✅ 清理旧的构建文件
3. ✅ 编译打包所有服务
4. ✅ 构建Docker镜像
5. ✅ 启动所有服务

#### 方法2：手动部署

**步骤1：验证MySQL**
```powershell
mysql -u root -p123456 -e "SELECT 1"
```

**步骤2：编译打包**
```powershell
mvn clean package -DskipTests
```

**步骤3：构建镜像**
```powershell
docker-compose -f docker-compose-hybrid.yml build
```

**步骤4：启动服务**
```powershell
docker-compose -f docker-compose-hybrid.yml up -d
```

---

## 🔧 配置说明

### Docker访问宿主机MySQL

#### Windows/Mac系统

Docker容器使用 `host.docker.internal` 访问宿主机：

```yaml
environment:
  - DB_HOST=host.docker.internal
  - DB_PORT=3306
```

**原理**：
- `host.docker.internal` 是Docker提供的特殊DNS名称
- 自动解析为宿主机的IP地址
- 无需手动配置IP

#### Linux系统

**方法1：使用host.docker.internal**
```yaml
extra_hosts:
  - "host.docker.internal:host-gateway"
```

**方法2：使用宿主机IP**
```yaml
environment:
  - DB_HOST=172.17.0.1  # Docker网桥网关
  - DB_PORT=3306
```

**方法3：使用实际IP**
```yaml
environment:
  - DB_HOST=192.168.1.100  # 宿主机实际IP
  - DB_PORT=3306
```

---

## 📊 服务管理

### 查看服务状态

```powershell
# 查看所有容器
docker ps

# 查看服务状态
docker-compose -f docker-compose-hybrid.yml ps

# 查看日志
docker-compose -f docker-compose-hybrid.yml logs -f
```

### 停止服务

```powershell
# 停止所有Docker服务
docker-compose -f docker-compose-hybrid.yml stop

# 停止并删除
docker-compose -f docker-compose-hybrid.yml down

# MySQL不受影响，继续运行
```

### 重启服务

```powershell
# 重启所有Docker服务
docker-compose -f docker-compose-hybrid.yml restart

# 重启特定服务
docker-compose -f docker-compose-hybrid.yml restart poti-user
```

---

## 🔍 验证部署

### 1. 验证MySQL连接

**从容器内测试**：
```powershell
# 进入应用容器
docker exec -it poti-user sh

# 测试MySQL连接（需要安装mysql-client）
# 或查看应用日志
tail -f /app/logs/application.log
```

**从本地测试**：
```powershell
# 查看数据库连接数
mysql -u root -p123456 -e "SHOW PROCESSLIST"

# 应该能看到多个来自Docker网络的连接
```

### 2. 验证服务健康

```powershell
# 测试用户服务
curl http://localhost:8200/actuator/health

# 测试题目服务
curl http://localhost:8300/actuator/health
```

### 3. 验证数据操作

**测试数据库操作**：
```powershell
# 通过API创建数据
curl -X POST http://localhost:8200/user/info -d '{"nickname":"test"}'

# 查看MySQL数据
mysql -u root -p123456 -e "SELECT * FROM poti_user.user"
```

---

## 🐛 常见问题

### 问题1：容器无法连接MySQL

**错误信息**：
```
Communications link failure
```

**排查步骤**：
1. 检查MySQL是否运行
   ```powershell
   net start | findstr "MySQL"
   ```

2. 检查MySQL是否允许外部连接
   ```sql
   SELECT user, host FROM mysql.user;
   ```

3. 检查防火墙
   ```powershell
   netsh advfirewall firewall show rule name="MySQL"
   ```

4. 测试连接
   ```powershell
   # 从容器内测试
   docker exec -it poti-user sh
   ping host.docker.internal
   ```

**解决方法**：
```sql
-- 授权root用户外部连接
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY '123456' WITH GRANT OPTION;
FLUSH PRIVILEGES;
```

### 问题2：host.docker.internal无法解析

**Linux系统问题**：

**解决方法**：
已在docker-compose-hybrid.yml中添加：
```yaml
extra_hosts:
  - "host.docker.internal:host-gateway"
```

### 问题3：MySQL连接数过多

**错误信息**：
```
Too many connections
```

**解决方法**：
```sql
-- 查看当前连接数
SHOW STATUS LIKE 'Threads_connected';

-- 修改最大连接数
SET GLOBAL max_connections = 500;

-- 或修改my.ini
[mysqld]
max_connections = 500
```

### 问题4：数据库性能下降

**排查方法**：
```sql
-- 查看慢查询
SHOW VARIABLES LIKE 'slow_query%';

-- 查看连接数
SHOW STATUS LIKE 'Threads_connected';

-- 查看锁等待
SHOW ENGINE INNODB STATUS;
```

**优化建议**：
- 调整连接池配置
- 优化SQL查询
- 添加数据库索引

---

## 📈 性能优化

### 1. MySQL配置优化

**my.ini配置**：
```ini
[mysqld]
# 连接配置
max_connections = 500
max_connect_errors = 1000

# 缓冲配置
innodb_buffer_pool_size = 1G
innodb_log_buffer_size = 64M

# 查询缓存
query_cache_size = 128M
query_cache_type = 1

# 慢查询日志
slow_query_log = 1
long_query_time = 2
```

### 2. 连接池优化

**应用配置**：
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

### 3. 网络优化

**使用host网络模式**（可选）：
```yaml
services:
  poti-user:
    network_mode: "host"
```

---

## 🔄 数据备份

### MySQL备份

**手动备份**：
```powershell
# 备份所有数据库
mysqldump -u root -p123456 --all-databases > backup.sql

# 备份特定数据库
mysqldump -u root -p123456 poti_user > poti_user_backup.sql
```

**自动备份脚本**：
```powershell
# backup-mysql.ps1
$Date = Get-Date -Format "yyyyMMdd_HHmmss"
$BackupPath = "D:\backup\mysql"

# 创建备份目录
New-Item -ItemType Directory -Force -Path $BackupPath

# 备份数据库
mysqldump -u root -p123456 --all-databases > "$BackupPath\backup_$Date.sql"

# 删除7天前的备份
Get-ChildItem $BackupPath -Filter "*.sql" | 
    Where-Object {$_.LastWriteTime -lt (Get-Date).AddDays(-7)} | 
    Remove-Item
```

---

## 📊 监控方案

### 1. MySQL监控

**使用MySQL Workbench**：
- 连接本地MySQL
- 查看性能仪表盘
- 监控连接数、查询数

**命令行监控**：
```sql
-- 查看状态
SHOW STATUS;

-- 查看进程
SHOW PROCESSLIST;

-- 查看InnoDB状态
SHOW ENGINE INNODB STATUS;
```

### 2. Docker监控

```powershell
# 查看容器资源使用
docker stats

# 查看容器详情
docker inspect poti-user
```

---

## 🎯 最佳实践

### 1. 开发环境
- ✅ 使用混合部署
- ✅ 本地MySQL便于调试
- ✅ Docker服务快速启动

### 2. 测试环境
- ✅ 可以使用混合部署
- ✅ 或完全Docker部署

### 3. 生产环境
- ⚠️ 建议MySQL独立部署
- ✅ 应用服务使用Docker
- ✅ 使用云数据库服务

---

## 📝 对比分析

### 混合部署 vs 完全Docker

| 对比项 | 混合部署 | 完全Docker |
|-------|---------|-----------|
| **数据安全** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |
| **性能** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **部署速度** | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **管理便利** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **迁移性** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **资源占用** | ⭐⭐⭐⭐ | ⭐⭐⭐ |

### 推荐场景

**混合部署适合**：
- ✅ 开发环境
- ✅ 数据安全要求高
- ✅ 需要频繁数据库操作
- ✅ 本地有MySQL实例

**完全Docker适合**：
- ✅ CI/CD环境
- ✅ 快速部署需求
- ✅ 环境一致性要求高
- ✅ 云部署场景

---

## 🎉 总结

### 混合部署优势

1. **数据安全**：MySQL数据在本地，更安全
2. **性能更好**：本地磁盘性能优于Docker卷
3. **管理方便**：可以使用熟悉的数据库工具
4. **资源优化**：减少Docker内存占用
5. **灵活性强**：可以复用现有MySQL实例

### 注意事项

1. ⚠️ 确保MySQL允许外部连接
2. ⚠️ 配置防火墙开放端口
3. ⚠️ 注意数据库连接数配置
4. ⚠️ 定期备份数据库

---

## 📞 技术支持

**相关文档**：
- Docker部署指南: `DOCKER_DEPLOYMENT_GUIDE.md`
- 部署检查清单: `DEPLOYMENT_CHECKLIST.md`
- 优化建议: `OPTIMIZATION_GUIDE.md`

**常用命令**：
```powershell
# 部署
.\deploy-hybrid.bat

# 查看状态
docker-compose -f docker-compose-hybrid.yml ps

# 查看日志
docker-compose -f docker-compose-hybrid.yml logs -f

# 停止服务
docker-compose -f docker-compose-hybrid.yml down
```

---

**生成时间**: 2026-05-14
**状态**: ✅ 混合部署配置完成
