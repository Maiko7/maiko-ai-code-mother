package com.maiko.maikoaicodeuser.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 微信登录请求
 */
@Data
public class WxLoginRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 微信授权code
     */
    private String code;
}
