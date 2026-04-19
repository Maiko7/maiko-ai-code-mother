package com.maiko.maikoaicodemother.ai.tools;

import cn.hutool.json.JSONObject;
import com.maiko.maikoaicodeuser.constant.AppConstant;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件删除工具
 * <p>
 * 支持 AI 通过工具调用的方式删除指定路径的文件。
 * 包含安全检查，防止删除项目中的核心配置文件。
 */
@Slf4j
@Component
public class FileDeleteTool extends BaseTool {

    /**
     * 执行文件删除操作
     *
     * @param relativeFilePath 文件的相对路径（相对于项目根目录）
     * @param appId            应用ID，用于构建项目根目录路径
     * @return 操作结果信息（成功或失败原因）
     */
    @Tool("删除指定路径的文件")
    public String deleteFile(
            @P("文件的相对路径")
            String relativeFilePath,
            @ToolMemoryId Long appId
    ) {
        try {
            Path path = Paths.get(relativeFilePath);
            // 如果传入的是相对路径，则拼接上项目的根目录
            if (!path.isAbsolute()) {
                String projectDirName = "vue_project_" + appId;
                Path projectRoot = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, projectDirName);
                path = projectRoot.resolve(relativeFilePath);
            }

            // 检查文件是否存在
            if (!Files.exists(path)) {
                return "警告：文件不存在，无需删除 - " + relativeFilePath;
            }

            // 检查是否为普通文件（防止误删目录）
            if (!Files.isRegularFile(path)) {
                return "错误：指定路径不是文件，无法删除 - " + relativeFilePath;
            }

            // 安全检查：避免删除重要配置文件
            String fileName = path.getFileName().toString();
            if (isImportantFile(fileName)) {
                return "错误：不允许删除重要文件 - " + fileName;
            }

            // 执行删除
            Files.delete(path);
            log.info("成功删除文件: {}", path.toAbsolutePath());
            return "文件删除成功: " + relativeFilePath;

        } catch (IOException e) {
            String errorMessage = "删除文件失败: " + relativeFilePath + ", 错误: " + e.getMessage();
            log.error(errorMessage, e);
            return errorMessage;
        }
    }

    /**
     * 判断是否是重要文件，如果是则禁止删除
     *
     * @param fileName 文件名
     * @return true 表示是重要文件，不允许删除
     */
    private boolean isImportantFile(String fileName) {
        String[] importantFiles = {
                "package.json", "package-lock.json", "yarn.lock", "pnpm-lock.yaml",
                "vite.config.js", "vite.config.ts", "vue.config.js",
                "tsconfig.json", "tsconfig.app.json", "tsconfig.node.json",
                "index.html", "main.js", "main.ts", "App.vue", ".gitignore", "README.md"
        };
        for (String important : importantFiles) {
            if (important.equalsIgnoreCase(fileName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getToolName() {
        return "deleteFile";
    }

    @Override
    public String getDisplayName() {
        return "删除文件";
    }

    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        String relativeFilePath = arguments.getStr("relativeFilePath");
        return String.format(" [工具调用] %s %s", getDisplayName(), relativeFilePath);
    }
}