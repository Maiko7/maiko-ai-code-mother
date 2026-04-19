package com.maiko.maikoaicodemother.utils;


import cn.hutool.core.util.StrUtil;

/**
 * 这一个正则类抽出来了校验手机、邮箱、验证码的格式
 * 它利用了字符串的matches匹配正则表达式，所以它还创建了一个正则类
 */
public class RegexUtils {
    /**
     * 是否是无效手机格式
     * @param phone 要校验的手机号
     * @return true:不符合，false：符合
     */
    public static boolean isPhoneInvalid(String phone){
        /**
         * 看见没有这就是那个正则类中的，电话号码的正则
         * 人家通过这个判断你的格式对不对有没有非法字符。
         */
        return mismatch(phone, RegexPatterns.PHONE_REGEX);
    }
    /**
     * 是否是无效邮箱格式
     * @param email 要校验的邮箱
     * @return true:不符合，false：符合
     */
    public static boolean isEmailInvalid(String email){
        return mismatch(email, RegexPatterns.EMAIL_REGEX);
    }


    /**
     * 是否是无效 URL 格式，无效返回True
     * @param url 要校验的 URL
     * @return true:不符合，false：符合
     */
    public static boolean isUrlInvalid(String url){
        return mismatch(url, RegexPatterns.URL_REGEX);
    }

    // 校验是否不符合正则格式
    private static boolean mismatch(String str, String regex){
        // StrUtil是胡图的工具类
        if (StrUtil.isBlank(str)) {
            return true;
        }
        /**
         * matches用于判断 str 是否符合指定的正则表达式 regex。它返回一个布尔值，
         * 如果字符串 str 符合正则表达式 regex，则返回 true，否则返回 false。
         * 他这里是取反，也就是不符合返回false但是取反就是true了
         */
        return !str.matches(regex);
    }
}
