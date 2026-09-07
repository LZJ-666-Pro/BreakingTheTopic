# 🚀 破题 - 微服务刷题小程序

> 破题万卷，码上有 offer

**破题** 是一款基于微服务架构的微信刷题小程序，旨在帮助程序员通过高质量题目练习，攻克技术面试难关。项目名称寓意“攻克难题”、“突破自我”，简洁有力，既适合技术实践也具备商业落地价值。

---

## ✨ 主要功能

- 📚 **海量题库**：支持多种题目类型（单选、多选、判断、简答、编程），分类清晰，难度分级。
- 📝 **智能刷题**：顺序刷题、随机挑战、专项突破，记录学习进度。
- ❌ **错题本**：自动记录错题，支持错题重做、掌握标记。
- ⭐ **收藏功能**：收藏经典题目，便于回顾。
- 🎤 **模拟面试**：限时答题，AI 评分，模拟大厂面试流程。
- 🔍 **题目搜索**：基于 Elasticsearch 的全文检索，快速定位题目。
- 📊 **学习统计**：刷题日历图、正确率分析，直观了解学习成果。
- 🔐 **微信登录**：一键授权登录，JWT 鉴权，安全可靠。

---

## 🧱 技术栈

| 类别 | 技术 |
|------|------|
| 后端框架 | Spring Boot 2.7.0 + Spring Cloud Alibaba 2021.0.4.0 |
| 注册/配置中心 | Nacos 2.2.0 |
| 服务网关 | Spring Cloud Gateway |
| ORM | MyBatis-Plus 3.5.2 |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis 7.0 + Caffeine |
| 搜索引擎 | Elasticsearch 7.17.0 |
| 流量控制 | Sentinel 1.8.5 |
| 热Key探测 | JD-HotKey |
| 前端 | 微信小程序原生框架 + Vant Weapp |
| 容器化 | Docker + Docker Compose |

---

## 📁 微服务模块

| 服务名 | 端口 | 说明 |
|--------|------|------|
| poti-gateway | 8080 | 统一入口，路由转发，鉴权 |
| poti-auth | 8100 | 认证中心（JWT 签发） |
| poti-user | 8200 | 用户服务（微信登录、个人信息） |
| poti-question | 8300 | 题库服务（题目管理、分类） |
| poti-practice | 8400 | 刷题服务（记录、统计） |
| poti-wrongbook | 8500 | 错题本服务 |
| poti-favorite | 8600 | 收藏服务 |
| poti-interview | 8700 | 模拟面试服务 |
| poti-search | 8800 | 搜索服务（ES） |
| poti-monitor | 8888 | 监控中心（Spring Boot Admin） |

---

## 🚀 快速开始

### 环境要求
- JDK 17+
- Maven 3.8+
- Docker & Docker Compose
- Git

### 1. 克隆项目
```bash
git clone https://github.com/yourname/poti.git
cd poti