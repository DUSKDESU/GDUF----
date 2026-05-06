# GDUF----
GDUF第三轮后端考核说明(智能应用方向)
# OpenAI API 代理平台

## 项目说明

本项目是一个企业级 OpenAI API 代理平台，基于 Spring Boot 3.x 构建，提供完整的用户认证、API Key 管理、配额限制、异步请求处理等核心功能。平台支持多用户并发访问、限流控制、使用量统计，并集成了 Swagger/OpenAPI 3 文档。

### 核心功能

- **用户认证系统**：支持用户注册、登录、JWT 令牌认证
- **API Key 管理**：支持创建、更新、删除 API Key，支持启用/禁用控制
- **配额与限流**：支持按 API Key 设置速率限制和每日配额限制
- **异步请求处理**：使用 DeferredResult 实现异步非阻塞请求处理
- **OpenAI API 代理**：兼容 OpenAI API 接口规范，代理聊天完成、文本完成等接口
- **权限控制**：基于 Spring Security + JWT 的 RBAC 权限控制
- **API 文档**：集成 SpringDoc OpenAPI 3，提供可视化接口文档
  
  ### 技术栈
- **后端框架**：Spring Boot 3.2.5
- **编程语言**：Java 17
- **数据库**：MySQL 8.0+
- **ORM 框架**：Spring Data JPA
- **安全认证**：Spring Security + JWT
- **API 文档**：SpringDoc OpenAPI (Swagger UI)
- **构建工具**：Maven
- **其他**：Lombok, Hibernate Validator


┌─────────────────────────────────────────────────────────────┐
│                        Client (SDK/Web)                      │
└──────────────────┬──────────────────────────────────────────┘
                   │ HTTP Request (Bearer Token)
                   ▼
┌─────────────────────────────────────────────────────────────┐
│                   Spring Security Filter                     │
│                   (JWT 认证 + 授权)                           │
└──────────────────┬──────────────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────────────┐
│                    REST Controllers                          │
│  • AuthController (认证管理)                                  │
│  • UserController (用户管理)                                  │
│  • ApiKeyController (API 密钥管理)                            │
│  • OpenAIProxyController (OpenAI API 代理)                   │
│  • ModelController (模型管理)                                 │
│  • FileController (文件管理)                                  │
│  • AdminController (管理员功能)                               │
└──────────────────┬──────────────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────────────┐
│                     Service Layer                            │
│  • UserService (用户业务逻辑)                                 │
│  • ApiKeyService (API 密钥业务逻辑)                           │
│  • CompletionService (AI 生成业务逻辑)                        │
│  • ModelService (模型管理逻辑)                                │
│  • FileService (文件管理逻辑)                                 │
└──────────────────┬──────────────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────────────┐
│                    Repository Layer                          │
│  (Spring Data JPA - 数据访问层)                               │
└──────────────────┬──────────────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────────────┐
│                    MySQL Database                            │
│  • users (用户表)                                            │
│  • api_keys (API 密钥表)                                     │
│  • completions (AI 生成记录表)                                │
│  • models (模型配置表)                                       │
│  • files (文件表)                                            │
└─────────────────────────────────────────────────────────────┘



