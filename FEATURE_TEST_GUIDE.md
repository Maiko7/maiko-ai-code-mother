# 对话轮次统计和导出功能测试指南

## ✅ 已完成的功能

### 任务1：统计应用对话轮次

**实现内容：**
1. ✅ 数据库 `app` 表添加 `totalRounds` 字段（INT类型，默认0）
2. ✅ `App` 实体类添加 `totalRounds` 属性
3. ✅ `AppVO` 添加 `totalRounds` 属性，前端可直接展示
4. ✅ 在 `AppServiceImpl.chatToGenCode()` 方法中，每次AI成功回复后自动+1
5. ✅ 失败不计入轮数，保证数据准确性

**修改的文件：**
- `sql/create_table.sql` - 添加了totalRounds字段定义
- `sql/alter_table_add_total_rounds.sql` - 数据库迁移脚本
- `model/entity/App.java` - 添加totalRounds字段
- `model/vo/AppVO.java` - 添加totalRounds字段
- `service/impl/AppServiceImpl.java` - 实现incrementAppTotalRounds()方法

---

### 任务2：对话历史导出为Markdown

**实现内容：**
1. ✅ 创建 `ChatHistoryExportRequest` DTO，支持时间范围筛选
2. ✅ `ChatHistoryService` 接口添加 `exportChatHistoryToMarkdown()` 方法
3. ✅ 实现Markdown格式化逻辑：
   - 标准Markdown语法
   - 按轮次分组（用户消息 + AI回复 = 1轮）
   - 包含时间戳、应用信息、对话轮数等元数据
   - 美观的分隔线和标题
4. ✅ `ChatHistoryController` 添加 `/export` 接口
5. ✅ 设置响应头实现浏览器直接下载 `.md` 文件
6. ✅ 权限控制：仅应用创建者和管理员可导出

**新增的文件：**
- `model/dto/chathistory/ChatHistoryExportRequest.java` - 导出请求DTO

**修改的文件：**
- `service/ChatHistoryService.java` - 添加导出方法接口
- `service/impl/ChatHistoryServiceImpl.java` - 实现导出逻辑
- `controlller/ChatHistoryController.java` - 添加导出接口

---

## 📋 测试步骤

### 前置准备

#### 1. 执行数据库迁移

```bash
# 连接到MySQL
mysql -u root -p123456

# 执行迁移脚本
source sql/alter_table_add_total_rounds.sql
```

或者手动执行：
```sql
USE maiko_ai_code_mother;
ALTER TABLE app ADD COLUMN totalRounds INT DEFAULT 0 NOT NULL COMMENT '对话总轮数' AFTER userId;
```

验证字段添加成功：
```sql
DESC app;
-- 应该能看到 totalRounds 字段
```

#### 2. 编译项目

```bash
cd e:\Code\Maiko7\AI\maiko-ai-code-mother
mvn clean compile
```

#### 3. 启动项目

```bash
mvn spring-boot:run
```

---

### 测试任务1：对话轮次统计

#### 测试步骤：

1. **创建一个新应用**
   
   使用Swagger或Postman调用：
   ```
   创建应用
   POST http://localhost:8123/api/app/add
   Content-Type: application/json
   
   {
     "initPrompt": "你是一个Java编程助手"
   }
   ```
   
   记录返回的 `appId`。

2. **查看初始轮数**
   
   ```
   获取应用详情
   GET http://localhost:8123/api/app/get/vo?id={appId}
   ```
   
   响应中应该看到：
   ```json
   {
     "code": 0,
     "data": {
       "id": 123456,
       "appName": null,
       "totalRounds": 0,  // 初始为0
       ...
     }
   }
   ```

3. **进行一轮对话**
   
   调用聊天接口（SSE流式）：
   ```
   聊天生成代码
   GET http://localhost:8123/api/app/chat/gen/code?appId={appId}&message=你好，请介绍一下Java
   ```
   
   等待AI回复完成。

4. **再次查看应用信息**
   
   ```
   获取应用详情
   GET http://localhost:8123/api/app/get/vo?id={appId}
   ```
   
   响应中应该看到：
   ```json
   {
     "totalRounds": 1  // 增加到1
   }
   ```

5. **再进行一轮对话**
   
   重复步骤3，发送第二条消息。

6. **验证轮数累加**
   
   ```
   GET http://localhost:8123/api/app/get/vo?id={appId}
   ```
   
   响应中应该看到：
   ```json
   {
     "totalRounds": 2  // 增加到2
   }
   ```

7. **检查数据库**
   
   ```sql
   SELECT id, appName, totalRounds FROM app WHERE id = {appId};
   ```

#### 预期结果：
- ✅ 每次成功对话后，`totalRounds` 自动+1
- ✅ 对话失败时，`totalRounds` 不变
- ✅ 前端可以通过 `get/app/vo` 接口实时看到轮数

---

### 测试任务2：导出对话历史

#### 测试步骤：

##### 测试1：导出全部对话历史

1. **确保应用有对话历史**
   
   先进行几轮对话（至少2-3轮）。

2. **调用导出接口**
   
   使用Postman或curl（注意：这个接口会直接返回文件，不适合用浏览器测试）：
   
   ```bash
   curl -X POST http://localhost:8123/api/chatHistory/export \
     -H "Content-Type: application/json" \
     -H "Cookie: JSESSIONID=your_session_id" \
     -d '{
       "appId": 123456
     }' \
     --output chat_history.md
   ```
   
   或者在Swagger中测试：
   - 访问：http://localhost:8123/api/doc.html
   - 找到 "对话历史管理" -> "导出对话历史"
   - 输入appId，点击执行
   - 浏览器会自动下载文件

3. **检查下载的文件**
   
   打开 `chat_history.md`，内容应该类似：
   
   ```markdown
   # 我的Java助手 - 对话历史
   
   **导出时间**: 2026-04-13 15:30:45
   
   **对话轮数**: 3 轮
   
   ---
   
   ## 第1轮
   
   *2026-04-13 15:20:10*
   
   ### 用户
   
   你好，请介绍一下Java
   
   ### AI
   
   Java是一种广泛使用的面向对象编程语言...
   
   ---
   
   ## 第2轮
   
   *2026-04-13 15:22:30*
   
   ### 用户
   
   Java有什么特点？
   
   ### AI
   
   Java的主要特点包括：跨平台、面向对象...
   
   ---
   
   *共 6 条消息，3 轮对话*
   ```

##### 测试2：按时间范围导出

1. **准备不同时间的对话**
   
   确保应用在不同时间有多轮对话。

2. **指定时间范围导出**
注意时间范围的格式为：`yyyy-MM-ddTHH:mm:ss`这样不行2026-04-13 08:10:29
   ```bash
   curl -X POST http://localhost:8123/api/chatHistory/export \
     -H "Content-Type: application/json" \
     -H "Cookie: JSESSIONID=your_session_id" \
     -d '{
       "appId": 123456,
       "startTime": "2026-04-13T10:00:00",
       "endTime": "2026-04-13T18:00:00"
     }' \
     --output chat_history_filtered.md
   ```

3. **验证过滤结果**
   
   打开文件，确认只包含指定时间范围内的对话。

##### 测试3：权限验证

1. **使用非创建者账号登录**
   
   用另一个用户账号登录（不是应用创建者）。

2. **尝试导出他人的应用**
   
   ```bash
   curl -X POST http://localhost:8123/api/chatHistory/export \
     -H "Content-Type: application/json" \
     -H "Cookie: JSESSIONID=other_user_session_id" \
     -d '{
       "appId": 123456
     }'
   ```

3. **验证返回错误**
   
   应该返回：
   ```json
   {
     "code": 403,
     "message": "无权导出该应用的对话历史"
   }
   ```

##### 测试4：管理员权限

1. **使用管理员账号**
   
   用admin账号登录。

2. **导出任意应用的对话**
   
   应该成功，不受创建者限制。

---

## 🔍 常见问题排查

### 问题1：totalRounds字段不存在

**错误信息**：`Unknown column 'totalRounds' in 'field list'`

**解决方案**：
```sql
-- 检查字段是否存在
DESC app;

-- 如果不存在，执行迁移
ALTER TABLE app ADD COLUMN totalRounds INT DEFAULT 0 NOT NULL COMMENT '对话总轮数' AFTER userId;
```

### 问题2：导出的文件为空

**可能原因**：
1. 应用没有对话历史
2. 时间范围设置错误，没有匹配的对话

**解决方案**：
```sql
-- 检查是否有对话历史
SELECT COUNT(*) FROM chat_history WHERE appId = {appId};

-- 检查时间范围
SELECT createTime FROM chat_history WHERE appId = {appId} ORDER BY createTime;
```

### 问题3：权限错误

**错误信息**：`无权导出该应用的对话历史`

**解决方案**：
- 确认当前登录用户是应用创建者或管理员
- 检查session是否有效

### 问题4：文件名乱码

**解决方案**：
- 确保浏览器支持UTF-8编码
- 检查响应头中的 `filename*=UTF-8''` 格式是否正确

---

## 📊 测试检查清单

### 任务1检查项：
- [ ] 数据库字段添加成功
- [ ] App实体类包含totalRounds
- [ ] AppVO包含totalRounds
- [ ] 初始值为0
- [ ] 每轮对话后+1
- [ ] 对话失败不计数
- [ ] 前端可以正常显示

### 任务2检查项：
- [ ] 导出接口可以正常调用
- [ ] Markdown格式正确
- [ ] 包含标题、时间戳、轮数等信息
- [ ] 对话按轮次分组
- [ ] 时间范围过滤生效
- [ ] 权限控制生效
- [ ] 文件可以直接下载
- [ ] 文件名格式正确
- [ ] 中文内容无乱码

---

## 🎯 API接口总结

### 1. 获取应用信息（包含对话轮数）
```
GET /api/app/get/vo?id={appId}
```

### 2. 导出对话历史
```
POST /api/chatHistory/export
Content-Type: application/json

{
  "appId": 123456,
  "startTime": "2026-04-13T10:00:00",  // 可选
  "endTime": "2026-04-13T18:00:00"      // 可选
}
```

**响应**：直接下载 `.md` 文件

---

## 💡 下一步优化建议

1. **批量导出**：支持一次导出多个应用的对话
2. **导出格式扩展**：支持JSON、CSV等格式
3. **异步导出**：大数据量时使用异步任务
4. **导出进度**：显示导出进度条
5. **定时导出**：支持定时自动导出备份
6. **对话轮数图表**：前端展示对话趋势图

---

祝测试顺利！如有问题随时联系。
