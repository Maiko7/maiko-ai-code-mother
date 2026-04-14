package com.maiko.maikoaicodemother.ai.tools;

import com.maiko.maikoaicodemother.constant.AppConstant;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * 文件写入工具
 * 作用：给 AI 调用，让 AI 能自动生成并保存项目文件（如 Vue、Java、HTML）
 */
@Slf4j
public class FileWriteTool {

    /**
     * 给 AI 使用的【文件写入工具】
     * @Tool 注解 = 告诉 AI 这个工具的功能（AI 能看懂）
     *
     * @param relativeFilePath  文件相对路径（由 AI 提供，如 src/App.vue）
     * @param content           文件内容（AI 生成的代码）
     * @param appId             项目唯一标识（区分不同用户/不同项目）
     * @return                  返回给 AI 的执行结果（成功/失败）
     */
    @Tool("写入文件到指定路径")
    public String writeFile(
            // @P = 告诉 AI 这个参数是干嘛的 → AI 会自动传值
            @P("文件的相对路径") String relativeFilePath,
            @P("要写入文件的内容") String content,
            // 工具记忆ID → 用来区分不同项目，避免文件覆盖
            @ToolMemoryId Long appId
    ) {
        try {
            // 把 AI 传过来的路径变成 Path 对象
            Path path = Paths.get(relativeFilePath);

            // 如果不是绝对路径 → 按项目目录生成
            if (!path.isAbsolute()) {
                // 生成项目文件夹名称：vue_project_项目ID
                String projectDirName = "vue_project_" + appId;
                // 项目根目录 = 配置的根目录 + 项目文件夹
                Path projectRoot = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, projectDirName);
                // 最终文件路径 = 项目根目录 + AI 传的相对路径
                path = projectRoot.resolve(relativeFilePath);
            }

            // 获取文件所在的父目录
            Path parentDir = path.getParent();
            if (parentDir != null) {
                // 如果目录不存在 → 自动创建所有层级目录
                Files.createDirectories(parentDir);
            }

            // 写入文件
            // StandardOpenOption.CREATE = 文件不存在就创建
            // StandardOpenOption.TRUNCATE_EXISTING = 文件存在就覆盖内容
            Files.write(path, content.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            // 打印日志
            log.info("成功写入文件: {}", path.toAbsolutePath());

            // 返回结果给 AI（只返回相对路径，不暴露服务器真实路径）
            return "文件写入成功: " + relativeFilePath;

        } catch (IOException e) {
            // 异常处理：报错 + 打日志
            String errorMessage = "文件写入失败: " + relativeFilePath + ", 错误: " + e.getMessage();
            log.error(errorMessage, e);
            return errorMessage;
        }
    }
}