package com.maiko.maikoaicodemother.core.parser;

/**
 * 【接口定义】代码解析器策略接口
 *
 * 核心作用：
 * 1. 统一标准：它规定了所有的“代码解析器”必须长什么样。不管你是解析 HTML 还是 Java，
 *    都必须实现这个接口，并提供一个 parseCode 方法。
 * 2. 泛型 <T>：这是一个通用的接口。
 *    - HtmlCodeParser 实现它时，T 就是 HtmlCodeResult。
 *    - MultiFileCodeParser 实现它时，T 就是 MultiFileCodeResult。
 *    这样可以保证解析出来的结果类型是安全的、对应的。
 *
 * 设计思想：
 * 就像注释里说的，“今后还有其他的新的代码体系也要实现这个接口”。
 * 这是一种开闭原则的体现：对扩展开放（加新解析器），对修改关闭（不用改现有代码）。
 */
public interface CodeParser<T> {

    /**
     * 【抽象方法】解析代码内容
     *
     * 作用：把一段“原始的字符串代码”，转换成“程序能理解的对象”。
     *
     * @param codeContent 原始代码内容（比如 AI 返回的一大段字符串）
     * @return 解析后的结果对象（泛型 T，具体类型由实现类决定）
     */
    T parseCode(String codeContent);
}