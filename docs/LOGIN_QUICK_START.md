# 快速开始 - 微信登录和手机号登录

## 前置检查清单

在开始之前，请确认：

- [ ] MySQL数据库正在运行
- [ ] Redis正在运行
- [ ] 已备份数据库

## 快速配置步骤

### 1. 执行数据库迁移 (2分钟)

```bash
# 方法1: 使用命令行
mysql -u root -p123456 < sql/alter_table_add_login_fields.sql

# 方法2: 使用MySQL Workbench或其他工具
# 打开 sql/alter_table_add_login_fields.sql 并执行
```

验证迁移成功：
```sql
USE maiko_ai_code_mother;
DESC user;
-- 应该能看到 phone, unionId, openId 三个新字段
```

### 2. 启动Redis (如果还没启动)

```bash
# Docker方式
docker run -d --name redis -p 6379:6379 redis:latest

# 或直接启动已安装的Redis服务
redis-server
```

验证Redis：
```bash
redis-cli ping
# 应返回: PONG
```

### 3. 配置微信公众号 (可选，仅测试微信登录时需要)

编辑 `src/main/resources/application.yml`，修改：

```yaml
wx:
  mp:
    app-id: wx1234567890abcdef    # 替换为你的真实AppID
    secret: abcdef1234567890      # 替换为你的真实AppSecret
```

**如果只是测试手机号登录，可以暂时不配置这部分。**

### 4. 编译并运行项目

```bash
# 清理并编译
mvn clean compile

# 运行项目
mvn spring-boot:run
```

看到以下输出表示启动成功：
```
Started MaikoAiCodeMotherApplication in X.XXX seconds
```

### 5. 测试API接口

#### 测试1: 发送验证码

```bash
curl -X POST http://localhost:8123/api/user/send/code \
  -H "Content-Type: application/json" \
  -d '{"phone":"13800138000"}'
```

预期响应：
```json
{"code":0,"data":true,"message":"ok"}
```

然后查看后端日志，找到验证码：
```
手机号 13800138000 的验证码为: 123456
```

#### 测试2: 手机号登录

使用上一步获取的验证码：

```bash
curl -X POST http://localhost:8123/api/user/login/phone \
  -H "Content-Type: application/json" \
  -d '{"phone":"13800138000","verificationCode":"123456"}'
```

预期响应：
```json
{
  "code": 0,
  "data": {
    "id": 123456789,
    "userAccount": "13800138000",
    "userName": "用户000",
    "phone": "13800138000",
    "userRole": "user",
    ...
  },
  "message": "ok"
}
```

#### 测试3: 查看Swagger文档

浏览器访问：
```
http://localhost:8123/api/doc.html
```

可以在图形界面中测试所有接口。

## 常见问题排查

### 问题1: Redis连接失败

**检查**:
```bash
# Redis是否在运行
redis-cli ping

# 端口是否正确
netstat -an | findstr 6379
```

**解决**:
```bash
# 启动Redis
redis-server

# 或者Docker方式
docker start redis
```

### 问题2: 数据库迁移失败

**错误**: "Duplicate column name 'phone'"

**原因**: 字段已经存在

**解决**: 这说明你已经执行过迁移了，可以跳过此步骤。

### 问题3: 微信登录报错

**错误**: "invalid appid" 或 "invalid secret"

**解决**:
1. 检查 `application.yml` 中的配置是否正确
2. 确保公众号已认证
3. 确保IP白名单已配置（微信公众平台 -> 基本配置）

## API接口总览

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 用户注册 | POST | /api/user/register | 账号密码注册 |
| 用户登录 | POST | /api/user/login | 账号密码登录 |
| 微信登录 | POST | /api/user/login/wx | 微信授权登录 |
| 手机登录 | POST | /api/user/login/phone | 手机号验证码登录 |
| 发送验证码 | POST | /api/user/send/code | 发送手机验证码 |
| 获取当前用户 | GET | /api/user/get/login | 获取登录用户信息 |
| 用户注销 | POST | /api/user/logout | 退出登录 |

## 下一步

1. **集成短信服务**: 参考 `LOGIN_FEATURE_GUIDE.md` 中的生产环境建议
2. **前端对接**: 将API接口集成到你的前端项目
3. **微信授权**: 配置微信公众号的OAuth2.0回调域名
4. **安全加固**: 添加图形验证码、限流等安全措施

## 需要帮助？

如果遇到无法解决的问题：

1. 查看后端日志输出
2. 检查数据库是否正确迁移
3. 确认Redis和MySQL都在运行
4. 阅读 `LOGIN_FEATURE_GUIDE.md` 获取详细说明

祝使用愉快！
