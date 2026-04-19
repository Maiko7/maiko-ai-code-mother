package com.maiko.maikoaicodemother.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.maiko.maikoaicodemother.ai.model.HtmlCodeResult;
import com.maiko.maikoaicodemother.ai.model.MultiFileCodeResult;
import com.maiko.maikoaicodemother.model.enums.CodeGenTypeEnum;

import java.io.File;

/**
 * 代码文件保存工具类
 * <p>
 * 提供将AI生成的代码结果保存到本地文件系统的功能，支持HTML单文件和多文件（HTML+CSS+JS）两种模式。
 * 所有文件保存在统一根目录下，通过业务类型和雪花算法生成的唯一ID创建独立目录。
 * </p>
 *
 * @author Maiko7
 * @create 2026-04-11 15:03
 */
@Deprecated
public class CodeFileSaver {

    /**
     * 文件保存的根目录路径
     */
    private static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

    /**
     * 保存HTML网页代码
     * <p>
     * 在唯一目录下创建 index.html 文件，将传入的HTML代码写入该文件。
     * 目录命名格式：{bizType}_{雪花ID}，其中 bizType 为 "html"
     * </p>
     *
     * @param htmlCodeResult HTML代码结果对象，包含要保存的HTML代码内容
     * @return 保存HTML文件的目录对象
     */
    public static File saveHtmlCodeResult(HtmlCodeResult htmlCodeResult) {
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.HTML.getValue());
        writeToFile(baseDirPath, "index.html", htmlCodeResult.getHtmlCode());
        return new File(baseDirPath);
    }

    /**
     * 保存多文件代码到文件系统
     * <p>
     * 在唯一目录下创建三个文件：index.html、style.css、script.js，
     * 分别写入对应的HTML、CSS和JavaScript代码内容。
     * 目录命名格式：{bizType}_{雪花ID}，其中 bizType 为 "multi_file"
     * </p>
     *
     * @param result 多文件代码结果对象，包含HTML、CSS和JavaScript代码内容
     * @return 保存代码文件的目录对象
     */
    public static File saveMultiFileCodeResult(MultiFileCodeResult result) {
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.MULTI_FILE.getValue());
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
        writeToFile(baseDirPath, "style.css", result.getCssCode());
        writeToFile(baseDirPath, "script.js", result.getJsCode());
        return new File(baseDirPath);
    }

    /**
     * 构建唯一的文件保存路径
     * <p>
     * 根据业务类型和雪花算法生成的唯一ID创建目录，确保每次保存都使用独立的目录。
     * 目录结构：{FILE_SAVE_ROOT_DIR}/{bizType}_{snowflakeId}
     * 如果目录已存在则复用，不存在则创建新目录
     * </p>
     *
     * @param bizType 业务类型标识，用于区分不同的代码生成模式
     * @return 创建的唯一目录的绝对路径
     */
    private static String buildUniqueDir(String bizType) {
        String uniqueDirName = StrUtil.format("{}_{}", bizType, IdUtil.getSnowflakeNextIdStr());
        String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 将文本内容写入指定目录下的文件
     * <p>
     * 使用UTF-8编码将内容写入到指定目录下的指定文件中。
     * 如果文件已存在则覆盖，不存在则创建新文件
     * </p>
     *
     * @param dirPath 目标目录的绝对路径
     * @param filename 要创建或覆盖的文件名
     * @param content 要写入文件的文本内容
     */
    private static void writeToFile(String dirPath, String filename, String content) {
        String filePath = dirPath + File.separator + filename;
        FileUtil.writeUtf8String(content, filePath);
    }
}
