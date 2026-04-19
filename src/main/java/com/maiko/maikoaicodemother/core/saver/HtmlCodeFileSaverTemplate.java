package com.maiko.maikoaicodemother.core.saver;

import cn.hutool.core.util.StrUtil;
import com.maiko.maikoaicodemother.ai.model.HtmlCodeResult;
import com.maiko.maikoaicodemother.exception.BusinessException;
import com.maiko.maikoaicodemother.exception.ErrorCode;
import com.maiko.maikoaicodemother.model.enums.CodeGenTypeEnum;

/**
 * 【类的作用】
 * 这是一个专门用来保存 HTML 代码文件的类。
 * 它继承自 CodeFileSaverTemplate，意味着它复用了父类的“保存流程”，
 * 只需要自己实现“怎么保存 HTML”的具体细节。
 * <HtmlCodeResult> 表示它处理的数据类型是 HtmlCodeResult。
 */
public class HtmlCodeFileSaverTemplate extends CodeFileSaverTemplate<HtmlCodeResult> {

    /**
     * 【方法1：定义类型】
     * 告诉系统：我是处理哪种代码的？
     */
    @Override
    protected CodeGenTypeEnum getCodeType() {
        // 返回枚举 HTML，表示这个类专门处理 HTML 类型的代码生成
        return CodeGenTypeEnum.HTML;
    }

    /**
     * 【方法2：执行保存】
     * 这是最核心的逻辑：拿到数据后，具体怎么存盘？
     */
    @Override
    protected void saveFiles(HtmlCodeResult result, String baseDirPath) {
        // 调用父类的工具方法 writeToFile
        // 意思就是：在 baseDirPath 目录下，创建一个叫 "index.html" 的文件，
        // 把 result 里的 htmlCode 内容写进去。
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
    }

    /**
     * 【方法3：数据校验】
     * 在保存之前，检查一下数据合不合法。
     */
    @Override
    protected void validateInput(HtmlCodeResult result) {
        // 1. 先执行父类通用的校验（比如检查 result 对象是不是 null）
        super.validateInput(result);

        // 2. 再执行针对 HTML 的特有校验
        // 使用 Hutool 工具类判断 HTML 内容是不是空的
        if (StrUtil.isBlank(result.getHtmlCode())) {
            // 如果 HTML 内容是空的，就抛出一个业务异常，提示“内容不能为空”
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HTML代码内容不能为空");
        }
    }
}