package com.maiko.maikoaicodemother.core.parser;

import com.maiko.maikoaicodemother.exception.BusinessException;
import com.maiko.maikoaicodemother.exception.ErrorCode;
import com.maiko.maikoaicodemother.model.enums.CodeGenTypeEnum;

/**
 * 【类定义】代码解析执行器
 *
 * 角色定位：这是“解析模块”的总门面（Facade）。
 * 作用：
 *   1. 外部（比如 Controller 或 Service）不需要知道有 HtmlCodeParser 还是 MultiFileCodeParser。
 *   2. 外部只需要调用 executeParser，告诉它“这是什么类型的代码”，它就能自动分发任务。
 *   3. 实现了“组合调用”，将不同的解析器统一管理起来。
 */
public class CodeParserExecutor {

    // 【静态成员】初始化具体的解析器实例
    // 为什么用 static final？
    // 1. 解析器通常是无状态的（没有成员变量），所以可以全局共享，节省内存。
    // 2. 避免每次调用都 new 对象，提高性能。
    // 对比注释里的“版本1”，那里是每次调用都 new 一次，效率较低。
    private static final HtmlCodeParser htmlCodeParser = new HtmlCodeParser();

    private static final MultiFileCodeParser multiFileCodeParser = new MultiFileCodeParser();

    /**
     * 【核心方法】执行代码解析
     *
     * 逻辑流程：
     * 1. 接收原始代码字符串和代码类型。
     * 2. 使用 switch 判断类型。
     * 3. 调用对应的解析器（htmlCodeParser 或 multiFileCodeParser）的 parseCode 方法。
     * 4. 返回解析后的对象。
     *
     * 注意：返回值是 Object，因为不同解析器返回的结果类型不同（HtmlCodeResult 或 MultiFileCodeResult）。
     * 调用者拿到结果后通常需要进行类型转换，或者使用泛型优化（但这里为了简单用了 Object）。
     */
    public static Object executeParser(String codeContent, CodeGenTypeEnum codeGenType) {
        return switch (codeGenType) {
            // 如果是 HTML 类型，交给 htmlCodeParser 处理
            case HTML -> htmlCodeParser.parseCode(codeContent);

            // 如果是多文件类型，交给 multiFileCodeParser 处理
            case MULTI_FILE -> multiFileCodeParser.parseCode(codeContent);

            // 如果类型不匹配，直接报错
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型: " + codeGenType);
        };
    }
}