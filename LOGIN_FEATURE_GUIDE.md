# 微信登录和手机号验证码登录功能实现指南

## 已完成的功能

### 1. 数据库变更
- 已更新 `sql/create_table.sql`，添加了以下字段：
  - `phone` - 手机号
  - `unionId` - 微信UnionID
  - `openId` - 微信OpenID
- 已创建迁移脚本 `sql/alter_table_add_login_fields.sql`

### 2. 新增的文件

#### DTO类
- `WxLoginRequest.java` - 微信登录请求
- `PhoneLoginRequest.java` - 手机号登录请求
- `SendCodeRequest.java` - 发送验证码请求

#### 配置类
- `RedisConfig.java` - Redis配置
- `WxMpConfiguration.java` - 微信公众号配置

### 3. 修改的文件
- `User.java` - 添加了phone、unionId、openId字段
- `UserService.java` - 添加了三个新方法接口
- `UserServiceImpl.java` - 实现了微信登录、发送验证码、手机号登录逻辑
- `UserController.java` - 添加了三个新的API接口
- `pom.xml` - 添加了Redis和微信SDK依赖
- `application.yml` - 添加了Redis和微信配置

## 需要人工完成的步骤

### 步骤1: 执行数据库迁移

**重要**: 在执行迁移脚本前，请先备份你的数据库！

```bash
# 连接到MySQL
mysql -u root -p

# 执行迁移脚本
source sql/alter_table_add_login_fields.sql
```

或者手动在MySQL客户端执行 `sql/alter_table_add_login_fields.sql` 文件中的SQL语句。



### 步骤3: 配置Redis

确保Redis已经安装并运行：

```bash
# Windows (使用Docker)
docker run -d --name redis -p 6379:6379 redis:latest

# 或者直接下载安装Redis for Windows
# https://github.com/microsoftarchive/redis/releases
```

验证Redis是否运行：
```bash
redis-cli ping
# 应该返回 PONG
```

### 步骤4: 配置微信公众号

1. **申请微信公众号账号**
   - 访问: https://mp.weixin.qq.com/
   - 注册并认证公众号

2. **获取AppID和AppSecret**
   - 登录微信公众平台
   - 进入 "开发" -> "基本配置"
   - 复制 AppID 和 AppSecret

3. **配置回调域名**
   - 在微信公众平台设置OAuth2.0回调域名
   - 例如: `yourdomain.com`

4. **更新配置文件**
   修改 `src/main/resources/application.yml`:
   ```yaml
   wx:
     mp:
       app-id: your_actual_appid_here        # 替换为你的AppID
       secret: your_actual_secret_here       # 替换为你的AppSecret
   ```

### 步骤5: 编译和运行

```bash
# 清理并编译
mvn clean compile

# 运行项目
mvn spring-boot:run
```

## API接口说明

### 1. 微信登录接口

**接口地址**: `POST /api/user/login/wx`

**请求体**:
```json
{
  "code": "微信授权code"
}
```

**响应**:
```json
{
  "code": 0,
  "data": {
    "id": 123456,
    "userAccount": "wx_昵称",
    "userName": "微信昵称",
    "userAvatar": "头像URL",
    "userRole": "user",
    "unionId": "xxx",
    "openId": "xxx"
  },
  "message": "ok"
}
```

**前端获取微信code示例**:
```javascript
// 重定向到微信授权页面
const appId = 'YOUR_APP_ID';
const redirectUri = encodeURIComponent('http://yourdomain.com/callback');
const scope = 'snsapi_userinfo'; // 或 snsapi_base
window.location.href = `https://open.weixin.qq.com/connect/oauth2/authorize?appid=${appId}&redirect_uri=${redirectUri}&response_type=code&scope=${scope}&state=STATE#wechat_redirect`;

// 在回调页面获取code
const urlParams = new URLSearchParams(window.location.search);
const code = urlParams.get('code');
```

### 2. 发送验证码接口

**接口地址**: `POST /api/user/send/code`

**请求体**:
```json
{
  "phone": "13800138000"
}
```

**响应**:
```json
{
  "code": 0,
  "data": true,
  "message": "ok"
}
```

**注意**: 当前是测试模式，验证码会打印在后端日志中。生产环境需要集成短信服务（阿里云、腾讯云等）。

### 3. 手机号验证码登录接口

**接口地址**: `POST /api/user/login/phone`

**请求体**:
```json
{
  "phone": "13800138000",
  "verificationCode": "123456"
}
```

**响应**:
```json
{
  "code": 0,
  "data": {
    "id": 123456,
    "userAccount": "13800138000",
    "userName": "用户000",
    "phone": "13800138000",
    "userRole": "user"
  },
  "message": "ok"
}
```

## 测试流程

### 测试手机号登录

1. **发送验证码**:
```bash
curl -X POST http://localhost:8123/api/user/send/code \
  -H "Content-Type: application/json" \
  -d '{"phone":"13800138000"}'
```

2. **查看日志获取验证码**:
   在后端日志中找到类似这样的输出：
   ```
   手机号 13800138000 的验证码为: 123456
   【测试模式】验证码已生成，请查看日志。生产环境需要集成短信服务。
   ```

3. **使用验证码登录**:
```bash
curl -X POST http://localhost:8123/api/user/login/phone \
  -H "Content-Type: application/json" \
  -d '{"phone":"13800138000","verificationCode":"123456"}'
```

### 测试微信登录

1. 确保已正确配置微信公众号AppID和AppSecret
2. 前端引导用户授权获取code
3. 调用登录接口:
```bash
curl -X POST http://localhost:8123/api/user/login/wx \
  -H "Content-Type: application/json" \
  -d '{"code":"071xxx..."}'
```

## 生产环境建议

### 1. 集成短信服务

当前验证码仅在日志中显示，生产环境需要集成真实的短信服务：

**阿里云短信示例** (需要在UserServiceImpl中替换):
```java
// 在sendVerificationCode方法中替换日志输出部分
DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
IAcsClient client = new DefaultAcsClient(profile);

CommonRequest request = new CommonRequest();
request.setSysMethod(MethodType.POST);
request.setSysDomain("dysmsapi.aliyuncs.com");
request.setSysVersion("2017-05-25");
request.setSysAction("SendSms");
request.putQueryParameter("RegionId", "cn-hangzhou");
request.putQueryParameter("PhoneNumbers", phone);
request.putQueryParameter("SignName", "你的签名");
request.putQueryParameter("TemplateCode", "SMS_XXXXX");
request.putQueryParameter("TemplateParam", "{\"code\":\"" + code + "\"}");

CommonResponse response = client.getCommonResponse(request);
```

### 2. 密码加密升级

当前使用MD5+Salt，建议升级为BCrypt：
```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
String encryptedPassword = encoder.encode(rawPassword);
boolean matches = encoder.matches(rawPassword, encryptedPassword);
```

### 3. Session改为JWT (可选)

如果需要无状态认证，可以将Session改为JWT token。

### 4. 添加限流和防刷

对发送验证码接口添加更严格的限流策略。

## 常见问题

### Q1: 编译时报错 "不支持发行版本 21"
**A**: 你的Java版本不是21。要么安装Java 21，要么将pom.xml中的java.version改为17。

### Q2: Redis连接失败
**A**: 确保Redis服务正在运行，并且application.yml中的配置正确。

### Q3: 微信登录报错 "invalid code"
**A**: 微信code只能使用一次，且有效期很短（约5分钟）。确保code是最新的。

### Q4: 手机号格式验证失败
**A**: 当前只支持中国大陆手机号（1开头的11位数字）。

## 下一步优化建议

1. 添加绑定手机号功能（已登录用户可以绑定手机号）
2. 添加解绑微信功能
3. 添加账号合并功能（同一用户多个登录方式合并）
4. 添加登录日志记录
5. 添加异常登录检测
6. 集成图形验证码防止机器刷验证码

## 联系支持

如有问题，请检查：
1. 后端日志输出
2. 数据库是否正确迁移
3. Redis是否正常运行
4. 微信公众号配置是否正确
