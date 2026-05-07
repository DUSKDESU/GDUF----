## GDUF第三轮后端考核说明(智能应用方向)

## 项目说明

本项目是GDUF爪哇部后端第三轮考核项目
实现以下功能
- **用户管理**：支持用户注册、登录和权限控制
- **API Key 管理**：多租户 API 密钥管理和使用统计
- **模型管理**：支持多种 AI 模型的配置和路由
- **文件服务**：文件上传、存储和管理功能
- **OpenAI 代理**：透明的 OpenAI API 代理转发服务


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
详细建表语句见  openai.text

修改 `src/main/resources/application.properties` 中的数据库配置：

properties spring.datasource.url=jdbc:mysql://localhost:3306/openai?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&characterEncoding=utf8 spring.datasource.username=your_username spring.datasource.password=your_password

修改 OpenAI API 配置：
properties openai.api.url=your_openai_api_url 
openai.api.key=your_openai_api_key



## SDK 测试说明

### 1. 用户注册和登录

#### 注册用户

```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }'


```
**响应示例**：
```json

{
  "success": true,
  "message": "注册成功",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000
  }
}

```
#### 用户登录
```bash
bash curl -X POST http://localhost:8081/api/auth/login
-H "Content-Type: application/json"
-d '{ "identifier":
"testuser",
"password":
"password123"
}'

```
**响应示例**：
```json
{ "success": true, 
"message": "注册成功", 
"data": 
{ "token": "eyJhbGciOiJIUzUxMiJ9...", 
"refreshToken": "eyJhbGciOiJIUzUxMiJ9...", 
"tokenType": "Bearer",
"expiresIn": 86400000 
} 
}

```


### 2. API Key 管理

#### 创建 API Key
```bash
curl -X POST http://localhost:8081/api/api-keys
-H "Content-Type: application/json"
-H "Authorization: Bearer YOUR_JWT_TOKEN"
-d '{ "name":
"测试密钥",
"rateLimit": 100,
 "dailyLimit": 1000,
 "expiresAt": "2025-12-31T23:59:59"
}'
```
**响应示例**：

```json
{ "success":
 true, "message":
 "操作成功",
"data":
{ "id": 1,
"apiKey":
 "sk-a1b2c3d4e5f6...",
 "maskedApiKey": "sk-***...f6g7",
"name": "测试密钥",
"isActive": true,
 "rateLimit": 100,
 "dailyLimit": 1000,
 "usedCount": 0,
"createdAt": "2025-05-06T10:30:00"
 }
}
```

#### 查看用户的所有 API Key
```bash
curl -X GET http://localhost:8081/api/api-keys 
-H "Authorization: Bearer YOUR_JWT_TOKEN"
```
#### 更新 API Key
```
bash curl -X PUT http://localhost:8081/api/api-keys/1
-H "Content-Type: application/json"
-H "Authorization: Bearer YOUR_JWT_TOKEN"
-d '{ "name": "更新后的密钥名称",
 "dailyLimit": 2000
}'

```

#### 禁用/启用 API Key

```bash
禁用
curl -X PATCH http://localhost:8081/api/api-keys/1/disable
-H "Authorization: Bearer YOUR_JWT_TOKEN"
启用
curl -X PATCH http://localhost:8081/api/api-keys/1/enable
-H "Authorization: Bearer YOUR_JWT_TOKEN"
```
#### 删除 API Key
```bash
curl -X DELETE http://localhost:8081/api/api-keys/1
-H "Authorization: Bearer YOUR_JWT_TOKEN"

```

### 3. OpenAI API 代理测试

#### 聊天完成（Chat Completions）

```bash curl -X POST http://localhost:8081/v1/chat/completions
-H "Content-Type: application/json"
-H "Authorization: Bearer YOUR_API_KEY"
-d '{ "model": "gpt-3.5-turbo",
"messages": [ { "role": "system",
"content": "你是一个 helpful assistant."},
{"role": "user", "content": "请介绍一下 Java 17 的新特性"} ],
 "temperature": 0.7, "max_tokens": 1000
}'

```
**响应示例**：
```json
 { "success": true,
 "message": "操作成功",
 "data": { "id": "chatcmpl-abc123",
 "object": "chat.completion",
 "created": 1680000000,
"model": "gpt-3.5-turbo",
 "choices": [ { "index": 0, "message": { "role": "assistant",
 "content": "Java 17 引入了以下新特性：\n1. 密封类（Sealed Classes）\n2. 模式匹配增强\n3. 新的 GC 算法\n..." },
"finish_reason": "stop" } ],
"usage": { "prompt_tokens": 50,
 "completion_tokens": 200,
 "total_tokens": 250
}
}
}

```

#### 获取模型列表
```bash curl -X GET http://localhost:8081/v1/models
-H "Authorization: Bearer YOUR_API_KEY"
```
#### 查询生成历史
```bash curl -X GET http://localhost:8081/v1/chat/completions/COMPLETION_ID
-H "Authorization: Bearer YOUR_API_KEY"
```

## AIGC 使用说明

本项目在开发过程中引入了人工智能生成内容技术，用于辅助代码编写、文档生成及资源制作，基本的代码风格增删改查等，结构框架等由自己设计，并让千问帮我写重复功能的体力活。\
使用范围\
代码辅助：部分通用逻辑代码（如工具类函数、正则表达式、数据库连接配置）由 AI 生成，并经人工审查与测试。\
文档编写：部分说明文档（如 README 结构、代码注释、API 文档模板）参考了 AI 生成的文本。\
数据/测试：部分测试数据（如 JSON 模拟数据、用户测试用例）由 AI 生成。\
代码bug修复：由ai查找并提供修改方案，经人工审查采用最优解。

## 用的一些注解的说明


| 注解 | Spring Boot 启动与配置 | 核心作用 |
| :--- | :--- | :--- |
| `@SpringBootApplication` | `OpenaiApplication.java` | 主启动类：开启自动配置与组件扫描 |
| `@Configuration` | `CorsConfig`, `SecurityConfig` | 配置类：标记为 Spring 配置类，用于定义 Bean |
| `@Bean` | 各类 Config 文件 | Bean 定义：将方法返回值注册为 Spring 容器管理的对象 |


| 注解 | Spring Security 安全控制 | 核心作用 |
| :--- | :--- | :--- |
| `@EnableWebSecurity` | `SecurityConfig.java` | 启用 Spring Security 的 Web 安全功能 |
| `@EnableMethodSecurity` | `SecurityConfig.java` | 启用方法级安全控制（如配合 `@PreAuthorize` 使用） |
| `@Component` | `JwtUtil`, `CustomUserDetailsService` | 通用组件：标记为 Spring 组件，自动注册到 IOC 容器 |

| 注解 | Spring Web MVC 控制器 | 作用说明 |
| :--- | :--- | :--- |
| `@RestController` | 所有 Controller 类 | RESTful 风格控制器，自动将返回值序列化为 JSON |
| `@RequestMapping` | Controller 类级别 | 定义 URL 路径的公共前缀 |
| `@GetMapping` / `@PostMapping` | 具体方法 | 映射 GET / POST 请求 |
| `@PutMapping` / `@PatchMapping` | 具体方法 | 映射 PUT (完整更新) / PATCH (部分更新) 请求 |
| `@DeleteMapping` | 具体方法 | 映射 DELETE 删除请求 |


| 注解 | 依赖注入与参数绑定 | 作用说明 |
| :--- | :--- | :--- |
| `@Autowired` | Service / Controller | 自动装配：按类型自动注入依赖对象 |
| `@Value` | Config / Controller | 属性注入：读取 `application.yml` 中的配置值 |
| `@RequestBody` | 方法参数 | 将 JSON 请求体反序列化为 Java 对象 |
| `@PathVariable` | 方法参数 | 提取 URL 路径中的变量（如 `/users/{id}`） |
| `@RequestParam` | 方法参数 | 提取 URL 查询参数（如 `?page=1`） |
| `@RequestHeader` | 方法参数 | 提取 HTTP 请求头信息 |
| `@Valid` | 方法参数 | 触发 JSR-303 数据校验（配合 DTO 使用） |

Spring Data JPA 数据库映射\
实体与主键\
@Entity / @Table(name="...")：标记 JPA 实体类并指定表名。\
@Id / @GeneratedValue(strategy=IDENTITY)：标识主键并使用数据库自增策略。\
字段与关系\
@Column：定义列属性（名称、长度、是否唯一等）。\
@ManyToOne(fetch=LAZY)：定义多对一关联，采用懒加载策略。\
@JoinColumn：指定关联表的外键列名。\
时间与枚举\
@CreationTimestamp / @UpdateTimestamp：自动记录数据的创建与更新时间。\
@Enumerated(EnumType.STRING)：将枚举类型以字符串形式存储到数据库。\


### Docker 部署
## 环境要求、

 1. 克隆项目\
git clone <https://github.com/DUSKDESU/GDUF_level_3.git> 
cd openai

 2. 一键启动\
docker compose up -d --build

 3. 查看启动日志\
docker compose logs -f app

 服务访问\
应用接口：http://localhost:8081
API 文档：http://localhost:8081/swagger-ui.html
健康检查：http://localhost:8081/actuator/health
MySQL 数据库：localhost:3307（root/默认密码1234）
   





