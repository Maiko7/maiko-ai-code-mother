# 快速测试指南

## 🎯 测试清单

### ✅ 测试1: 默认封面兜底 (5分钟)

```bash
# 1. 启动应用
mvn spring-boot:run

# 2. 查询一个没有封面的应用（或手动修改数据库）
curl http://localhost:8123/app/get/vo?id=YOUR_APP_ID

# 3. 检查返回的 cover 字段
# 预期: "cover": "/images/Mango.png"

# 4. 浏览器访问默认封面
# http://localhost:8123/images/Mango.png
# 预期: 显示芒果图片
```

---

### ✅ 测试2: 删除应用时清理云端文件 (10分钟)

```bash
# 1. 找一个有封面的应用
SELECT id, appName, cover FROM app WHERE cover IS NOT NULL LIMIT 1;

# 2. 登录腾讯云 COS 控制台，确认文件存在
# https://console.cloud.tencent.com/cos

# 3. 删除应用
curl -X POST http://localhost:8123/app/delete \
  -H "Content-Type: application/json" \
  -d '{"id": YOUR_APP_ID}' \
  -H "Cookie: YOUR_COOKIE"

# 4. 刷新 COS 控制台，确认文件已删除

# 5. 查看日志
tail -f logs/application.log | grep "应用云端封面删除"
```

---

### ✅ 测试3: 定时任务手动触发 (5分钟)

```bash
# 1. 创建测试文件夹
mkdir -p tmp/screenshots/test_old_1
mkdir -p tmp/code_output/test_old_2
touch -d "10 days ago" tmp/screenshots/test_old_1
touch -d "10 days ago" tmp/code_output/test_old_2

# 2. 编写简单测试类
cat > src/test/java/com/maiko/maikoaicodemother/task/FileCleanupTaskTest.java << 'EOF'
package com.maiko.maikoaicodemother.task;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FileCleanupTaskTest {
    @Resource
    private FileCleanupTask fileCleanupTask;
    
    @Test
    void testManualCleanup() {
        fileCleanupTask.manualCleanup();
    }
}
EOF

# 3. 运行测试
mvn test -Dtest=FileCleanupTaskTest

# 4. 检查文件夹是否被删除
ls tmp/screenshots/  # test_old_1 应该不存在
ls tmp/code_output/  # test_old_2 应该不存在
```

---

## 📊 验证结果

| 测试项 | 状态 | 备注 |
|--------|------|------|
| 默认封面返回 | ⬜ 待测试 | |
| 默认封面可访问 | ⬜ 待测试 | |
| 云端文件删除 | ⬜ 待测试 | |
| 定时任务执行 | ⬜ 待测试 | |
| 日志正常输出 | ⬜ 待测试 | |

---

## 🔍 常用调试命令

```bash
# 查看实时日志
tail -f logs/application.log

# 搜索封面相关日志
grep "封面" logs/application.log

# 搜索定时任务日志
grep "清理任务" logs/application.log

# 搜索COS删除日志
grep "COS文件删除" logs/application.log

# 检查磁盘使用情况
du -sh tmp/*
```

---

## ❌ 常见问题

### Q1: 默认封面返回404
**解决**: 检查静态资源配置
```yaml
spring:
  web:
    resources:
      static-locations: classpath:/static/
```

### Q2: 定时任务不执行
**解决**: 确认 `@EnableScheduling` 已添加

### Q3: COS删除失败
**解决**: 检查 AK/SK 权限和网络连接
