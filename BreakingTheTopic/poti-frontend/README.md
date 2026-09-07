# 破题微信小程序

## 项目介绍
破题是一个基于微信原生框架开发的智能刷题小程序，使用 Vant Weapp 组件库构建。

## 技术栈
- 微信小程序原生框架
- Vant Weapp 组件库
- 微信云开发（可选）

## 项目结构
```
poti-frontend/
├── pages/              # 页面目录
│   ├── index/         # 首页
│   ├── login/         # 登录页
│   ├── question/      # 题库页
│   ├── practice/      # 刷题页
│   ├── wrongbook/     # 错题本页
│   ├── favorite/      # 收藏页
│   ├── interview/     # 模拟面试页
│   └── user/         # 用户中心页
├── images/            # 图片资源
├── app.js            # 小程序逻辑
├── app.json           # 小程序配置
├── app.wxss          # 小程序样式
├── sitemap.json       # 站点地图
├── project.config.json # 项目配置
└── package.json       # 依赖管理
```

## 功能模块
1. 用户认证
   - 微信一键登录
   - 用户信息管理

2. 题库功能
   - 题目分类浏览
   - 题目搜索

3. 刷题功能
   - 单选题/多选题
   - 答案提交
   - 答案解析

4. 错题本
   - 错题记录
   - 错题复习

5. 收藏功能
   - 题目收藏
   - 收藏管理

6. 模拟面试
   - 基础面试
   - 进阶面试
   - 算法面试

## 开发环境
- 微信开发者工具：最新稳定版
- Node.js：14.0+
- 微信小程序基础库：2.19.4+

## 安装步骤
1. 克隆项目
```bash
git clone <repository-url>
cd poti-frontend
```

2. 安装依赖
```bash
npm install
```

3. 配置小程序
- 在 `project.config.json` 中配置 `appid`
- 在 `app.js` 中配置 `baseUrl`

4. 导入微信开发者工具
- 打开微信开发者工具
- 导入项目目录
- 点击"编译"按钮

## 配置说明
### 后端接口地址
在 `app.js` 中修改 `baseUrl`：
```javascript
globalData: {
  userInfo: null,
  token: null,
  baseUrl: 'http://localhost:8080'  // 修改为实际后端地址
}
```

### 小程序 AppID
在 `project.config.json` 中修改 `appid`：
```json
{
  "appid": "your_appid_here"  // 替换为实际的小程序 AppID
}
```

## API 接口说明
所有接口都通过 `wx.request` 调用，请求头需要携带 token：
```javascript
wx.request({
  url: `${app.globalData.baseUrl}/api/endpoint`,
  method: 'GET',
  header: {
    'Authorization': `Bearer ${token}`
  },
  success: (res) => {
    // 处理响应
  }
})
```

## 开发规范
1. 命名规范
   - 页面文件：小写字母，用 `-` 分隔
   - 变量命名：驼峰命名法
   - 常量命名：全大写，用 `_` 分隔

2. 代码规范
   - 使用 ES6+ 语法
   - 统一使用 `const` 和 `let`，避免使用 `var`
   - 使用箭头函数

3. 样式规范
   - 使用 rpx 单位适配不同屏幕
   - 避免使用 ID 选择器
   - 使用 class 选择器

## 注意事项
1. 所有页面必须在 `app.json` 中注册
2. 使用 Vant 组件时需要在 `app.json` 中引入
3. 网络请求需要在微信公众平台配置服务器域名
4. 用户信息需要加密存储

## 常见问题
1. Vant 组件无法使用
   - 检查 `app.json` 中的 `usingComponents` 配置
   - 确认 `npm install` 已执行

2. 网络请求失败
   - 检查 `baseUrl` 配置是否正确
   - 确认后端服务是否启动
   - 检查微信公众平台域名配置

3. 登录失败
   - 确认小程序 AppID 配置正确
   - 检查后端登录接口是否正常

## 更新日志
### v1.0.0 (2024-01-01)
- 初始版本发布
- 实现基础功能模块
