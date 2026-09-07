# Docker 配置说明

## 📋 Docker 服务配置

### 服务列表

| 服务 | 容器名称 | 端口映射 | 用途 |
|------|---------|---------|------|
| MySQL | poti-mysql | 3306:3306 | 数据库 |
| Redis | poti-redis | 6379:6379 | 缓存 |
| Nacos | poti-nacos | 8848:8848 | 服务注册与配置中心 |
| Elasticsearch | poti-es | 9200:9200, 9300:9300 | 搜索引擎 |
| Kibana | poti-kibana | 5601:5601 | ES 可视化 |

## 🔧 微服务配置说明

### 网络架构说明

本项目采用 **微服务在容器外运行，Docker 容器提供基础服务** 的架构：

```
┌─────────────────────────────────────────┐
│         宿主机（Host Machine）          │
│                                         │
│  ┌──────────────┐  ┌──────────────┐   │
│  │ poti-gateway │  │ poti-user    │   │
│  │   :8080      │  │   :8200      │   │
│  └──────────────┘  └──────────────┘   │
│         ...              ...            │
│                                         │
│  ┌──────────────────────────────────┐  │
│  │     Docker 容器（Bridge 网络）    │  │
│  │                                  │  │
│  │  ┌──────────┐  ┌──────────┐    │  │
│  │  │  MySQL   │  │  Redis   │    │  │
│  │  │  :3306   │  │  :6379   │    │  │
│  │  └──────────┘  └──────────┘    │  │
│  │                                  │  │
│  │  ┌──────────┐  ┌──────────┐    │  │
│  │  │  Nacos   │  │    ES    │    │  │
│  │  │  :8848   │  │  :9200   │    │  │
│  │  └──────────┘  └──────────┘    │  │
│  └──────────────────────────────────┘  │
│         ↓ 端口映射 ↓                   │
└─────────────────────────────────────────┘
```

### 配置地址说明

#### 1. Nacos 配置

**所有微服务的 bootstrap.yml**：
```yaml
spring:
  cloud:
    nacos:
      server-addr: 127.0.0.1:8848  # ✅ 正确
```

**说明**：
- 微服务运行在宿主机上
- Docker 容器中的 Nacos 通过端口映射暴露到宿主机 8848 端口
- 使用 `127.0.0.1:8848` 或 `localhost:8848` 访问

#### 2. Redis 配置

**poti-practice 的 application-dev.yml**：
```yaml
spring:
  redis:
    host: localhost  # ✅ 正确
    port: 6379
```

**说明**：
- Docker 容器中的 Redis 通过端口映射暴露到宿主机 6379 端口
- 使用 `localhost:6379` 访问

#### 3. Elasticsearch 配置

**poti-search 的 application-dev.yml**：
```yaml
spring:
  elasticsearch:
    uris: http://localhost:9200  # ✅ 正确
```

**说明**：
- Docker 容器中的 ES 通过端口映射暴露到宿主机 9200 端口
- 使用 `localhost:9200` 访问

#### 4. MySQL 配置

**所有微服务的 application-dev.yml**：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/数据库名?...
    username: root
    password: 123456
```

**说明**：
- Docker 容器中的 MySQL 通过端口映射暴露到宿主机 3306 端口
- 使用 `localhost:3306` 访问
- 需要手动创建各个业务数据库

## 🚀 启动步骤

### 1. 启动 Docker 服务

```bash
cd d:\study\BreakingTheTopic\poti
docker-compose up -d
```

### 2. 检查服务状态

```bash
docker-compose ps
```

### 3. 创建数据库

连接到 MySQL 并创建数据库：

```sql
CREATE DATABASE poti_user CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE poti_question CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE poti_practice CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE poti_wrongbook CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE poti_favorite CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE poti_interview CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 4. 启动微服务

按以下顺序启动微服务：

1. 启动 Nacos（Docker 自动启动）
2. 启动 poti-gateway
3. 启动 poti-auth
4. 启动 poti-user
5. 启动其他业务服务

## 📊 服务端口规划

| 服务名 | 端口 | 说明 |
|--------|------|------|
| poti-gateway | 8080 | 网关服务 |
| poti-auth | 8100 | 认证授权 |
| poti-user | 8200 | 用户管理 |
| poti-question | 8300 | 题库管理 |
| poti-practice | 8400 | 刷题核心 |
| poti-wrongbook | 8500 | 错题本 |
| poti-favorite | 8600 | 收藏 |
| poti-interview | 8700 | 模拟面试 |
| poti-search | 8800 | 搜索 |
| poti-monitor | 8888 | 监控 |

## 🔍 访问地址

### Web 界面

- **Nacos 控制台**：http://localhost:8848/nacos
  - 用户名：nacos
  - 密码：nacos

- **Kibana**：http://localhost:5601

### API 端点

- **网关入口**：http://localhost:8080
- **用户服务**：http://localhost:8080/user/**
- **认证服务**：http://localhost:8080/auth/**
- **题库服务**：http://localhost:8080/question/**
- **刷题服务**：http://localhost:8080/practice/**
- **搜索服务**：http://localhost:8080/search/**

## ⚠️ 注意事项

1. **端口冲突**：确保宿主机上没有其他服务占用 3306、6379、8848、9200、5601 端口

2. **数据库密码**：
   - Docker MySQL root 密码：`123456`
   - 微服务配置的密码：`123456`
   - 需要保持一致

3. **Nacos 配置**：
   - 默认用户名：nacos
   - 默认密码：nacos
   - 首次登录后建议修改密码

4. **数据持久化**：
   - MySQL 数据：`./mysql/data`
   - Redis 数据：`./redis/data`
   - ES 数据：`./elasticsearch/data`
   - Nacos 日志：`./nacos/logs`

5. **网络配置**：
   - 所有 Docker 容器在 `poti-network` 网络中
   - 微服务通过端口映射访问容器服务

## 🐛 常见问题

### 1. 无法连接到 MySQL

**原因**：MySQL 容器未启动或端口未映射

**解决**：
```bash
docker-compose ps mysql
docker-compose logs mysql
```

### 2. 无法连接到 Nacos

**原因**：Nacos 容器未启动或端口未映射

**解决**：
```bash
docker-compose ps nacos
docker-compose logs nacos
```

### 3. 微服务注册失败

**原因**：Nacos 地址配置错误

**解决**：检查 `bootstrap.yml` 中的 `server-addr` 是否为 `127.0.0.1:8848`

### 4. 数据库连接失败

**原因**：数据库未创建或密码不匹配

**解决**：
- 检查数据库是否已创建
- 检查密码是否为 `123456`
- 检查 MySQL 容器是否正在运行

## 📝 配置文件位置

- **Docker 配置**：`docker-compose.yml`
- **网关配置**：`poti-gateway/src/main/resources/`
- **用户服务配置**：`poti-user/src/main/resources/`
- **题库服务配置**：`poti-question/src/main/resources/`
- **刷题服务配置**：`poti-practice/src/main/resources/`
- **搜索服务配置**：`poti-search/src/main/resources/`
