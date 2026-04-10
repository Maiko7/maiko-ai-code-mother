-- 为user表添加微信登录和手机号登录相关字段
-- 执行此脚本前请备份数据

USE maiko_ai_code_mother;

-- 添加新字段
ALTER TABLE user
ADD COLUMN phone VARCHAR(20) NULL COMMENT '手机号（用于验证码登录）' AFTER userRole,
ADD COLUMN unionId VARCHAR(256) NULL COMMENT '微信开放平台UnionID（用于跨应用识别用户）' AFTER phone,
ADD COLUMN openId VARCHAR(256) NULL COMMENT '微信公众号OpenID' AFTER unionId;

-- 添加唯一索引
ALTER TABLE user
ADD UNIQUE INDEX uk_phone (phone),
ADD UNIQUE INDEX uk_unionId (unionId),
ADD INDEX idx_openId (openId);
