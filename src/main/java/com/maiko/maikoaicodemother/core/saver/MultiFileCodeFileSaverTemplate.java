package com.maiko.maikoaicodemother.core.saver;

import cn.hutool.core.util.StrUtil;
import com.maiko.maikoaicodemother.ai.model.MultiFileCodeResult;
import com.maiko.maikoaicodemother.exception.BusinessException;
import com.maiko.maikoaicodemother.exception.ErrorCode;
import com.maiko.maikoaicodemother.model.enums.CodeGenTypeEnum;

/**
 * 【类定义】多文件代码保存器
 *
 * 作用：专门用来保存包含多个文件的项目（例如：HTML + CSS + JS）。
 * 设计思路：
 *   它继承自父类 CodeFileSaverTemplate，复用了“建目录、校验、写文件”的通用流程。
 *   它只需要告诉父类：“我是多文件类型”以及“具体要存哪几个文件”。
 *   正如注释所说，这种拆分方式让代码结构非常清晰，维护时只需要找对应的类即可。
 */
public class MultiFileCodeFileSaverTemplate extends CodeFileSaverTemplate<MultiFileCodeResult> {

    /**
     * 【实现1：定义类型】
     * 告诉系统：我是处理多文件类型的。
     */
    @Override
    public CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.MULTI_FILE;
    }

    /**
     * 【实现2：具体保存逻辑】
     * 这里定义了多文件项目具体包含哪些文件。
     *
     * 逻辑：
     *   1. 调用父类的 writeToFile 方法。
     *   2. 分别把 HTML、CSS、JS 内容写入对应的文件名（index.html, style.css, script.js）。
     *   3. 如果某个内容为空（比如 CSS），writeToFile 内部会自动跳过（基于之前的代码逻辑）。
     */
    @Override
    protected void saveFiles(MultiFileCodeResult result, String baseDirPath) {
        // 保存 HTML 文件 -> index.html
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
        // 保存 CSS 文件 -> style.css
        writeToFile(baseDirPath, "style.css", result.getCssCode());
        // 保存 JavaScript 文件 -> script.js
        writeToFile(baseDirPath, "script.js", result.getJsCode());
    }

    /**
     * 【实现3：自定义校验】
     * 针对多文件场景的特殊校验规则。
     */
    @Override
    protected void validateInput(MultiFileCodeResult result) {
        // 1. 先执行父类的通用校验（比如检查 result 是否为空）
        super.validateInput(result);

        // 2. 增加特定业务校验：
        // 对于多文件项目，CSS 和 JS 可以为空（可能只有结构），但 HTML 必须有。
        if (StrUtil.isBlank(result.getHtmlCode())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HTML代码内容不能为空");
        }
    }
}