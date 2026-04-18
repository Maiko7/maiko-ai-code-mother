
# 开发规范指南
为保证代码质量、可维护性、安全性与可扩展性，请在开发过程中严格遵循以下规范。

## 一、项目基础信息

- **作者**：73450
- **用户工作目录**：`E:\Code\Maiko7\AI\maiko-ai-code-mother`
- **构建工具**：Maven
- **开发语言**：Java 21
- **主框架**：Spring Boot 3.5.13

## 二、项目目录结构

本项目采用标准的 Maven 目录结构，并结合 MyBatis-Flex 与 Spring Boot 特定规范。

```text
maiko-ai-code-mother
├── docs                          # 项目文档
├── sql                           # 数据库脚本文件
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com.maiko.maikaiicodemother
│   │   │       ├── ai              # AI 相关逻辑（模型、工具）
│   │   │       ├── annotation      # 自定义注解
│   │   │       ├── aop             # 切面编程
│   │   │       ├── common          # 公共类/常量
│   │   │       ├── config          # 配置类
│   │   │       ├── constant        # 常量定义
│   │   │       ├── controlller     # 控制层 (注意：项目包名为 controlller)
│   │   │       ├── core            # 核心业务逻辑（构建、处理、解析）
│   │   │       ├── exception       # 异常处理
│   │   │       ├── generator       # 代码生成器
│   │   │       ├── langgraph4j     # LangGraph4j 工作流编排
│   │   │       ├── manager         # 管理器组件
│   │   │       ├── mapper          # MyBatis-Flex Mapper 接口
│   │   │       ├── model           # 数据模型
│   │   │       │   ├── dto         # 数据传输对象
│   │   │       │   ├── entity      # 数据库实体
│   │   │       │   ├── enums       # 枚举类
│   │   │       │   └── vo          # 视图对象
│   │   │       ├── serve           # 服务层（如 WebSocket 等）
│   │   │       ├── service         # 业务逻辑接口
│   │   │       │   └── impl        # 业务逻辑实现
│   │   │       ├── task            # 异步任务
│   │   │       └── utils           # 工具类
│   │   └── resources
│   │       ├── mapper              # MyBatis XML 映射文件（如有）
│   │       ├── prompt             # AI 提示词模板
│   │       ├── static             # 静态资源
│   │       └── application.yml     # 配置文件
│   └── test                        # 测试代码
└── tmp                             # 临时文件（部署、输出、截图）
```

## 三、技术栈与核心依赖

| 类别 | 名称/版本 | 说明 |
|------|-----------|------|
| **核心框架** | Spring Boot 3.5.13 | 基础框架 |
| **Java 版本** | JDK 21 | 编译与运行环境 |
| **ORM 框架** | MyBatis-Flex 1.11.0 | 替代 JPA，提供更灵活的数据库操作 |
| **数据库** | MySQL | 使用 HikariCP 连接池 |
| **缓存** | Redis (Spring Session) | 用于 Session 存储与业务缓存 |
| **AI SDK** | LangChain4j 1.1.0 | AI 应用开发核心 |
| **工作流** | LangGraph4j 1.6.0-rc2 | 状态图与工作流编排 |
| **文档工具** | Knife4j 4.4.0 | 接口文档（基于 OpenAPI 3） |
| **工具库** | Hutool 5.8.38 | Java 工具类库 |
| **其他** | Lombok, Selenium, WxJava, COS SDK | 代码简化、网页操作、微信开发、对象存储 |

## 四、分层架构规范

| 层级 | 职责说明 | 开发约束与注意事项 |
|------|----------|---------------------|
| **Controller** | 处理 HTTP 请求与响应，定义 API 接口 | 不得直接访问数据库，必须通过 Service 层调用；统一使用 `controlller` 包名 |
| **Service** | 实现业务逻辑、事务管理与数据校验 | 必须通过 Mapper 层访问数据库；返回 DTO 而非 Entity（除非必要） |
| **Mapper** | 数据库访问与持久化操作 | 继承 `BaseMapper`；优先使用 MyBatis-Flex 的 QueryWrapper 进行查询 |
| **Entity** | 映射数据库表结构 | 不得直接返回给前端（需转换为 DTO）；使用 MyBatis-Flex 注解（如 `@Table`） |

### 接口与实现分离

- 所有 Service 接口实现类需放在 `service.impl` 包中。

## 五、安全与性能规范

### 输入校验

- 使用 `@Valid` 与 JSR-303 校验注解（如 `@NotBlank`, `@Size` 等）。
  - 注意：Spring Boot 3.x 中校验注解位于 `jakarta.validation.constraints.*`。
- 禁止手动拼接 SQL 字符串，防止 SQL 注入攻击（使用 MyBatis-Flex 的条件构造器）。

### 事务管理

- `@Transactional` 注解仅用于 **Service 层**方法。
- 避免在循环中频繁提交事务，影响性能。

### 缓存策略

- 利用 Redis 进行高频数据的缓存，减少数据库压力。
- Session 存储于 Redis，配置超时时间为 30 天（259200 秒）。

## 六、代码风格规范

### 命名规范

| 类型 | 命名方式 | 示例 |
|------|----------|------|
| 类名 | UpperCamelCase | `UserServiceImpl` |
| 方法/变量 | lowerCamelCase | `saveUser()` |
| 常量 | UPPER_SNAKE_CASE | `MAX_LOGIN_ATTEMPTS` |

### 注释规范

- 所有类、方法、字段需添加 **Javadoc** 注释。
- **语言要求**：注释必须使用 **中文**（简体）编写，确保团队成员能够无障碍理解。

### 类型命名规范（阿里巴巴风格）

| 后缀 | 用途说明 | 示例 |
|------|----------|------|
| DTO | 数据传输对象 | `UserDTO` |
| Entity | 数据库实体对象 | `User` |
| VO | 视图展示对象 | `UserVO` |
| Query | 查询参数封装对象 | `UserQuery` |

### 实体类简化工具

- 使用 Lombok 注解替代手动编写 getter/setter/构造方法：
  - `@Data`
  - `@NoArgsConstructor`
  - `@AllArgsConstructor`

## 七、扩展性与日志规范

### 接口优先原则

- 所有业务逻辑通过接口定义（如 `UserService`），具体实现放在 `impl` 包中（如 `UserServiceImpl`）。

### 日志记录

- 使用 `@Slf4j` 注解代替 `System.out.println`。

## 八、编码原则总结

| 原则 | 说明 |
|------|------|
| **SOLID** | 高内聚、低耦合，增强可维护性与可扩展性 |
| **DRY** | 避免重复代码，提高复用性 |
| **KISS** | 保持代码简洁易懂 |
| **YAGNI** | 不实现当前不需要的功能 |
| **OWASP** | 防范常见安全漏洞，如 SQL 注入、XSS 等 |
