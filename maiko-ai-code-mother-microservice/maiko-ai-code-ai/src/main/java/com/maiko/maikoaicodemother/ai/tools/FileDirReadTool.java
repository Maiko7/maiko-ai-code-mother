package com.maiko.maikoaicodemother.ai.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.maiko.maikoaicodemother.constant.AppConstant;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

/**
 * 文件目录读取工具
 * <p>
 * 使用 Hutool 简化文件操作，用于读取并展示指定目录下的文件树结构。
 * 包含对 node_modules、.git 等无关目录的过滤逻辑。
 */
@Slf4j
@Component
public class FileDirReadTool extends BaseTool {

    /**
     * 需要忽略的文件和目录名称集合
     * 包含常见的构建产物、版本控制目录和 IDE 配置文件
     */
    private static final Set<String> IGNORED_NAMES = Set.of(
            "node_modules", ".git", "dist", "build", ".DS_Store",
            ".env", "target", ".mvn", ".idea", ".vscode", "coverage"
    );

    /**
     * 需要忽略的文件扩展名集合
     * 包含日志、临时文件和缓存文件
     */
    private static final Set<String> IGNORED_EXTENSIONS = Set.of(
            ".log", ".tmp", ".cache", ".lock"
    );

    /**
     * 读取目录结构工具方法
     *
     * @param relativeDirPath 目录的相对路径，为空则读取整个项目结构
     * @param appId           应用ID，用于定位项目根目录
     * @return 格式化的目录结构字符串
     */
    @Tool("读取目录结构，获取指定目录下的所有文件和子目录信息")
    public String readDir(
            @P("目录的相对路径，为空则读取整个项目结构")
            String relativeDirPath,
            @ToolMemoryId Long appId
    ) {
        try {
            // 1. 解析路径
            Path path = Paths.get(relativeDirPath == null ? "" : relativeDirPath);
            if (!path.isAbsolute()) {
                String projectDirName = "vue_project_" + appId;
                Path projectRoot = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, projectDirName);
                path = projectRoot.resolve(relativeDirPath == null ? "" : relativeDirPath);
            }

            File targetDir = path.toFile();
            if (!targetDir.exists() || !targetDir.isDirectory()) {
                return "错误：目录不存在或不是目录 - " + relativeDirPath;
            }

            StringBuilder structure = new StringBuilder();
            structure.append("项目目录结构:\n");

            // 2. 使用 Hutool 递归获取所有文件，并应用过滤规则
            List<File> allFiles = FileUtil.loopFiles(targetDir, file -> !shouldIgnore(file.getName()));

            // 3. 排序：按层级深度优先，同层级按名称排序
            allFiles.stream()
                    .sorted((f1, f2) -> {
                        int depth1 = getRelativeDepth(targetDir, f1);
                        int depth2 = getRelativeDepth(targetDir, f2);
                        if (depth1 != depth2) {
                            return Integer.compare(depth1, depth2);
                        }
                        return f1.getPath().compareTo(f2.getPath());
                    })
                    .forEach(file -> {
                        int depth = getRelativeDepth(targetDir, file);
                        String indent = "  ".repeat(depth); // 使用空格进行缩进
                        structure.append(indent).append(file.getName()).append("\n");
                    });

            return structure.toString();
        } catch (Exception e) {
            String errorMessage = "读取目录结构失败: " + relativeDirPath + ", 错误: " + e.getMessage();
            log.error(errorMessage, e);
            return errorMessage;
        }
    }

    /**
     * 计算文件相对于根目录的层级深度
     *
     * @param root 根目录
     * @param file 目标文件
     * @return 深度数值（根目录下的文件深度为 0）
     */
    private int getRelativeDepth(File root, File file) {
        Path rootPath = root.toPath();
        Path filePath = file.toPath();
        return rootPath.relativize(filePath).getNameCount() - 1;
    }

    /**
     * 判断是否应该忽略该文件或目录
     *
     * @param fileName 文件或目录名称
     * @return true 表示忽略，false 表示保留
     */
    private boolean shouldIgnore(String fileName) {
        // 检查是否在忽略名称列表中
        if (IGNORED_NAMES.contains(fileName)) {
            return true;
        }
        // 检查文件扩展名
        return IGNORED_EXTENSIONS.stream().anyMatch(fileName::endsWith);
    }

    @Override
    public String getToolName() {
        return "readDir";
    }

    @Override
    public String getDisplayName() {
        return "读取目录";
    }

    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        String relativeDirPath = arguments.getStr("relativeDirPath");
        if (StrUtil.isEmpty(relativeDirPath)) {
            relativeDirPath = "根目录";
        }
        return String.format("[工具调用] %s %s", getDisplayName(), relativeDirPath);
    }
}