# 🐳 破题小程序 - Docker部署指南

## 📋 前置要求

### 1. 安装Docker

**Windows系统**:
1. 下载Docker Desktop: https://www.docker.com/products/docker-desktop
2. 安装并启动Docker Desktop
3. 验证安装:
   ```powershell
   docker --version
   docker-compose --version
   ```

**Linux系统**:
```bash
# 安装Docker
curl -fsSL https://get.docker.com | bash

# 安装Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# 验证安装
docker --version
docker-compose --version
```

### 2. 系统要求

- **内存**: 至少8GB（推荐16GB）
- **磁盘**: 至少20GB可用空间
- **CPU**: 至少4核（推荐8核）

---

## 🚀 快速部署（推荐）

### 方法1：使用部署脚本（最简单）

**Windows系统**:
```powershell
# 进入项目目录
cd d:\study\BreakingTheTopic\poti

# 运行部署脚本
.\deploy-docker.bat
```

**Linux/Mac系统**:
```bash
# 进入项目目录
cd /path/to/BreakingTheTopic/poti

# 添加执行权限
chmod +x deploy-docker.sh

# 运行部署脚本
./deploy-docker.sh
```

**脚本会自动完成**:
1. ✅ 清理旧的构建文件
2. ✅ 编译打包所有服务
3. ✅ 构建Docker镜像
4. ✅ 启动所有服务

---

### 方法2：手动部署（分步操作）

#### 第一步：编译打包

```bash
# 进入项目目录
cd d:\study\BreakingTheTopic\poti

# 清理并打包
mvn clean package -DskipTests
```

**预期结果**:
```
[INFO] BUILD SUCCESS
[INFO] Total time:  02:30 min
```

#### 第二步：构建Docker镜像

```bash
# 构建所有服务的Docker镜像
docker-compose -f docker-compose-full.yml build
```

**预期结果**:
```
Successfully built xxxxxxx
Successfully tagged poti-gateway:latest
...
```

#### 第三步：启动所有服务

```bash
# 启动所有服务（后台运行）
docker-compose -f docker-compose-full.yml up -d
```

**预期结果**:
```
Creating poti-mysql ... done
Creating poti-redis ... done
Creating poti-nacos ... done
Creating poti-gateway ... done
...
```

---

## 📊 服务管理

### 查看服务状态

```bash
# 查看所有服务状态
docker-compose -f docker-compose-full.yml ps

# 查看服务日志
docker-compose -f docker-compose-full.yml logs

# 查看特定服务日志
docker-compose -f docker-compose-full.yml logs poti-user

# 实时查看日志
docker-compose -f docker-compose-full.yml logs -f
```

### 停止服务

```bash
# 停止所有服务
docker-compose -f docker-compose-full.yml stop

# 停止并删除所有服务
docker-compose -f docker-compose-full.yml down

# 停止并删除所有服务和数据卷
docker-compose -f docker-compose-full.yml down -v
```

### 重启服务

```bash
# 重启所有服务
docker-compose -f docker-compose-full.yml restart

# 重启特定服务
docker-compose -f docker-compose-full.yml restart poti-user
```

### 扩容服务

```bash
# 扩容用户服务到3个实例
docker-compose -f docker-compose-full.yml up -d --scale poti-user=3
```

---

## 🔍 验证部署

### 1. 检查服务健康状态

```bash
# 检查所有容器状态
docker ps

# 应该看到所有容器状态为 "Up"
```

### 2. 访问服务

**基础服务**:
- MySQL: `localhost:3306`
- Redis: `localhost:6379`
- Nacos: http://localhost:8848/nacos (nacos/nacos)
- Elasticsearch: http://localhost:9201

**应用服务**:
- 网关: http://localhost:8080/actuator/health
- 认证: http://localhost:8100/actuator/health
- 用户: http://localhost:8200/swagger-ui.html
- 题目: http://localhost:8300/swagger-ui.html
- 练习: http://localhost:8400/actuator/health
- 错题本: http://localhost:8500/actuator/health
- 收藏: http://localhost:8600/actuator/health
- 面试: http://localhost:8700/actuator/health
- 搜索: http://localhost:8800/actuator/health
- 管理后台: http://localhost:8088/actuator/health

### 3. 检查Nacos服务注册

1. 浏览器访问: http://localhost:8848/nacos
2. 登录（用户名/密码: nacos/nacos）
3. 点击"服务管理" → "服务列表"
4. 应该看到所有10个微服务已注册

### 4. 测试API接口

```bash
# 测试用户服务
curl http://localhost:8200/user/info

# 测试题目服务
curl http://localhost:8300/question/categories
```

---

## 🔧 配置说明

### 环境变量配置

创建 `.env` 文件（可选）:

```bash
# 数据库配置
DB_PASSWORD=your_password

# Redis配置
REDIS_PASSWORD=your_redis_password

# JVM配置
JAVA_OPTS=-Xms512m -Xmx1024m
```

### 修改服务配置

**修改端口映射**:
编辑 `docker-compose-full.yml`，修改 `ports` 部分:
```yaml
services:
  poti-user:
    ports:
      - "8200:8200"  # 修改为 "新端口:8200"
```

**修改资源限制**:
```yaml
services:
  poti-user:
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 1G
        reservations:
          cpus: '1'
          memory: 512M
```

---

## 📁 目录结构

部署后会创建以下目录：

```
poti/
├── mysql/
│   ├── data/          # MySQL数据
│   └── init/          # 初始化SQL脚本
├── redis/
│   └── data/          # Redis数据
├── nacos/
│   └── logs/          # Nacos日志
├── elasticsearch/
│   └── data/          # ES数据
└── docker-compose-full.yml
```

---

## 🐛 常见问题

### 问题1：端口被占用

**错误信息**:
```
Error: port is already allocated
```

**解决方法**:
```bash
# 查看端口占用
netstat -ano | findstr "8080"

# 修改docker-compose-full.yml中的端口映射
```

### 问题2：内存不足

**错误信息**:
```
ERROR: Service 'poti-user' failed to build
```

**解决方法**:
1. 增加Docker内存限制（Docker Desktop设置）
2. 减少服务数量或降低JVM内存配置

### 问题3：镜像构建失败

**错误信息**:
```
COPY failed: stat /var/lib/docker/tmp/...: no such file or directory
```

**解决方法**:
```bash
# 确保先执行打包
mvn clean package -DskipTests

# 然后再构建镜像
docker-compose -f docker-compose-full.yml build
```

### 问题4：服务启动失败

**解决方法**:
```bash
# 查看服务日志
docker-compose -f docker-compose-full.yml logs poti-user

# 检查依赖服务是否启动
docker-compose -f docker-compose-full.yml ps
```

### 问题5：数据库连接失败

**解决方法**:
```bash
# 检查MySQL是否启动
docker-compose -f docker-compose-full.yml ps mysql

# 查看MySQL日志
docker-compose -f docker-compose-full.yml logs mysql

# 进入MySQL容器
docker exec -it poti-mysql mysql -u root -p
```

---

## 📊 性能优化

### 1. 调整JVM参数

编辑各服务的Dockerfile:
```dockerfile
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

### 2. 调整数据库连接池

在 `application-docker.yml` 中:
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 30
      minimum-idle: 10
```

### 3. 使用Docker网络优化

```yaml
networks:
  poti-network:
    driver: bridge
    ipam:
      config:
        - subnet: 172.20.0.0/16
```

---

## 🔄 更新部署

### 更新单个服务

```bash
# 重新构建特定服务
docker-compose -f docker-compose-full.yml build poti-user

# 重启服务
docker-compose -f docker-compose-full.yml up -d poti-user
```

### 更新所有服务

```bash
# 重新构建所有镜像
docker-compose -f docker-compose-full.yml build

# 重启所有服务
docker-compose -f docker-compose-full.yml up -d
```

---

## 📝 生产环境建议

### 1. 使用Docker Swarm或Kubernetes

对于生产环境，建议使用容器编排工具：
- Docker Swarm（简单）
- Kubernetes（功能强大）

### 2. 配置持久化存储

```yaml
volumes:
  mysql-data:
    driver: local
    driver_opts:
      type: none
      o: bind
      device: /data/mysql
```

### 3. 配置日志收集

```yaml
services:
  poti-user:
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"
```

### 4. 配置监控

使用Prometheus + Grafana监控服务状态。

---

## 🎯 部署成功标志

部署完成后，请确认：

- [ ] 所有容器状态为 "Up"
- [ ] Nacos中能看到所有服务
- [ ] API文档可以访问
- [ ] 健康检查全部通过
- [ ] 小程序功能正常

---

## 📞 技术支持

**相关文档**:
- 部署检查清单: `DEPLOYMENT_CHECKLIST.md`
- 优化建议: `OPTIMIZATION_GUIDE.md`
- 最终部署指南: `FINAL_DEPLOYMENT_GUIDE.md`

**常用命令**:
```bash
# 查看服务状态
docker-compose -f docker-compose-full.yml ps

# 查看日志
docker-compose -f docker-compose-full.yml logs -f

# 重启服务
docker-compose -f docker-compose-full.yml restart

# 停止服务
docker-compose -f docker-compose-full.yml down
```

---

## 🎉 总结

**Docker部署优势**:
- ✅ 环境一致性
- ✅ 快速部署
- ✅ 易于扩展
- ✅ 便于管理

**部署时间**: 约5-10分钟（首次部署）

**祝你部署顺利！** 🚀

---

**生成时间**: 2026-05-14
**状态**: ✅ Docker配置完成
