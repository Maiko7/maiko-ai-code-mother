-- 为app表添加totalRounds字段
-- 执行此脚本前请备份数据

USE maiko_ai_code_mother;

-- 添加新字段
ALTER TABLE app
ADD COLUMN totalRounds INT DEFAULT 0 NOT NULL COMMENT '对话总轮数' AFTER userId;

-- 验证字段添加成功
DESC app;
