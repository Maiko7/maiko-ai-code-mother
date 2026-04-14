-- 为智能记忆管理添加数据库支持
-- 执行此脚本前请备份数据

USE maiko_ai_code_mother;

-- 1. 为chat_history表添加总结相关字段
ALTER TABLE chat_history
ADD COLUMN isSummarized TINYINT DEFAULT 0 NOT NULL COMMENT '是否已被总结压缩' AFTER userId,
ADD COLUMN summaryId BIGINT NULL COMMENT '所属的总结记录ID' AFTER isSummarized;

-- 2. 创建对话总结表
CREATE TABLE IF NOT EXISTS chat_summary (
    id BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
    appId BIGINT NOT NULL COMMENT '应用id',
    summaryContent TEXT NOT NULL COMMENT '总结内容（AI生成的会议纪要）',
    summarizedRounds INT NOT NULL COMMENT '本次总结涵盖的对话轮数',
    startRoundId BIGINT NOT NULL COMMENT '起始对话ID',
    endRoundId BIGINT NOT NULL COMMENT '结束对话ID',
    userId BIGINT NOT NULL COMMENT '执行总结的用户id',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    INDEX idx_appId (appId),
    INDEX idx_createTime (createTime)
) COMMENT '对话总结记录' COLLATE = utf8mb4_unicode_ci;

-- 3. 验证字段添加成功
DESC chat_history;
DESC chat_summary;
