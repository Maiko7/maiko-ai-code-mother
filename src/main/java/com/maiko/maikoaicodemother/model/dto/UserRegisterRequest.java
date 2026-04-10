package com.maiko.maikoaicodemother.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 *
 * 就是前端传进来的 比如让你注册需要的数据。你注册你不是账号密码和确认密码吗
 * 用于封装用户注册时提交的表单数据，包含账号、密码及确认密码字段。
 * 该类实现了Serializable接口以支持序列化传输。
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    /**
     * 用户账号
     *
     * 用于注册的唯一标识账号，通常为用户名、邮箱或手机号。
     */
    private String userAccount;

    /**
     * 用户密码
     *
     * 注册时设置的登录密码，需满足系统密码强度要求。
     */
    private String userPassword;

    /**
     * 确认密码
     *
     * 用于二次验证密码输入的一致性，应与userPassword字段值相同。
     */
    private String checkPassword;
}
