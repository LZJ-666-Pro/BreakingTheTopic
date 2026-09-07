## 本文档定义了破题小程序所有对外接口，统一通过网关 http://gateway-host:8080 访问，各服务路由前缀如下：

服务	路由前缀	说明
认证中心	/auth	登录、Token 刷新
用户服务	/user	用户信息、学习统计
题库服务	/question	题目、分类
刷题服务	/practice	刷题记录、提交
错题本服务	/wrongbook	错题管理
收藏服务	/favorite	收藏管理
模拟面试服务	/interview	面试记录
搜索服务	/search	题目搜索


1. 认证模块 /auth
### 描述：通过微信小程序 code 换取用户信息，并返回 JWT Token。
URL：POST /auth/login
请求参数（JSON Body）：
{
    "code": "微信登录临时凭证"
}
{
    "code": 200,
    "msg": "success",
    "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "userId": 1001,
    "nickname": "微信用户",
    "avatarUrl": "https://..."
}

## 刷新 Token（可选）
URL：POST /auth/refresh
请求头：携带旧 Token
响应：返回新 Token（结构与登录相同）


2. 用户模块 /user
### 描述：用户信息、学习统计
## 2.1 获取当前用户信息
URL：GET /user/info
响应：
json
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


## 2.2 更新用户信息
URL：PUT /user/info
请求体：
json
{
    "nickname": "新昵称",
    "avatarUrl": "新头像",
    "gender": 2,
    "phone": "13800000000",
    "email": "user@example.com"
}
响应：{"code":200,"msg":"success"}

## 2.3 获取学习统计
URL：GET /user/statistics
响应：
json
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

## 2.4 获取刷题日历（月度统计）
URL：GET /user/calendar?year=2024&month=3
响应：
json
{
    "code": 200,
    "data": [
    {"date": "2024-03-01", "count": 10},
    {"date": "2024-03-02", "count": 5},
    ...
    ]
}

3. 题库模块 /question
### 描述：题目、分类

## 3.1 获取题目分类树
URL：GET /question/categories
响应：
json
{
    "code": 200,
    "data": [
        {
            "id": 1,
            "name": "Java",
            "children": [
            {"id": 2, "name": "基础语法"},
            {"id": 3, "name": "多线程"}
            ]
        }
    ]
}

## 3.2 分页获取题目列表
URL：GET /question/list
请求参数：
categoryId：分类 ID（可选）
type：题型（1单选，2多选，3判断，4简答，5编程，可选）
difficulty：难度（1简单，2中等，3困难，可选）
keyword：标题关键词（可选）
pageNum：页码，默认1
pageSize：每页条数，默认20
响应：
json
{
    "code": 200,
    "data": {
        "total": 200,
        "list": [
            {
            "id": 101,
            "title": "下列哪个是Java关键字？",
            "type": 1,
            "difficulty": 1,
            "viewCount": 1280,
            "favoriteCount": 35
            }
        ]
    }
}

## 3.3 获取题目详情
URL：GET /question/detail/{questionId}
响应：
json
{
    "code": 200,
    "data": {
        "id": 101,
        "categoryId": 2,
        "title": "下列哪个是Java关键字？",
        "content": "A. public B. static C. void D. main",
        "type": 1,
        "difficulty": 1,
        "options": ["A. public", "B. static", "C. void", "D. main"],
        "analysis": "public、static、void 都是关键字，main 不是。",
        "tags": "基础,关键字",
        "viewCount": 1281
    }
}
注意：answer 字段仅管理员可见，普通用户不返回。


## 3.4 管理员：创建/编辑题目
URL：POST /question/save、PUT /question/update
请求体：包含完整题目信息（需管理员权限）

4. 刷题模块 /practice
### 描述：刷题记录、提交
## 4.1 提交答案
URL：POST /practice/submit
请求体：
json
{
    "questionId": 101,
    "userAnswer": "A",
    "spendSeconds": 12
}
响应：
json
{
    "code": 200,
    "data": {
        "isCorrect": true,
        "correctAnswer": "A",
        "analysis": "public、static、void 都是关键字，main 不是。"
    }
}

## 4.2 获取刷题历史（按时间倒序）
URL：GET /practice/history
请求参数：
pageNum、pageSize（默认1,20）
响应：
json
{
    "code": 200,
    "data": {
        "total": 50,
        "list": [
            {
                "questionId": 101,
                "title": "下列哪个是Java关键字？",
                "userAnswer": "A",
                "isCorrect": true,
                "practiceTime": "2024-03-21 14:30:00"
            }
        ]
    }
}

## 4.3 获取某题目的刷题记录（仅当前用户）
URL：GET /practice/record/{questionId}


5. 错题本模块 /wrongbook
### 描述：错题管理
## 5.1 获取错题本列表
URL：GET /wrongbook/list
请求参数：
mastered：0未掌握，1已掌握（可选）
pageNum、pageSize
响应：
json
{
    "code": 200,
    "data": {
        "total": 12,
        "list": [
            {
                "questionId": 203,
                "title": "Spring 事务传播行为有哪些？",
                "wrongCount": 2,
                "lastWrongTime": "2024-03-20 09:15:00"
            }
        ]
    }
}

## 5.2 标记错题为已掌握
URL：PUT /wrongbook/mastered/{questionId}

## 5.3 移除错题（从错题本删除）
URL：DELETE /wrongbook/{questionId}

## 5.4 错题分析统计
URL：GET /wrongbook/statistics
响应：
json
{
    "code": 200,
    "data": {
            "totalWrong": 12,
            "byCategory": [
            {"categoryName": "Java", "count": 5},
            {"categoryName": "数据库", "count": 7}
        ]
    }
}


6. 收藏模块 /favorite
### 描述：收藏题目
## 6.1 添加收藏
URL：POST /favorite/add
请求体：{"questionId": 101}

## 6.2 取消收藏
URL：DELETE /favorite/{questionId}

## 6.3 获取收藏列表
URL：GET /favorite/list

参数：pageNum、pageSize

## 6.4 检查是否已收藏
URL：GET /favorite/check/{questionId}
响应：{"code":200,"data":true}


7. 模拟面试模块 /interview
### 描述：模拟面试、面试记录
## 7.1 开始一场面试
URL：POST /interview/start
请求体：
json
{
"title": "Java 模拟面试",
"questionIds": [101, 203, 305]   // 可选，不传则随机生成
}
响应：
json
{
    "code": 200,
    "data": {
        "interviewId": 10001,
        "questions": [
        {"id": 101, "title": "题目1", "type": 1},
        {"id": 203, "title": "题目2", "type": 2}
        ]
    }
}

## 7.2 提交面试答案
URL：POST /interview/submit
请求体：
json
{
    "interviewId": 10001,
    "answers": [
    {"questionId": 101, "userAnswer": "A"},
    {"questionId": 203, "userAnswer": "B,C"}
    ]
}
响应：
json
{
    "code": 200,
    "data": {
        "score": 85.5,
        "correctCount": 2,
        "totalCount": 3,
        "details": [
        {"questionId": 101, "isCorrect": true, "correctAnswer": "A"},
        {"questionId": 203, "isCorrect": false, "correctAnswer": "A,C"}
        ]
    }
}

## 7.3 获取面试历史
URL：GET /interview/history
响应：
json
{
    "code": 200,
    "data": [
        {
        "id": 10001,
        "title": "Java 模拟面试",
        "startTime": "2024-03-21 15:00:00",
        "score": 85.5
        }
    ]
}

## 7.4 获取面试详情
URL：GET /interview/detail/{interviewId}



8. 搜索模块 /search
### 描述：题目搜索
## 8.1 题目搜索
URL：GET /search/questions
参数：
keyword：关键词（必填）
pageNum、pageSize
响应：与题目列表接口结构一致


9. 通用响应格式
## 所有接口返回统一 JSON 结构：
json
{
    "code": 200,          // 200成功，其他为错误码
    "msg": "success",     // 提示信息
    "data": {}            // 具体数据，可为对象、数组、基本类型
}

| code | msg | data |
401：未登录或 Token 无效
403：无权限
404：资源不存在
500：服务器内部错误


10. 注意事项
 时间格式：所有时间字段统一使用 yyyy-MM-dd HH:mm:ss。

分页参数：页码从1开始，每页条数默认20，最大100。

网关前缀：实际请求时需在路径前加上网关地址，例如 http://localhost:8080/user/info。

Token 传递：除登录接口外，必须在请求头中携带 Authorization: Bearer <token>。

敏感信息：题目答案、管理员接口需严格鉴权，普通用户不应获取答案。









