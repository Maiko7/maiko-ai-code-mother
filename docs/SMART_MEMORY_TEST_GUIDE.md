# 智能记忆管理功能测试指南

## ✅ 已完成的功能

### 📊 功能概述

实现了基于AI的智能对话总结功能，当对话轮数超过10轮时，自动使用AI对历史对话进行"会议纪要"式总结，有效节省Token并优化记忆效果。

### 🎯 核心特性

1. **自动触发**: 每10轮对话自动触发一次AI总结
2. **智能压缩**: AI提取关键要点，忽略无关细节
3. **混合记忆**: 保留最新5轮完整对话 + 之前对话的AI摘要
4. **Token优化**: 将上下文控制在6k tokens以内（预留2k给新对话）
5. **异步执行**: 总结过程不阻塞用户对话，提升体验
6. **持久化存储**: 总结记录保存到数据库，支持追溯

---

## 📁 新增/修改的文件清单

### 新增文件：

#### 数据库相关
- `sql/alter_table_add_chat_summary.sql` - 数据库迁移脚本
- `model/entity/ChatSummary.java` - 对话总结实体类
- `mapper/ChatSummaryMapper.java` - 总结Mapper接口

#### AI服务相关
- `ai/ChatSummaryAiService.java` - AI总结服务接口
- `prompt/chat-summary-system-prompt.txt` - AI总结用的System Prompt模板

#### 业务逻辑相关
- `service/ChatSummaryService.java` - 总结服务接口
- `service/impl/ChatSummaryServiceImpl.java` - 总结服务实现类

### 修改文件：

- `sql/create_table.sql` - 添加chat_summary表定义和chat_history新字段
- `model/entity/ChatHistory.java` - 添加isSummarized和summaryId字段
- `service/impl/ChatHistoryServiceImpl.java` - 优化loadChatHistoryToMemory支持加载摘要
- `service/impl/AppServiceImpl.java` - 集成自动总结检查逻辑

---

## 📋 测试步骤

### 🔴 前置准备

#### 1. 执行数据库迁移

```bash
# 方式1：使用命令行
mysql -u root -p123456 < sql/alter_table_add_chat_summary.sql

# 方式2：手动执行SQL
USE maiko_ai_code_mother;

-- 为chat_history表添加总结相关字段
ALTER TABLE chat_history
ADD COLUMN isSummarized TINYINT DEFAULT 0 NOT NULL COMMENT '是否已被总结压缩' AFTER userId,
ADD COLUMN summaryId BIGINT NULL COMMENT '所属的总结记录ID' AFTER isSummarized;

-- 创建对话总结表
CREATE TABLE IF NOT EXISTS chat_summary (
    id BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
    appId BIGINT NOT NULL COMMENT '应用id',
    summaryContent TEXT NOT NULL COMMENT '总结内容',
    summarizedRounds INT NOT NULL COMMENT '本次总结涵盖的对话轮数',
    startRoundId BIGINT NOT NULL COMMENT '起始对话ID',
    endRoundId BIGINT NOT NULL COMMENT '结束对话ID',
    userId BIGINT NOT NULL COMMENT '执行总结的用户id',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    INDEX idx_appId (appId),
    INDEX idx_createTime (createTime)
);
```

验证迁移成功：
```sql
DESC chat_history;  -- 应该看到 isSummarized 和 summaryId 字段
DESC chat_summary;  -- 应该看到完整的总结表结构
```

#### 2. 编译并启动项目

```bash
cd e:\Code\Maiko7\AI\maiko-ai-code-mother
mvn clean compile
mvn spring-boot:run
```

#### 3. 准备测试数据

创建一个新应用或选择现有应用，确保可以进行多轮对话。

---

### 🧪 测试场景1：验证自动总结触发

#### 测试目标
确认在第10轮对话完成后，系统自动触发AI总结。

#### 测试步骤

**步骤1: 查看初始状态**

在数据库中查询应用的当前状态：
```sql
SELECT id, appName, totalRounds FROM app WHERE id = {your_app_id};
```

**步骤2: 进行10轮对话**

使用Swagger或前端界面，连续发送10条不同的消息：

```
第1轮: "帮我创建一个登录页面"
第2轮: "改成深色主题"
第3轮: "添加邮箱验证"
第4轮: "增加记住我功能"
第5轮: "美化一下按钮样式"
第6轮: "添加响应式设计"
第7轮: "优化移动端显示"
第8轮: "增加动画效果"
第9轮: "添加错误提示"
第10轮: "完善表单验证规则"
```

**步骤3: 观察后端日志**

在第10轮对话完成后，查看后端日志输出：

预期日志：
```
应用 {appId} 达到总结阈值，开始异步执行智能总结
开始对应用 {appId} 的 20 条对话进行AI总结
为 appId: {appId} 加载了最新总结，涵盖 10 轮对话
应用 {appId} 智能总结完成，总结ID: {summaryId}
```

**步骤4: 验证数据库记录**

```sql
-- 1. 检查总结记录是否生成
SELECT * FROM chat_summary WHERE appId = {your_app_id} ORDER BY createTime DESC LIMIT 1;

-- 应该看到一条新的总结记录，包含：
-- - summaryContent: AI生成的总结内容
-- - summarizedRounds: 10（涵盖10轮）
-- - startRoundId, endRoundId: 起始和结束对话ID

-- 2. 检查对话历史的标记状态
SELECT id, messageType, isSummarized, summaryId 
FROM chat_history 
WHERE appId = {your_app_id} 
ORDER BY createTime;

-- 应该看到：
-- - 前10轮对话（20条消息）中，较早的5轮被标记为 isSummarized=1
-- - 最近5轮对话保持 isSummarized=0（未被总结）
```

**步骤5: 验证总结内容质量**

查看 `summaryContent` 字段的内容，应该类似：

```markdown
【对话总结】

**核心主题**：创建和完善登录页面功能

**关键要点**：
- 需要响应式设计的登录页面
- 采用深色主题风格
- 包含邮箱验证功能
- 实现"记住我"功能
- 优化移动端显示效果
- 添加动画效果和错误提示
- 完善表单验证规则

**技术栈/工具**：HTML、CSS、原生JavaScript

**待处理事项**：等待AI生成完整的登录页面代码

**用户偏好**：偏好深色主题、响应式设计、注重用户体验
```

---

### 🧪 测试场景2：验证总结后的记忆加载

#### 测试目标
确认AI在后续对话中能正确加载和使用历史总结。

#### 测试步骤

**步骤1: 进行第11轮对话**

发送一条与之前相关的消息：
```
"根据之前的讨论，现在可以生成完整的代码了吗？"
```

**步骤2: 检查AI的上下文理解**

观察AI的回复，应该能正确引用之前的对话内容，例如：
- 提到深色主题
- 提到邮箱验证
- 提到响应式设计

这说明AI成功加载了历史总结。

**步骤3: 验证记忆加载日志**

查看后端日志：
```
为 appId: {appId} 加载了最新总结，涵盖 10 轮对话
成功为 appId: {appId} 加载了 11 条历史对话（含总结）
```

这表明：
- 1条总结消息
- 10条最近的完整对话（5轮 × 2）
- 总计11条消息被加载到记忆中

---

### 🧪 测试场景3：验证多次总结

#### 测试目标
确认系统在达到20轮、30轮时会继续触发总结。

#### 测试步骤

**步骤1: 继续进行对话直到20轮**

再发送10条消息（第11-20轮）。

**步骤2: 验证第二次总结触发**

查看后端日志，应该看到第二次总结被触发。

**步骤3: 检查总结记录**

```sql
SELECT id, summarizedRounds, createTime 
FROM chat_summary 
WHERE appId = {your_app_id} 
ORDER BY createTime;

-- 应该看到2条总结记录：
-- 第1条：summarizedRounds=10（第1-10轮）
-- 第2条：summarizedRounds=10（第11-20轮）
```

**步骤4: 验证记忆结构**

此时的记忆结构应该是：
```
[第2次总结] + [最近5轮完整对话(15-20轮)]
```

第1次总结被替换为第2次总结，只保留最新的总结。

---

### 🧪 测试场景4：性能测试

#### 测试目标
验证总结功能不会明显影响对话响应速度。

#### 测试步骤

**步骤1: 测量对话响应时间**

在第9轮和第10轮对话时，分别记录：
- 用户发送消息的时间
- AI开始回复的时间
- AI完成回复的时间

**步骤2: 验证异步执行**

第10轮对话应该快速返回（因为总结是异步执行的）。

预期行为：
- 用户消息立即得到AI回复
- 总结在后台执行（日志中稍后出现）
- 不阻塞用户继续发送第11条消息

---

### 🧪 测试场景5：边界情况测试

#### 测试5.1: 空对话总结

```sql
-- 尝试对没有对话的应用触发总结
-- 应该不会报错，只是返回null
```

#### 测试5.2: 少于10轮的对话

进行5轮对话后，验证：
- 不会触发总结
- 日志中没有总结相关的输出

#### 测试5.3: 总结失败的处理

模拟AI调用失败（如网络断开），验证：
- 不影响正常的对话流程
- 日志中记录错误但不抛出异常
- 下次达到阈值时会重试

---

## 🔍 验证检查清单

### 数据库验证
- [ ] `chat_summary` 表创建成功
- [ ] `chat_history` 表添加了 `isSummarized` 和 `summaryId` 字段
- [ ] 第10轮对话后生成了总结记录
- [ ] 总结记录的 `summaryContent` 非空且格式正确
- [ ] 较早的对话被标记为 `isSummarized=1`
- [ ] 最近5轮对话保持 `isSummarized=0`

### 功能验证
- [ ] 第10轮对话后自动触发总结
- [ ] 总结过程不阻塞用户对话（异步执行）
- [ ] AI在后续对话中能引用历史总结
- [ ] 多次总结时只保留最新总结
- [ ] 总结内容格式符合"会议纪要"风格

### 日志验证
- [ ] 达到阈值时输出"达到总结阈值"日志
- [ ] AI总结开始时输出"开始AI总结"日志
- [ ] 总结完成后输出"智能总结完成"日志
- [ ] 加载记忆时输出"加载了最新总结"日志

### 性能验证
- [ ] 总结不影響对话响应速度
- [ ] 异步执行正常工作
- [ ] 无内存泄漏或资源浪费

---

## 📊 预期的Token节省效果

### 未使用总结的情况

假设每轮对话平均600 tokens：
- 20轮对话 = 12,000 tokens
- **超出8k限制！**

### 使用总结后的情况

- 1个总结 ≈ 500 tokens
- 最近5轮完整对话 = 3,000 tokens
- **总计 ≈ 3,500 tokens**
- **节省约70%的Token！**

---

## ⚠️ 常见问题排查

### 问题1: 总结没有自动触发

**可能原因**:
1. 对话轮数未达到10轮
2. 数据库字段未添加成功
3. 代码未重新编译

**解决方案**:
```sql
-- 检查当前未总结的轮数
SELECT COUNT(*) / 2 as unsummarized_rounds
FROM chat_history
WHERE appId = {appId} AND isSummarized = 0;

-- 应该 >= 10 才会触发
```

### 问题2: 总结内容为空或质量差

**可能原因**:
1. DeepSeek API调用失败
2. Prompt配置不正确
3. 对话历史格式有问题

**解决方案**:
```sql
-- 查看总结内容
SELECT summaryContent FROM chat_summary WHERE id = {summaryId};

-- 检查是否为空或包含错误信息
```

### 问题3: AI没有使用历史总结

**可能原因**:
1. 总结未正确加载到记忆中
2. MessageWindowChatMemory配置问题

**解决方案**:
查看日志确认：
```
为 appId: {appId} 加载了最新总结
```

如果没有这条日志，说明总结加载失败。

### 问题4: 总结执行太慢

**可能原因**:
1. AI响应速度慢
2. 网络延迟

**解决方案**:
总结是异步执行的，不会影响用户体验。如果确实很慢，可以：
- 检查网络连接
- 考虑降低总结频率（改为15轮触发）

---

## 💡 优化建议

### 1. 调整总结触发阈值

如果10轮太频繁，可以修改 `ChatSummaryServiceImpl`:
```java
private static final int SUMMARY_TRIGGER_ROUNDS = 15;  // 改为15轮
```

### 2. 调整保留的完整对话轮数

如果需要更多上下文，可以修改:
```java
private static final int KEEP_RECENT_ROUNDS = 7;  // 保留7轮
```

### 3. 手动触发总结

可以添加一个接口让用户手动触发总结：
```java
@PostMapping("/summary/trigger")
public BaseResponse<Long> triggerSummary(@RequestParam Long appId, HttpServletRequest request) {
    User loginUser = userService.getLoginUser(request);
    Long summaryId = chatSummaryService.summarizeChatHistory(appId, 10, loginUser);
    return ResultUtils.success(summaryId);
}
```

### 4. 查看总结历史

添加接口让用户查看历史总结：
```java
@GetMapping("/summary/list")
public BaseResponse<List<ChatSummary>> listSummaries(@RequestParam Long appId) {
    // 查询该应用的所有总结记录
}
```

---

## 🎯 下一步优化方向

1. **Token精确计算**: 使用tiktoken实时计算token数，而不是按轮数
2. **多层摘要**: 保留多个历史摘要，形成分层记忆
3. **摘要检索**: 根据当前对话内容，动态检索相关的历史摘要
4. **用户控制**: 允许用户设置是否启用自动总结、总结频率等
5. **总结优化**: 让AI评估总结质量，不满意时重新总结

---

## 📝 总结

智能记忆管理功能已成功实现，主要特性：

✅ 每10轮对话自动触发AI总结  
✅ 生成"会议纪要"式的高质量摘要  
✅ 保留最近5轮完整对话 + 历史摘要  
✅ 异步执行，不阻塞用户对话  
✅ Token节省约70%，有效控制上下文长度  
✅ 持久化存储，支持追溯和审计  

通过此功能，即使进行100轮对话，也能将上下文控制在8k token限制内，同时保持对话的连贯性和准确性。

祝测试顺利！如有问题随时联系。
