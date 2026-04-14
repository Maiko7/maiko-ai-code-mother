# Maiko AI Code Mother

> 一个基于 AI 大模型的**零代码前端应用生成平台**后端服务。用户只需用自然语言描述需求，即可通过 AI 流式生成 HTML/CSS/JS 前端代码，并一键部署到服务器上访问。

---

## 项目简介

本项目是一个 AI 驱动的前端代码生成平台，核心功能是让用户通过对话描述需求，AI 实时生成完整的前端应用（支持单 HTML 文件和 HTML+CSS+JS 三文件模式），并提供应用部署、对话历史记录、智能记忆管理等完整功能。

**作者**：代码卡壳Maiko7

---

## 技术栈

| 分类 | 技术 | 版本 |
|---|---|---|
| 核心框架 | Spring Boot | 3.5.13 |
| Java 版本 | Java | 21 |
| ORM | MyBatis-Flex | 1.11.0 |
| 数据库 | MySQL | 8.x |
| 缓存 | Redis + Caffeine | — |
| AI 框架 | LangChain4j | 1.1.0 |
| AI 模型 | DeepSeek Chat | — |
| 流式输出 | LangChain4j Reactor (SSE) | 1.1.0-beta7 |
| AI 记忆存储 | LangChain4j Community Redis | 1.1.0-beta7 |
| Session 管理 | Spring Session Data Redis | — |
| 微信登录 | WxJava (weixin-java-mp) | 4.6.0 |
| API 文档 | Knife4j (OpenAPI 3) | 4.4.0 |
| 工具库 | Hutool | 5.8.38 |

---

## 核心功能

### 1. AI 代码生成（流式 SSE）
- 用户通过自然语言描述需求，AI 实时流式生成前端代码
- 支持两种生成模式：
  - **HTML 模式**：生成单个完整的 `.html` 文件（CSS + JS 内联）
  - **多文件模式**：生成 `index.html` + `style.css` + `script.js` 三个独立文件
- 使用 Server-Sent Events（SSE）实现打字机效果的实时输出

### 2. 应用管理
- 创建、更新、删除应用
- 应用支持自定义 `initPrompt`（系统提示词），为 AI 设定专属角色
- 精选应用功能（`priority=99` 为精选应用）
- 记录每个应用的对话总轮数

### 3. 应用部署
- 一键将生成的代码部署到服务器，生成可访问的 URL
- 通过唯一 `deployKey` 标识应用，提供静态文件访问服务

### 4. 对话历史
- 自动保存每轮对话（用户消息 + AI 回复）
- 支持游标分页查询历史记录
- 支持导出为 Markdown 文件（含时间范围筛选）

### 5. 智能记忆管理
- 基于 Redis 持久化 AI 对话记忆，保障上下文连贯性
- **智能总结**：当未总结对话达到 10 轮时，自动触发 AI 异步生成「会议纪要式」总结，压缩历史上下文，有效降低 Token 消耗
- 每个应用拥有独立、隔离的对话记忆

### 6. 用户体系
- **账号密码注册/登录**（MD5+Salt 加密）
- **手机号验证码登录**（Redis 存储验证码，60s 发送频率限制）
- **微信公众号 OAuth2.0 登录**
- Session 持久化存储于 Redis，有效期 30 天
- 多角色权限体系：`user` / `admin` / `vip` / `editor` / `partner` / `ban`
- 基于 AOP 的 `@AuthCheck` 注解权限拦截

---

## 项目结构

```
src/main/java/com/maiko/maikoaicodemother/
├── ai/                        # AI 服务层
│   ├── AiCodeGeneratorService.java        # AI 代码生成接口（LangChain4j）
│   ├── AiCodeGeneratorServiceFactory.java # AI 服务工厂（含 Caffeine 缓存）
│   └── ChatSummaryAiService.java          # 对话总结 AI 接口
├── aop/                       # AOP 切面
│   └── MultiRoleAuthInterceptor.java      # 多角色权限拦截器
├── config/                    # 配置类
│   ├── CorsConfig.java                    # 跨域配置
│   ├── RedisConfig.java                   # Redis 序列化配置
│   ├── RedisChatMemoryStoreConfig.java    # AI 记忆 Redis 存储配置
│   └── WxMpConfiguration.java            # 微信公众号配置
├── controlller/               # 控制层
│   ├── AppController.java                 # 应用管理接口
│   ├── ChatHistoryController.java         # 对话历史接口
│   ├── UserController.java                # 用户管理接口
│   └── StaticResourceController.java     # 静态资源访问（已部署应用）
├── core/                      # 核心业务
│   ├── AiCodeGeneratorFacade.java         # 门面模式：统一代码生成入口
│   ├── parser/                            # 策略模式：代码解析器
│   └── saver/                             # 模板方法模式：代码文件保存器
├── model/                     # 数据模型
│   ├── entity/                            # 实体类
│   ├── dto/                               # 请求 DTO
│   ├── vo/                                # 响应 VO
│   └── enums/                             # 枚举类
├── service/                   # 服务接口 + 实现
└── mapper/                    # MyBatis-Flex Mapper
```

---

## 数据库设计

### 快速初始化

执行 `sql/create_table.sql` 完成建库建表：

```sql
create database if not exists maiko_ai_code_mother;
```

### 核心表说明

| 表名 | 说明 |
|---|---|
| `user` | 用户表（支持账号密码/手机/微信三种登录方式） |
| `app` | 应用表（含 initPrompt、部署信息、对话轮数等） |
| `chat_history` | 对话历史表（用户消息 + AI 回复，含总结状态） |
| `chat_summary` | 对话总结表（AI 自动生成的会议纪要式摘要） |

---

## 快速开始

### 1. 环境准备

| 工具 | 版本要求 |
|---|---|
| JDK | 21+ |
| MySQL | 8.x |
| Redis | 6.x+ |
| Maven | 3.6+ |

### 2. 初始化数据库

```bash
mysql -u root -p < sql/create_table.sql
```

### 3. 配置文件

修改 `src/main/resources/application.yml` 中的数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/maiko_ai_code_mother
    username: root
    password: 你的密码
  data:
    redis:
      host: localhost
      port: 6379
```

创建 `src/main/resources/application-local.yml`（已在 `.gitignore` 中排除，**不要提交到 Git**）并填写 AI 配置：

```yaml
langchain4j:
  open-ai:
    chat-model:
      base-url: https://api.deepseek.com   # DeepSeek 或其他 OpenAI 兼容接口
      api-key: sk-xxxxxxxxxxxxxxxxxxxx
      model-name: deepseek-chat
      max-tokens: 8192
      strict-json-schema: true
      response-format: json_object
    streaming-chat-model:
      base-url: https://api.deepseek.com
      api-key: sk-xxxxxxxxxxxxxxxxxxxx
      model-name: deepseek-chat
      max-tokens: 8192
```

> **注意**：`application-local.yml` 中包含 AI API Key 等敏感信息，已配置 `.gitignore` 防止提交，请勿上传到公开仓库。

### 4. 配置微信公众号（可选）

若需使用微信登录，修改 `application.yml` 中的微信配置：

```yaml
wx:
  mp:
    app-id: 你的微信公众号AppID
    secret: 你的微信公众号AppSecret
```

### 5. 启动项目

```bash
mvn clean spring-boot:run
```

服务启动后访问：
- 服务地址：`http://localhost:8123/api`
- API 文档：`http://localhost:8123/api/doc.html`

---

## API 接口概览

接口前缀：`/api`（Context-Path）

### 用户模块 `/user`

| 方法 | 路径 | 说明 | 权限 |
|---|---|---|---|
| POST | `/user/register` | 账号密码注册 | 无 |
| POST | `/user/login` | 账号密码登录 | 无 |
| GET | `/user/get/login` | 获取当前登录用户 | 登录 |
| POST | `/user/logout` | 退出登录 | 登录 |
| POST | `/user/send/code` | 发送手机验证码 | 无 |
| POST | `/user/login/phone` | 手机号验证码登录 | 无 |
| POST | `/user/login/wx` | 微信公众号登录 | 无 |
| POST | `/user/add` | 创建用户 | Admin |
| POST | `/user/delete` | 删除用户 | Admin |
| POST | `/user/update` | 更新用户 | Admin |
| GET | `/user/get` | 根据 ID 获取用户 | Admin |
| POST | `/user/list/page/vo` | 分页获取用户列表 | Admin |

### 应用模块 `/app`

| 方法 | 路径 | 说明 | 权限 |
|---|---|---|---|
| GET | `/app/chat/gen/code` | AI 流式生成代码（SSE） | 登录 |
| POST | `/app/deploy` | 部署应用 | 登录 |
| POST | `/app/add` | 创建应用 | 登录 |
| POST | `/app/update` | 更新应用（本人） | 登录 |
| POST | `/app/delete` | 删除应用（本人/Admin） | 登录 |
| GET | `/app/get/vo` | 获取应用详情 | 无 |
| POST | `/app/my/list/page/vo` | 获取我的应用列表 | 登录 |
| POST | `/app/good/list/page/vo` | 获取精选应用列表 | 无 |
| POST | `/app/admin/update` | 管理员更新应用 | Admin |
| POST | `/app/admin/delete` | 管理员删除应用 | Admin |
| POST | `/app/admin/list/page/vo` | 管理员查询应用列表 | Admin |
| GET | `/app/admin/get/vo` | 管理员获取应用详情 | Admin |

### 对话历史模块 `/chatHistory`

| 方法 | 路径 | 说明 | 权限 |
|---|---|---|---|
| GET | `/chatHistory/app/{appId}` | 游标分页查询对话历史 | 登录 |
| POST | `/chatHistory/export` | 导出对话历史为 Markdown | 登录（创建者/Admin） |
| POST | `/chatHistory/admin/list/page/vo` | 管理员分页查询全部记录 | Admin |

### 其他

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/health/` | 健康检查 |
| GET | `/static/{deployKey}/**` | 访问已部署的应用静态资源 |

---

## 关键设计说明

### 门面模式（Facade）
`AiCodeGeneratorFacade` 作为门面，统一封装了「调用 AI → 解析代码 → 保存文件」的完整流程，调用方只需传入用户消息和生成类型，无需关心底层细节。

### 策略模式（Strategy）
`CodeParserExecutor` + `CodeParser` 接口，针对 HTML 模式和多文件模式分别实现不同的解析策略，支持灵活扩展新的生成类型。

### 模板方法模式（Template Method）
`CodeFileSaverTemplate` 定义「构建目录 → 保存文件 → 返回目录」的标准流程，`HtmlCodeFileSaverTemplate` 和 `MultiFileCodeFileSaverTemplate` 作为子类只需实现具体的文件写入逻辑。

### 智能记忆管理
- 每个应用（`appId`）拥有独立的 `MessageWindowChatMemory`，存储在 Redis 中，最多保留 20 条最近消息。
- 当未总结对话达到 **10 轮**时，异步触发 AI 生成总结，总结以「会议纪要」格式存入 `chat_summary` 表，并将已总结的早期对话标记压缩，保留最近 **5 轮**完整对话。
- 下次加载记忆时，会先注入最新总结作为上下文，再追加最近的未总结消息，兼顾上下文完整性与 Token 效率。

---

## SQL 文件说明

| 文件 | 说明 |
|---|---|
| `sql/create_table.sql` | 全量建库建表脚本（含初始测试数据） |
| `sql/alter_table_add_login_fields.sql` | 旧库迁移：user 表添加手机/微信字段 |
| `sql/alter_table_add_total_rounds.sql` | 旧库迁移：app 表添加 totalRounds 字段 |
| `sql/alter_table_add_chat_summary.sql` | 旧库迁移：添加 chat_history 总结字段 + 创建 chat_summary 表 |

> **新建项目**只需执行 `create_table.sql`；已有旧库则按需执行对应 `alter_table_*.sql` 迁移脚本。

---

## 默认账号

执行 `create_table.sql` 后会自动插入以下测试账号（密码未加密，仅作示例，生产环境请修改）：

| 账号 | 密码 | 角色 |
|---|---|---|
| admin | 123456 | admin |
| huge | 123456 | user |
| maiko | 123456 | user |

---

## 注意事项

1. **API Key 安全**：所有敏感配置（AI API Key、微信 Secret）请放在 `application-local.yml` 中，该文件已被 `.gitignore` 忽略，严禁提交到公开仓库。
2. **短信服务**：手机验证码功能当前为**测试模式**，验证码会打印在后端日志中。生产环境需集成阿里云/腾讯云短信服务。
3. **部署路径**：生成的代码默认输出到项目根目录下的 `tmp/code_output/`，部署后复制到 `tmp/code_deploy/`。
4. **流式接口**：`/app/chat/gen/code` 为 SSE 流式接口，Swagger 文档中无法直接测试，建议使用浏览器或 Postman 的 SSE 模式调试。

---

## License

本项目仅用于学习交流，请勿用于商业用途。
