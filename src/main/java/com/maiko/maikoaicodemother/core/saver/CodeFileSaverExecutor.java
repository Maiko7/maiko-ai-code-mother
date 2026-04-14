package com.maiko.maikoaicodemother.core.saver;

import com.maiko.maikoaicodemother.ai.model.HtmlCodeResult;
import com.maiko.maikoaicodemother.ai.model.MultiFileCodeResult;
import com.maiko.maikoaicodemother.exception.BusinessException;
import com.maiko.maikoaicodemother.exception.ErrorCode;
import com.maiko.maikoaicodemother.model.enums.CodeGenTypeEnum;

import java.io.File;

/**
 * 【类定义】代码文件保存执行器
 *
 * 角色定位：这是整个“代码保存功能”的总入口（门面模式）。
 * 作用：
 *   1. 外部调用者不需要知道具体有哪些保存器（HtmlSaver, MultiFileSaver）。
 *   2. 只需要把结果对象和类型丢给这个类，它会自动根据类型选择对应的保存器去执行。
 */
public class CodeFileSaverExecutor {

    // 【静态成员】初始化具体的保存器实例
    // 这里直接 new 了具体的实现类。因为保存器通常是无状态的（只负责干活），所以用 static 全局共享一份即可。
    private static final HtmlCodeFileSaverTemplate htmlCodeFileSaver = new HtmlCodeFileSaverTemplate();

    private static final MultiFileCodeFileSaverTemplate multiFileCodeFileSaver = new MultiFileCodeFileSaverTemplate();

    /**
     * 【核心方法】执行代码保存
     *
     * @param codeResult  代码结果对象（可能是 HTML 结果，也可能是多文件结果）
     * @param codeGenType 代码生成类型（用来判断到底该用哪个保存器）
     * @param appId       应用ID（用于构建目录）
     * @return 返回保存后的目录文件对象
     */
    public static File executeSaver(Object codeResult, CodeGenTypeEnum codeGenType, Long appId) {
        // 使用 Java 14+ 的 switch 表达式语法，代码更简洁
        return switch (codeGenType) {
            // 情况1：如果是 HTML 类型
            case HTML ->
                // 1. 强制类型转换：把通用的 Object 转回具体的 HtmlCodeResult
                // 2. 调用之前定义的 htmlCodeFileSaver 的 saveCode 方法（执行模板流程）
                    htmlCodeFileSaver.saveCode((HtmlCodeResult) codeResult, appId);

            // 情况2：如果是 多文件 类型
            case MULTI_FILE ->
                // 同理，转换类型并调用多文件保存器
                    multiFileCodeFileSaver.saveCode((MultiFileCodeResult) codeResult, appId);

            // 情况3：如果传入了不支持的类型
            default ->
                // 直接抛出业务异常，告诉调用方“我不支持这种格式”
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型: " + codeGenType);
        };
    }
}