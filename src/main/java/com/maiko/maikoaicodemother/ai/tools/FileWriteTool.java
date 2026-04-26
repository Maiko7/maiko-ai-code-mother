package com.maiko.maikoaicodemother.ai.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import com.maiko.maikoaicodemother.constant.AppConstant;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * 文件写入工具
 * <p>
 * 核心职责：
 * 1. 暴露给 AI 一个 "writeFile" 能力。
 * 2. 负责处理文件路径隔离（不同用户/应用写入不同目录）。
 * 3. 负责自动创建缺失的父级目录。
 *
 * @author Maiko
 */
@Slf4j
@Component
public class FileWriteTool extends BaseTool {

    /**
     * 执行文件写入操作
     *
     * @param relativeFilePath 文件的相对路径 (例如: "src/components/App.vue")
     * @param content          要写入的文件内容 (源代码字符串)
     * @param appId            应用ID (由 @ToolMemoryId 自动注入，用于区分不同用户的项目)
     * @return 操作结果信息 (返回给 AI 的反馈)
     */
    @Tool("写入文件到指定路径") // 【关键】告诉 AI：我有一个工具叫“写入文件到指定路径”，当你需要保存代码时调用我。
    public String writeFile(
            @P("文件的相对路径") String relativeFilePath,
            @P("要写入文件的内容") String content,
            @ToolMemoryId Long appId // 【关键】自动获取当前会话的 ID，不需要 AI 传递，保证安全性
    ) {
        try {
            Path path = Paths.get(relativeFilePath);

            // --- 路径安全与隔离处理 ---
            if (!path.isAbsolute()) {
                // 如果 AI 传的是相对路径（通常都是），我们需要把它映射到具体的物理磁盘目录
                // 策略：根目录/常量前缀 + appId/相对路径
                // 这样保证了用户A的 AI 无法覆盖用户B的文件
                String projectDirName = "vue_project_" + appId;
                Path projectRoot = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, projectDirName);
                path = projectRoot.resolve(relativeFilePath);
            }

            // --- 目录自动创建 ---
            // 如果文件在子目录中（如 a/b/c.txt），先创建 a/b 目录
            Path parentDir = path.getParent();
            if (parentDir != null) {
                Files.createDirectories(parentDir);
            }

            // --- 执行写入 ---
            // CREATE: 不存在则创建
            // TRUNCATE_EXISTING: 存在则清空重写（覆盖模式）
            Files.write(path, content.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            log.info("成功写入文件: {}", path.toAbsolutePath());

            // 【重要】反馈给 AI 的信息
            // 我们只告诉 AI "相对路径" 写入成功，不暴露服务器绝对路径，防止信息泄露
            return "文件写入成功: " + relativeFilePath;

        } catch (IOException e) {
            // 异常处理：如果磁盘满了或权限不足，将错误信息返回给 AI
            // AI 收到这个错误后，可能会尝试修复或告诉用户“保存失败”
            String errorMessage = "文件写入失败: " + relativeFilePath + ", 错误: " + e.getMessage();
            log.error(errorMessage, e);
            return errorMessage;
        }
    }

    // --- 以下是 BaseTool 的抽象方法实现，用于前端展示或日志记录 ---

    @Override
    public String getToolName() {
        return "writeFile";
    }

    @Override
    public String getDisplayName() {
        return "写入文件";
    }

    /**
     * 生成工具执行后的 Markdown 格式日志
     * 这个方法通常用于在聊天界面直接展示“AI 修改了什么代码”，而不需要 AI 再把代码复述一遍
     */
    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        String relativeFilePath = arguments.getStr("relativeFilePath");
        String suffix = FileUtil.getSuffix(relativeFilePath); // 获取后缀，用于 Markdown 语法高亮
        String content = arguments.getStr("content");

        // 返回一段 Markdown 代码块，前端渲染时会显示为代码片段
        return String.format("""
                        [工具调用] %s %s
                        ```%s
                        %s
                        ```
                        """, getDisplayName(), relativeFilePath, suffix, content);
    }
}