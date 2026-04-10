package com.maiko.maikoaicodemother.constant;

/**
 * 用户常量接口
 * <p>定义用户相关的常量，包括登录态、短信验证码、角色权限等</p>
 */
public interface UserConstant {

    /**
     * 用户登录态键
     * <p>用于Session或Redis中存储用户登录信息的键名</p>
     */
    String USER_LOGIN_STATE = "user_login";


    // ==================== 短信验证码 Redis Key ====================

    /**
     * 短信验证码Redis键前缀
     * <p>格式: verify:code:{手机号}</p>
     */
    String VERIFY_CODE_KEY = "verify:code:";

    /**
     * 短信验证码发送频率限制Redis键前缀
     * <p>格式: verify:rate:{手机号}</p>
     */
    String VERIFY_RATE_LIMIT_KEY = "verify:rate:";

    /**
     * 验证码有效期（分钟）
     */
    long VERIFY_CODE_TTL = 5;

    /**
     * 验证码发送频率限制时间（秒）
     */
    long VERIFY_RATE_LIMIT_TTL = 60;

    /**
     * 验证码发送限制提示信息
     */
    String VERIFY_LIMIT_FLAG = "请60秒后再发送验证码";


    //  region 权限

    /**
     * 默认角色
     * <p>新用户注册时分配的默认角色</p>
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员角色
     * <p>具有系统管理权限的角色</p>
     */
    String ADMIN_ROLE = "admin";

    /**
     * 密码加密盐值
     * <p>用于用户密码加密的固定盐值</p>
     */
    String SALT = "maiko";
    // endregion
}
