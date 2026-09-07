# 🚀 破题小程序部署检查清单

## 📅 检查时间
**日期**: 2026-05-14
**版本**: 1.0.0
**检查人**: AI Assistant

---

## ✅ 已完成检查项

### 1. 后端代码检查
- ✅ SpringDoc API文档集成完成
- ✅ 全局异常处理已优化
- ✅ 环境变量配置已优化
- ✅ 安全问题已修复（敏感信息使用环境变量）

### 2. 前端代码检查
- ✅ 小程序配置已简化
- ✅ process.env问题已修复

### 3. 数据库检查
- ✅ 数据库连接配置正确
- ✅ 环境变量支持已添加

---

## ⚠️ 需要修复的问题

### 🔴 高优先级问题

#### 1. 前端配置问题
**文件**: `poti-frontend/config/index.js`
**问题**: 使用硬编码IP地址
**影响**: 无法在生产环境使用
**修复方案**:
```javascript
// 需要修改为生产环境地址
const config = {
  baseUrl: 'http://你的生产环境IP:8080',
  authUrl: 'http://你的生产环境IP:8100',
  userUrl: 'http://你的生产环境IP:8200',
  // ... 其他服务地址
}
```

#### 2. 后端配置问题
**文件**: 多个 `application-dev.yml`
**问题**: 部分配置未使用环境变量
**影响**: 生产环境部署困难
**修复方案**:
- poti-wrongbook: 需要使用环境变量
- poti-favorite: 需要使用环境变量
- poti-practice: 需要使用环境变量

#### 3. 日志问题
**文件**: `UserCacheServiceImpl.java`
**问题**: 使用 `e.printStackTrace()`
**影响**: 日志不规范，生产环境难以追踪
**修复方案**:
```java
// 需要改为
log.error("缓存用户信息失败", e);
```

#### 4. 前端调试代码
**文件**: 多个前端JS文件
**问题**: 存在大量 `console.log`
**影响**: 生产环境性能影响，信息泄露风险
**修复方案**: 移除或禁用console.log

---

### 🟡 中优先级问题

#### 5. 数据库连接池配置
**问题**: 未配置连接池参数
**影响**: 高并发时可能性能问题
**修复方案**:
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

#### 6. 生产环境配置文件缺失
**问题**: 没有 `application-prod.yml`
**影响**: 无法区分开发和生产环境
**修复方案**: 创建生产环境配置文件

#### 7. Redis密码配置
**问题**: 部分服务Redis密码为空
**影响**: 安全风险
**修复方案**: 配置Redis密码

---

## 📋 部署前必须完成的任务

### 第一步：修复配置问题
- [ ] 修改前端config/index.js为生产环境地址
- [ ] 统一后端配置文件使用环境变量
- [ ] 创建application-prod.yml配置文件
- [ ] 配置Redis密码

### 第二步：修复代码问题
- [ ] 替换e.printStackTrace()为日志
- [ ] 移除前端console.log
- [ ] 添加数据库连接池配置

### 第三步：安全检查
- [ ] 确认所有敏感信息使用环境变量
- [ ] 检查数据库用户权限（不应使用root）
- [ ] 配置防火墙规则
- [ ] 启用HTTPS

### 第四步：性能优化
- [ ] 配置数据库连接池
- [ ] 配置Redis连接池
- [ ] 启用Gzip压缩
- [ ] 配置CDN（可选）

### 第五步：监控和日志
- [ ] 配置日志输出到文件
- [ ] 设置日志滚动策略
- [ ] 配置监控告警（可选）
- [ ] 准备日志分析工具（可选）

---

## 🔧 快速修复脚本

### 1. 修复UserCacheServiceImpl.java
```bash
# 将在第54、69、81行的e.printStackTrace()替换为log.error
```

### 2. 移除前端console.log
```bash
# 需要手动移除或使用构建工具自动移除
```

### 3. 创建生产环境配置
```bash
# 复制application-dev.yml为application-prod.yml
# 修改为生产环境配置
```

---

## 📊 服务启动顺序

### 基础服务（必须先启动）
1. MySQL数据库
2. Redis缓存
3. Nacos注册中心

### 应用服务（按顺序启动）
1. poti-gateway (8080) - 网关
2. poti-auth (8100) - 认证服务
3. poti-user (8200) - 用户服务
4. poti-question (8300) - 题目服务
5. poti-practice (8400) - 练习服务
6. poti-wrongbook (8500) - 错题本服务
7. poti-favorite (8600) - 收藏服务
8. poti-interview (8700) - 面试服务
9. poti-search (8800) - 搜索服务
10. poti-admin (8088) - 管理后台

---

## 🌐 端口清单

| 服务 | 端口 | 说明 |
|-----|------|------|
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |
| Nacos | 8848 | 注册中心 |
| Gateway | 8080 | 网关入口 |
| Auth | 8100 | 认证服务 |
| User | 8200 | 用户服务 |
| Question | 8300 | 题目服务 |
| Practice | 8400 | 练习服务 |
| Wrongbook | 8500 | 错题本服务 |
| Favorite | 8600 | 收藏服务 |
| Interview | 8700 | 面试服务 |
| Search | 8800 | 搜索服务 |
| Admin | 8088 | 管理后台 |

---

## 🔒 安全检查清单

### 环境变量
- [ ] DB_HOST - 数据库地址
- [ ] DB_PORT - 数据库端口
- [ ] DB_USERNAME - 数据库用户名
- [ ] DB_PASSWORD - 数据库密码
- [ ] REDIS_HOST - Redis地址
- [ ] REDIS_PORT - Redis端口
- [ ] REDIS_PASSWORD - Redis密码
- [ ] JWT_SECRET - JWT密钥
- [ ] WX_APPID - 微信AppID
- [ ] WX_SECRET - 微信Secret

### 数据库安全
- [ ] 不使用root用户
- [ ] 创建专用数据库用户
- [ ] 限制用户权限
- [ ] 启用SSL连接（可选）

### 网络安全
- [ ] 配置防火墙
- [ ] 只开放必要端口
- [ ] 启用HTTPS
- [ ] 配置CORS

---

## 📝 部署文档位置

- 部署文档: `DEPLOYMENT.md`
- 环境配置脚本: `setup_env.bat`
- 数据库脚本: `sql/`
- 配置文件: `poti-frontend/config/`

---

## ✅ 最终检查

部署前请确认：
- [ ] 所有高优先级问题已修复
- [ ] 所有环境变量已配置
- [ ] 数据库已初始化
- [ ] 基础服务已启动
- [ ] 应用服务启动成功
- [ ] API文档可访问
- [ ] 小程序可正常使用

---

**生成时间**: 2026-05-14 13:40:00
**状态**: 待修复
