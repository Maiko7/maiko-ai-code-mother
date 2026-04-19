package com.maiko.maikoaicodemother.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.maiko.maikoaicodemother.constant.AppConstant;
import com.maiko.maikoaicodemother.exception.BusinessException;
import com.maiko.maikoaicodemother.exception.ErrorCode;
import com.maiko.maikoaicodemother.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * 【类定义】抽象代码文件保存器
 *
 * 设计模式：模板方法模式
 * 作用：定义了一套保存代码文件的“标准流程”（saveCode），
 *      但是把流程中具体的“细节”（比如存什么文件、目录叫什么）留给子类去实现。
 *
 * <T>：泛型，代表不同的代码结果对象（比如 HtmlCodeResult, JavaCodeResult 等）。
 */
public abstract class CodeFileSaverTemplate<T> {

    // 【常量】定义所有文件保存的根目录，从常量类中读取
    protected static final String FILE_SAVE_ROOT_DIR = AppConstant.CODE_OUTPUT_ROOT_DIR;

    /**
     * 【核心流程】模板方法
     *
     * final 关键字：表示这个方法是“最终”的，子类不能修改（重写）这个流程。
     * 保证所有代码保存的步骤都是一致的：校验 -> 建目录 -> 保存 -> 返回。
     */
    public final File saveCode(T result, Long appId) {
        // 1. 验证输入：检查数据是否合法
        validateInput(result);
        // 2. 构建唯一目录：根据应用ID创建专属文件夹
        String baseDirPath = buildUniqueDir(appId);
        // 3. 保存文件：这是抽象方法，具体怎么存（存几个文件、叫什么名）由子类决定
        saveFiles(result, baseDirPath);
        // 4. 返回结果：返回创建好的目录文件对象
        return new File(baseDirPath);
    }

    /**
     * 【步骤1：校验】
     * protected：允许子类访问，甚至允许子类重写（扩展）校验逻辑。
     * 默认只检查对象是否为空，子类（如 HtmlCodeFileSaverTemplate）可以增加更多检查。
     */
    protected void validateInput(T result) {
        if (result == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码结果对象不能为空");
        }
    }

    /**
     * 【步骤2：建目录】
     * final：子类不能修改建目录的逻辑。
     * 逻辑：根目录 + 类型_应用ID（例如：output/html_101）。
     */
    protected final String buildUniqueDir(Long AppId) {
        if (AppId == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用ID不能为空");
        }
        // 获取代码类型（例如 "html"），这是调用子类实现的 getCodeType()
        String codeType = getCodeType().getValue();
        // 拼接目录名：类型_应用ID
        String uniqueDirName = StrUtil.format("{}_{}", codeType, AppId);
        // 拼接完整路径
        String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;
        // 创建目录（如果不存在）
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 【工具方法】写入单个文件
     * final：工具方法，逻辑固定，子类直接调用即可。
     * 作用：把字符串内容写入到指定目录下的指定文件中。
     */
    protected final void writeToFile(String dirPath, String filename, String content) {
        // 只有内容不为空时才写入
        if (StrUtil.isNotBlank(content)) {
            String filePath = dirPath + File.separator + filename;
            // 使用 Hutool 工具类写入文件，指定 UTF-8 编码
            FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
        }
    }

    /**
     * 【抽象方法1】获取代码类型
     * abstract：没有方法体，强制要求子类必须实现。
     * 目的：让父类知道当前处理的是 HTML 还是 Java 代码，用于创建目录名。
     */
    protected abstract CodeGenTypeEnum getCodeType();

    /**
     * 【抽象方法2】保存文件的具体实现
     * abstract：强制子类实现。
     * 目的：父类不知道具体要存几个文件、文件名是什么，完全交给子类（如 HtmlCodeFileSaverTemplate）去写。
     */
    protected abstract void saveFiles(T result, String baseDirPath);
}