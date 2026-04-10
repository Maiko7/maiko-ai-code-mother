package com.maiko.maikoaicodemother.utils;

import cn.hutool.core.util.StrUtil;
import com.maiko.maikoaicodemother.exception.BusinessException;
import com.maiko.maikoaicodemother.exception.ErrorCode;


/**
 * 用户信息校验工具类
 * 提供静态方法来校验用户账号、密码、手机号和邮箱的合法性
 */
public class UserValidator {

    /**
     * 校验用户账号的合法性
     *
     * @param account 用户账号，应为非空字符串，长度在4到20位之间
     * @throws BusinessException 如果账号为空或长度不符合要求，则抛出业务异常
     */
    public static void validateAccount(String account) {
        // 检查账号是否为空或只包含空白字符
        if (StrUtil.isBlank(account)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不能为空");
        }
        // 检查账号长度是否在规定范围内
        if (account.length() < 2 || account.length() > 10) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度必须在 2~10 位之间");
        }
    }

    /**
     * 校验用户密码的合法性及一致性
     *
     * @param password 用户密码，应为非空字符串，长度在6到16位之间
     * @param checkPassword 用户再次输入的密码，用于确认密码的一致性
     * @throws BusinessException 如果密码为空、长度不符合要求或两次输入的密码不一致，则抛出业务异常
     */
    public static void validatePassword(String password, String checkPassword) {
        // 检查密码及其确认密码是否为空或只包含空白字符
        if (StrUtil.hasBlank(password, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不能为空");
        }
        // 检查密码长度是否在规定范围内
        if (password.length() < 6 || password.length() > 16) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度必须在 6~16 位之间");
        }
        // 检查两次输入的密码是否一致
        if (!password.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
    }

    /**
     * 校验用户手机号的合法性
     *
     * @param phone 用户手机号，应为非空字符串，且符合手机号的正则表达式规则
     * @throws BusinessException 如果手机号为空或格式不正确，则抛出业务异常
     */
    public static void validatePhone(String phone) {
        // 使用正则表达式检查手机号格式是否正确
        if (RegexUtils.isPhoneInvalid(phone)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号格式不正确");
        }
    }

    /**
     * 校验用户邮箱的合法性
     *
     * @param email 用户邮箱，应为非空字符串，且符合邮箱的正则表达式规则
     * @throws BusinessException 如果邮箱为空或格式不正确，则抛出业务异常
     */
    public static void validateEmail(String email) {
        // 使用正则表达式检查邮箱格式是否正确
        if (RegexUtils.isEmailInvalid(email)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式不正确");
        }
    }

    /**
     * 校验 URL 的合法性
     *
     * @param url URL 字符串
     * @throws BusinessException 如果 URL 格式不正确，则抛出业务异常
     */
    public static void validateUrl(String url) {
        if (RegexUtils.isUrlInvalid(url)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "URL 格式不正确");
        }
    }
}
