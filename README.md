## GDUF第三轮后端考核说明(智能应用方向)

## 项目说明

本项目是GDUF第三轮考核项目，相比于第二轮考核，

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


# 项目架构

## 系统架构图

```mermaid
graph TD
    A[Client SDK/Web] --> B[HTTP Request Bearer Token]
    B --> C[Spring Security Filter<br/>JWT认证+授权]
    C --> D[REST Controllers]
    D --> E[AuthController<br/>认证管理]
    D --> F[UserController<br/>用户管理]
    D --> G[ApiKeyController<br/>API密钥管理]
    D --> H[OpenAIProxyController<br/>OpenAI API代理]
    D --> I[ModelController<br/>模型管理]
    D --> J[FileController<br/>文件管理]
    D --> K[AdminController<br/>管理员功能]
    D --> L[Service Layer]
    L --> M[UserService<br/>用户业务逻辑]
    L --> N[ApiKeyService<br/>API密钥业务逻辑]
    L --> O[CompletionService<br/>AI生成业务逻辑]
    L --> P[ModelService<br/>模型管理逻辑]
    L --> Q[FileService<br/>文件管理逻辑]
    Q --> R[Repository Layer<br/>Spring Data JPA]
    R --> S[MySQL Database]
    S --> T[users<br/>用户表]
    S --> U[api_keys<br/>API密钥表]
    S --> V[completions<br/>AI生成记录表]
    S --> W[models<br/>模型配置表]
    S --> X[files<br/>文件表]

```
## 鉴权与生成流程说明
- **JWT 认证流程**

```mermaid
graph LR
    %% --- 流程部分 ---
    %% 定义节点
    C1[Client]
    AC[AuthController]
    C1_Out[Client]
    JU[JwtUtil<br/>生成 Access Token]

    %% 定义连线
    C1 -- "注册/登录" --> AC
    AC -- "返回 JWT Token" --> C1_Out
    AC -- "UserService.login()" --> JU

```
- **AI 生成请求流程**
  
```mermaid

graph TD
    Client3[Client] -->|提交生成请求| Proxy[OpenAIProxyCtrl]
    
    subgraph Validation [验证阶段]
        direction TB
        Proxy -->|1.验证 API Key| KeySvc[ApiKeyService]
        KeySvc --> Res[验证结果]
    end

    subgraph Execution [执行阶段]
        direction TB
        Res -.-> Comp[CompletionService<br/>检查配额限制]
        Comp -->|3.调用 OpenAI API| Rest[RestTemplate<br/>异步调用]
        Rest --> OpenAI[OpenAI API]
    end
    
    Rest -->|4.返回结果| Client3

```
- **API Key 生成流程**
```mermaid
graph LR
    %% 定义节点
    Client1[Client]
    Ctrl[ApiKeyController]
    Client2[Client]
    Service[生成 sk-xxx 格式密钥<br/>保存到数据库]

    %% 定义连线
    Client1 -- "创建 API Key" --> Ctrl
    Ctrl -- "生成密钥" --> Client2
    Ctrl -- "ApiKeyService.createApiKey()" --> Service
```
**数据库配置**

CREATE DATABASE openai CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;




