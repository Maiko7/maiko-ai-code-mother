package com.maiko.maikoaicodemother.utils;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;

/**
 * 缓存 Key 生成工具类
 * <p>
 * 该类主要用于在缓存场景下，将复杂的对象参数转换为唯一的字符串标识。
 * 通过将对象序列化为 JSON 字符串并进行 MD5 哈希，确保不同属性组合的参数能生成不同的缓存键，
 * 同时保持键的长度固定且安全。
 * </p>
 *
 * @author Maiko7
 */
public class CacheKeyUtils {

    /**
     * 根据对象生成缓存 Key (JSON序列化 + MD5哈希)
     * <p>
     * 该方法首先检查对象是否为空，如果为空则返回 "null" 的 MD5 值。
     * 否则，使用 Hutool 工具将对象转换为 JSON 字符串，然后计算该字符串的 MD5 摘要作为最终键。
     * 这种方式适用于需要将整个请求体或复杂查询条件作为缓存维度的场景。
     * </p>
     *
     * @param obj 要生成 key 的对象（可以是 Map、POJO 或其他类型）
     * @return MD5 哈希后的缓存 key（32位小写十六进制字符串）
     */
    public static String generateKey(Object obj) {
        if (obj == null) {
            return DigestUtil.md5Hex("null");
        }
        // 先转 JSON，保证对象内容的有序性和一致性，再进行 MD5 加密
        /**
         * 它这里为什么还要转成MD5，其实JSON就已经一致性了。
         * 因为你JSON占用空间太大了，转成MD5提升性能。
         */
        String jsonStr = JSONUtil.toJsonStr(obj);
        return DigestUtil.md5Hex(jsonStr);
    }
}