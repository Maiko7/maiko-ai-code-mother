package com.maiko.maikoaicodemother.core.parser;

/**
 * 代码解析器策略接口
 * 这就是统一方法的请求参数，你今后还有其他的新的代码体系也要实现这个接口
 */
public interface CodeParser<T> {

    /**
     * 解析代码内容
     * 
     * @param codeContent 原始代码内容
     * @return 解析后的结果对象
     */
    T parseCode(String codeContent);
}
