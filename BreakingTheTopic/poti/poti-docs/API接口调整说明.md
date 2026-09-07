# API 接口调整说明

## 📋 调整概述

根据 `poti-docs/API文档.md` 中的接口规范，对现有的用户登录和认证接口进行了全面调整。

## 🔧 调整内容

### 1. 认证模块 (/auth)

#### 1.1 登录接口调整

**文件**: `poti-auth/src/main/java/com/poti/auth/controller/AuthController.java`

**调整前**:
- 接口路径: `POST /auth/login`
- 请求方式: `@RequestHeader("Authorization")`
- 功能: Token 认证

**调整后**:
- 接口路径: `POST /auth/login`
- 请求方式: `@RequestBody Map<String, String> request`
- 请求参数: `{"code": "微信登录临时凭证"}`
- 功能: 通过微信 code 换取用户信息并返回 JWT Token
- 返回数据:
  ```json
  {
    "code": 200,
    "msg": "success",
    "data": {
      "token": "eyJhbGciOiJIUzI1NiIs...",
      "userId": 1001,
      "nickname": "微信用户",
      "avatarUrl": "https://..."
    }
  }
  ```

**新增接口**:
- `POST /auth/refresh` - 刷新 Token
- `GET /auth/validate` - 验证 Token
- `GET /auth/userinfo` - 获取用户信息

#### 1.2 配置文件

**文件**: `poti-auth/src/main/resources/application-dev.yml`

**新增配置**:
```yaml
wx:
  appid: wx920f3d57f4121af7
  secret: your_secret_here
```

**文件**: `poti-auth/src/main/resources/application.yml`

**配置**:
```yaml
jwt:
  secret: poti2024secretkey1234567890abcdefghijklmnopqrstuvwxyz
  expire: 86400000  # 24小时
```

### 2. 用户模块 (/user)

#### 2.1 删除重复的登录接口

**删除文件**: `poti-user/src/main/java/com/poti/user/controller/LoginController.java`

**原因**: 登录接口已迁移到 `/auth/login`，避免重复

#### 2.2 获取用户信息

**文件**: `poti-user/src/main/java/com/poti/user/controller/UserController.java`

**接口**: `GET /user/info`

**请求头**:
```
Authorization: Bearer <token>
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "id": 1001,
    "nickname": "微信用户",
    "avatarUrl": "https://...",
    "gender": 1,
    "phone": null,
    "email": null,
    "status": 1
  }
}
```

#### 2.3 更新用户信息

**接口**: `PUT /user/info`

**请求体**:
```json
{
  "nickname": "新昵称",
  "avatarUrl": "新头像",
  "gender": 2,
  "phone": "13800000000",
  "email": "user@example.com"
}
```

**响应**: `{"code":200,"msg":"success"}`

#### 2.4 获取学习统计

**接口**: `GET /user/statistics`

**响应**:
```json
{
  "code": 200,
  "data": {
    "totalQuestionCount": 150,
    "correctCount": 120,
    "wrongCount": 30,
    "favoriteCount": 8,
    "lastPracticeTime": "2024-03-21 10:30:00"
  }
}
```

#### 2.5 获取刷题日历

**接口**: `GET /user/calendar?year=2024&month=3`

**响应**:
```json
{
  "code": 200,
  "data": {
    "list": [
      {"date": "2024-03-01", "count": 10},
      {"date": "2024-03-02", "count": 5}
    ]
  }
}
```

#### 2.6 Service 层调整

**文件**: `poti-user/src/main/java/com/poti/user/service/UserService.java`

**新增方法**:
```java
Map<String, Object> getStatistics(Long userId);
Map<String, Object> getCalendar(Long userId, Integer year, Integer month);
```

**文件**: `poti-user/src/main/java/com/poti/user/service/impl/UserServiceImpl.java`

**实现方法**:
- `getStatistics()`: 返回用户学习统计数据
- `getCalendar()`: 返回月度刷题日历

### 3. 网关模块 (/gateway)

#### 3.1 认证过滤器白名单调整

**文件**: `poti-gateway/src/main/java/com/poti/gateway/filter/AuthGlobalFilter.java`

**调整前白名单**:
```java
private static final List<String> EXCLUDE_PATHS = Arrays.asList(
    "/auth/login",
    "/user/login",
    "/user/register",
    "/question/list",
    "/question/detail"
);
```

**调整后白名单**:
```java
private static final List<String> EXCLUDE_PATHS = Arrays.asList(
    "/auth/login",
    "/auth/validate",
    "/auth/refresh",
    "/user/login",
    "/user/register",
    "/question/list",
    "/question/categories",
    "/question/detail",
    "/search/questions"
);
```

**新增白名单路径**:
- `/auth/validate` - Token 验证
- `/auth/refresh` - Token 刷新
- `/question/categories` - 题目分类
- `/search/questions` - 题目搜索

### 4. JWT 工具类调整

**文件**: `poti-common/poti-common-security/src/main/java/com/poti/common/security/JwtUtil.java`

**新增方法**:
```java
public String getOpenidFromToken(String token)
public boolean isTokenExpired(String token)
public String refreshToken(String token)
```

**优化**:
- `validateToken()` 方法增加了过期时间检查
- `refreshToken()` 方法用于刷新过期的 Token

## 📊 接口对照表

| 模块 | 接口 | 方法 | 说明 | 认证 | 状态 |
|------|------|------|------|------|------|
| /auth | /auth/login | POST | 微信登录 | 否 | ✅ 已调整 |
| /auth | /auth/refresh | POST | 刷新 Token | 否 | ✅ 新增 |
| /auth | /auth/validate | GET | 验证 Token | 否 | ✅ 新增 |
| /auth | /auth/userinfo | GET | 获取用户信息 | 否 | ✅ 新增 |
| /user | /user/info | GET | 获取用户信息 | 是 | ✅ 已调整 |
| /user | /user/info | PUT | 更新用户信息 | 是 | ✅ 新增 |
| /user | /user/statistics | GET | 获取学习统计 | 是 | ✅ 新增 |
| /user | /user/calendar | GET | 获取刷题日历 | 是 | ✅ 新增 |

## 🎯 测试建议

### 1. 测试登录流程

```bash
# 1. 微信登录
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "code": "微信登录code"
}

# 2. 获取用户信息
GET http://localhost:8080/user/info
Authorization: Bearer <token>

# 3. 更新用户信息
PUT http://localhost:8080/user/info
Authorization: Bearer <token>
Content-Type: application/json

{
  "nickname": "新昵称",
  "avatarUrl": "新头像"
}

# 4. 获取学习统计
GET http://localhost:8080/user/statistics
Authorization: Bearer <token>

# 5. 获取刷题日历
GET http://localhost:8080/user/calendar?year=2024&month=3
Authorization: Bearer <token>

# 6. 刷新 Token
POST http://localhost:8080/auth/refresh
Authorization: Bearer <token>
```

### 2. 验证网关认证

```bash
# 测试白名单接口（无需 Token）
GET http://localhost:8080/auth/validate
GET http://localhost:8080/question/list

# 测试需要认证的接口（无 Token）
GET http://localhost:8080/user/info
# 预期返回: 401 未授权

# 测试需要认证的接口（有 Token）
GET http://localhost:8080/user/info
Authorization: Bearer <token>
# 预期返回: 200 成功
```

## ⚠️ 注意事项

1. **微信配置**: 需要在 `poti-auth/src/main/resources/application-dev.yml` 中配置正确的 `wx.appid` 和 `wx.secret`

2. **JWT 密钥**: 建议在生产环境中修改 `jwt.secret` 为更复杂的密钥

3. **Token 过期时间**: 默认为 24 小时，可根据需求调整 `jwt.expire`

4. **数据库表**: 确保 `user` 表已创建，包含必要的字段

5. **服务注册**: 确保所有服务已成功注册到 Nacos

## 📝 后续工作

根据 API 文档，还需要实现以下模块：

1. **题库模块** (/question)
   - GET /question/categories - 获取题目分类树
   - GET /question/list - 分页获取题目列表
   - GET /question/detail/{questionId} - 获取题目详情

2. **刷题模块** (/practice)
   - POST /practice/submit - 提交答案
   - GET /practice/history - 获取刷题历史
   - GET /practice/record/{questionId} - 获取某题目的刷题记录

3. **错题本模块** (/wrongbook)
   - GET /wrongbook/list - 获取错题本列表
   - PUT /wrongbook/mastered/{questionId} - 标记错题为已掌握
   - DELETE /wrongbook/{questionId} - 移除错题
   - GET /wrongbook/statistics - 错题分析统计

4. **收藏模块** (/favorite)
   - POST /favorite/add - 添加收藏
   - DELETE /favorite/{questionId} - 取消收藏
   - GET /favorite/list - 获取收藏列表
   - GET /favorite/check/{questionId} - 检查是否已收藏

5. **模拟面试模块** (/interview)
   - POST /interview/start - 开始一场面试
   - POST /interview/submit - 提交面试答案
   - GET /interview/history - 获取面试历史
   - GET /interview/detail/{interviewId} - 获取面试详情

6. **搜索模块** (/search)
   - GET /search/questions - 题目搜索

## 📚 参考资料

- [API文档.md](file:///d:/study/BreakingTheTopic/poti/poti-docs/API文档.md) - 完整的 API 接口文档
- [用户登录和认证测试文档.md](file:///d:/study/BreakingTheTopic/poti/用户登录和认证测试文档.md) - 登录和认证测试指南
