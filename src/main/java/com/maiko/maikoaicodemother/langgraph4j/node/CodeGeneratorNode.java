package com.maiko.maikoaicodemother.langgraph4j.node;

import com.maiko.maikoaicodemother.constant.AppConstant;
import com.maiko.maikoaicodemother.core.AiCodeGeneratorFacade;
import com.maiko.maikoaicodemother.langgraph4j.model.QualityResult;
import com.maiko.maikoaicodemother.langgraph4j.state.WorkflowContext;
import com.maiko.maikoaicodemother.model.enums.CodeGenTypeEnum;
import com.maiko.maikoaicodemother.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import reactor.core.publisher.Flux;

import java.time.Duration;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 网站代码生成节点
 * <p>
 * 该节点负责调用 AI 服务生成网站代码，支持流式输出，
 * 并具备根据质量检查结果进行错误修复的闭环能力。
 * </p>
 *
 * @author Maiko7
 */
@Slf4j
public class CodeGeneratorNode {

    /**
     * 创建代码生成节点的异步动作
     *
     * @return 异步节点动作 {@link AsyncNodeAction}
     */
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            // 1. 获取当前工作流上下文
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 代码生成");

            // 2. 构造用户消息（包含原始提示词或错误修复指令）
            String userMessage = buildUserMessage(context);
            CodeGenTypeEnum generationType = context.getGenerationType();

            // 3. 获取 AI 代码生成外观服务
            AiCodeGeneratorFacade codeGeneratorFacade = SpringContextUtil.getBean(AiCodeGeneratorFacade.class);
            log.info("开始生成代码，类型: {} ({})", generationType.getValue(), generationType.getText());

            // 4. 先使用固定的 appId (后续再整合到业务中)
            Long appId = 0L;

            // 5. 调用流式代码生成接口
            Flux<String> codeStream = codeGeneratorFacade.generateAndSaveCodeStream(userMessage, generationType, appId);

            // 6. 同步等待流式输出完成（阻塞当前线程直到流结束）
            // 设置超时时间为 10 分钟，防止长时间无响应
            codeStream.blockLast(Duration.ofMinutes(10));

            // 7. 根据类型设置生成目录路径
            String generatedCodeDir = String.format("%s/%s_%s", AppConstant.CODE_OUTPUT_ROOT_DIR, generationType.getValue(), appId);
            log.info("AI 代码生成完成，生成目录: {}", generatedCodeDir);

            // 8. 更新上下文状态
            context.setCurrentStep("代码生成");
            context.setGeneratedCodeDir(generatedCodeDir);

            // 9. 保存并返回更新后的上下文
            return WorkflowContext.saveContext(context);
        });
    }

    /**
     * 构造用户消息
     * <p>
     * 如果存在质检失败结果，则构造错误修复提示词；否则使用增强后的原始提示词。
     * </p>
     *
     * @param context 工作流上下文
     * @return 最终发送给 AI 的用户消息
     */
    private static String buildUserMessage(WorkflowContext context) {
        String userMessage = context.getEnhancedPrompt();

        // 检查是否存在质检失败结果
        QualityResult qualityResult = context.getQualityResult();
        if (isQualityCheckFailed(qualityResult)) {
            // 直接将错误修复信息作为新的提示词（覆盖原有提示词，实现纠错循环）
            userMessage = buildErrorFixPrompt(qualityResult);
        }
        return userMessage;
    }

    /**
     * 判断质检是否失败
     *
     * @param qualityResult 质检结果
     * @return true 表示质检失败，需要修复
     */
    private static boolean isQualityCheckFailed(QualityResult qualityResult) {
        return qualityResult != null &&
                !qualityResult.getIsValid() &&
                qualityResult.getErrors() != null &&
                !qualityResult.getErrors().isEmpty();
    }

    /**
     * 构造错误修复提示词
     * <p>
     * 将具体的错误信息和修复建议格式化为 Prompt，指导 AI 重新生成正确的代码。
     * </p>
     *
     * @param qualityResult 包含错误信息的质检结果
     * @return 格式化后的修复提示词
     */
    private static String buildErrorFixPrompt(QualityResult qualityResult) {
        StringBuilder errorInfo = new StringBuilder();
        errorInfo.append("\n\n## 上次生成的代码存在以下问题，请修复：\n");

        // 添加错误列表
        qualityResult.getErrors().forEach(error ->
                errorInfo.append("- ").append(error).append("\n"));

        // 添加修复建议（如果有）
        if (qualityResult.getSuggestions() != null && !qualityResult.getSuggestions().isEmpty()) {
            errorInfo.append("\n## 修复建议：\n");
            qualityResult.getSuggestions().forEach(suggestion ->
                    errorInfo.append("- ").append(suggestion).append("\n"));
        }

        errorInfo.append("\n请根据上述问题和建议重新生成代码，确保修复所有提到的问题。");
        return errorInfo.toString();
    }
}