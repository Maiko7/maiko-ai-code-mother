-- 测试脚本：验证新增字段是否添加成功

USE maiko_ai_code_mother;

-- 1. 查看user表结构，确认新字段已添加
DESC user;

-- 2. 查看索引，确认新索引已创建
SHOW INDEX FROM user;

-- 3. 插入测试数据（可选）
-- 注意：执行前请确认是否需要测试数据

-- 测试手机号用户
INSERT INTO user (userAccount, userPassword, userName, phone, userRole)
VALUES ('13800138000', '', '测试用户1', '13800138000', 'user')
ON DUPLICATE KEY UPDATE userName = '测试用户1';

-- 测试微信用户
INSERT INTO user (userAccount, userPassword, userName, unionId, openId, userRole)
VALUES ('wx_test', '', '微信测试用户', 'test_union_id_123', 'test_open_id_456', 'user')
ON DUPLICATE KEY UPDATE userName = '微信测试用户';

-- 4. 查询测试数据
SELECT id, userAccount, userName, phone, unionId, openId, userRole
FROM user
WHERE phone IS NOT NULL OR unionId IS NOT NULL;

-- 5. 清理测试数据（如果需要）
-- DELETE FROM user WHERE phone = '13800138000' OR unionId = 'test_union_id_123';
