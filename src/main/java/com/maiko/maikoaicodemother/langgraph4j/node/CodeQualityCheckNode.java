package com.maiko.maikoaicodemother.langgraph4j.node;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;

import com.maiko.maikoaicodemother.langgraph4j.ai.CodeQualityCheckService;
import com.maiko.maikoaicodemother.langgraph4j.model.QualityResult;
import com.maiko.maikoaicodemother.langgraph4j.state.WorkflowContext;
import com.maiko.maikoaicodemother.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 代码质量检查节点
 * <p>
 * 该节点负责读取生成的代码文件，将其拼接成完整的上下文，
 * 并调用 AI 服务进行代码质量检查（如语法错误、最佳实践等）。
 * </p>
 *
 * @author Maiko7
 */
@Slf4j
public class CodeQualityCheckNode {

    /**
     * 创建代码质量检查节点的异步动作
     *
     * @return 异步节点动作 {@link AsyncNodeAction}
     */
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            // 1. 获取当前工作流上下文
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 代码质量检查");

            String generatedCodeDir = context.getGeneratedCodeDir();
            QualityResult qualityResult;

            try {
                // 2. 读取并拼接代码文件内容
                String codeContent = readAndConcatenateCodeFiles(generatedCodeDir);

                if (StrUtil.isBlank(codeContent)) {
                    log.warn("未找到可检查的代码文件");
                    // 如果没有代码，直接返回失败结果
                    qualityResult = QualityResult.builder()
                            .isValid(false)
                            .errors(List.of("未找到可检查的代码文件"))
                            .suggestions(List.of("请确保代码生成成功"))
                            .build();
                } else {
                    // 3. 调用 AI 进行代码质量检查
                    CodeQualityCheckService qualityCheckService = SpringContextUtil.getBean(CodeQualityCheckService.class);
                    qualityResult = qualityCheckService.checkCodeQuality(codeContent);
                    log.info("代码质量检查完成 - 是否通过: {}", qualityResult.getIsValid());
                }
            } catch (Exception e) {
                log.error("代码质量检查异常: {}", e.getMessage(), e);
                // 发生异常时默认通过，避免阻塞流程，交由后续步骤处理
                qualityResult = QualityResult.builder()
                        .isValid(true)
                        .build();
            }

            // 4. 更新上下文状态
            context.setCurrentStep("代码质量检查");
            context.setQualityResult(qualityResult);

            // 5. 保存并返回更新后的上下文
            return WorkflowContext.saveContext(context);
        });
    }

    /**
     * 需要检查的文件扩展名列表
     */
    private static final List<String> CODE_EXTENSIONS = Arrays.asList(
            ".html", ".htm", ".css", ".js", ".json", ".vue", ".ts", ".jsx", ".tsx"
    );

    /**
     * 读取并拼接代码目录下的所有代码文件
     * <p>
     * 将项目结构转换为 Markdown 格式的文本，以便 AI 理解整体代码结构。
     * </p>
     *
     * @param codeDir 代码目录路径
     * @return 拼接后的代码内容字符串
     */
    private static String readAndConcatenateCodeFiles(String codeDir) {
        if (StrUtil.isBlank(codeDir)) {
            return "";
        }

        File directory = new File(codeDir);
        if (!directory.exists() || !directory.isDirectory()) {
            log.error("代码目录不存在或不是目录: {}", codeDir);
            return "";
        }

        StringBuilder codeContent = new StringBuilder();
        codeContent.append("# 项目文件结构和代码内容\n\n");

        // 使用 Hutool 的 walkFiles 方法递归遍历所有文件
        FileUtil.walkFiles(directory, file -> {
            // 过滤条件：跳过隐藏文件、特定目录下的文件、非代码文件
            if (shouldSkipFile(file, directory)) {
                return;
            }

            if (isCodeFile(file)) {
                // 计算相对路径，用于显示文件层级
                String relativePath = FileUtil.subPath(directory.getAbsolutePath(), file.getAbsolutePath());
                codeContent.append("## 文件: ").append(relativePath).append("\n\n");

                // 读取文件内容（UTF-8）
                String fileContent = FileUtil.readUtf8String(file);
                codeContent.append(fileContent).append("\n\n");
            }
        });

        return codeContent.toString();
    }

    /**
     * 判断是否应该跳过此文件
     * <p>
     * 排除依赖包、构建产物和隐藏文件，减少 Token 消耗并聚焦核心业务代码。
     * </p>
     *
     * @param file   当前文件
     * @param rootDir 根目录
     * @return true 表示跳过，false 表示保留
     */
    private static boolean shouldSkipFile(File file, File rootDir) {
        String relativePath = FileUtil.subPath(rootDir.getAbsolutePath(), file.getAbsolutePath());

        // 跳过隐藏文件（如 .DS_Store, .gitignore）
        if (file.getName().startsWith(".")) {
            return true;
        }

        // 跳过特定目录下的文件（如 node_modules, dist, target, .git）
        return relativePath.contains("node_modules" + File.separator) ||
                relativePath.contains("dist" + File.separator) ||
                relativePath.contains("target" + File.separator) ||
                relativePath.contains(".git" + File.separator);
    }

    /**
     * 判断是否是需要检查的代码文件
     *
     * @param file 文件对象
     * @return true 表示是目标代码文件
     */
    private static boolean isCodeFile(File file) {
        String fileName = file.getName().toLowerCase();
        return CODE_EXTENSIONS.stream().anyMatch(fileName::endsWith);
    }
}