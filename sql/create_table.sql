# 数据库初始化

-- 创建库
create database if not exists maiko_ai_code_mother;

-- 切换库
use maiko_ai_code_mother;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin/ban/vip/editor/partner',
    phone        varchar(20)                            null comment '手机号（用于验证码登录）',
    unionId      varchar(256)                           null comment '微信开放平台UnionID（用于跨应用识别用户）',
    openId       varchar(256)                           null comment '微信公众号OpenID',
    editTime     datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    UNIQUE KEY uk_userAccount (userAccount),
    UNIQUE KEY uk_phone (phone),
    UNIQUE KEY uk_unionId (unionId),
    INDEX idx_userName (userName),
    INDEX idx_openId (openId)
) comment '用户' collate = utf8mb4_unicode_ci;

INSERT INTO user (userAccount, userPassword, userName, userAvatar, userProfile, userRole, phone, unionId, openId) VALUES
('admin', '123456', '管理员', 'https://profile-avatar.csdnimg.cn/0149eab8ef14432986f56a2cab3ad1c9_weixin_44146541.jpg!1', '每天都要充实开心呀', 'admin', NULL, NULL, NULL),
('huge', '123456', '胡哥', 'https://profile-avatar.csdnimg.cn/0149eab8ef14432986f56a2cab3ad1c9_weixin_44146541.jpg!1', '每天都要充实开心呀', 'user', NULL, NULL, NULL),
('maiko', '123456', '写不来代码的Maiko', 'https://profile-avatar.csdnimg.cn/0149eab8ef14432986f56a2cab3ad1c9_weixin_44146541.jpg!1', '每天都要充实开心呀', 'user', NULL, NULL, NULL);

--  扩展设计
--    vipExpireTime datetime     null comment '会员过期时间',
--    vipCode       varchar(128) null comment '会员兑换码',
--    vipNumber     bigint       null comment '会员编号'
--    shareCode     varchar(20)  DEFAULT NULL COMMENT '分享码',
--    inviteUser    bigint       DEFAULT NULL COMMENT '邀请用户 id'
--    phone        varchar(20)   null comment '手机号（用于验证码登录）',
--    unionId      varchar(256)    null comment '微信开放平台UnionID（用于跨应用识别用户）',

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


-- 应用表
create table app
(
    id           bigint auto_increment comment 'id' primary key,
    appName      varchar(256)                       null comment '应用名称',
    cover        varchar(512)                       null comment '应用封面',
    initPrompt   text                               null comment '应用初始化的 prompt',
    codeGenType  varchar(64)                        null comment '代码生成类型（枚举）',
    deployKey    varchar(64)                        null comment '部署标识',
    deployedTime datetime                           null comment '部署时间',
    priority     int      default 0                 not null comment '优先级',
    userId       bigint                             not null comment '创建用户id',
    editTime     datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    UNIQUE KEY uk_deployKey (deployKey), -- 确保部署标识唯一
    INDEX idx_appName (appName),         -- 提升基于应用名称的查询性能
    INDEX idx_userId (userId)            -- 提升基于用户 ID 的查询性能
) comment '应用' collate = utf8mb4_unicode_ci;

-- 对话历史表
create table chat_history
(
    id          bigint auto_increment comment 'id' primary key,
    message     text                               not null comment '消息',
    messageType varchar(32)                        not null comment 'user/ai',
    appId       bigint                             not null comment '应用id',
    userId      bigint                             not null comment '创建用户id',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除',
    INDEX idx_appId (appId),                       -- 提升基于应用的查询性能
    INDEX idx_createTime (createTime),             -- 提升基于时间的查询性能
    INDEX idx_appId_createTime (appId, createTime) -- 游标查询核心索引
) comment '对话历史' collate = utf8mb4_unicode_ci;

-- parentId   bigint  null comment '父消息id（便于生成失败时重试）'
