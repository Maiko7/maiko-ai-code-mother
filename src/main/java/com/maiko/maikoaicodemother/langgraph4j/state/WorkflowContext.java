package com.maiko.maikoaicodemother.langgraph4j.state;

import com.maiko.maikoaicodemother.langgraph4j.model.ImageCollectionPlan;
import com.maiko.maikoaicodemother.langgraph4j.model.ImageResource;
import com.maiko.maikoaicodemother.langgraph4j.model.QualityResult;
import com.maiko.maikoaicodemother.model.enums.CodeGenTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 工作流上下文 (Workflow Context)
 * <p>
 * 该对象用于在整个 LangGraph 工作流中持久化和传递状态数据。
 * 它被封装在 {@link MessagesState} 中，作为各个节点 (Node) 之间共享数据的载体。
 * </p>
 *
 * @author Maiko7
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowContext implements Serializable {

    /**
     * WorkflowContext 在 MessagesState 中的存储key
     */
    public static final String WORKFLOW_CONTEXT_KEY = "workflowContext";

    /**
     * 当前执行步骤
     * <p>
     * 用于记录工作流当前所处的节点名称，便于调试和状态追踪。
     * </p>
     */
    private String currentStep;

    /**
     * 用户原始输入的提示词
     * <p>
     * 存储用户最初提交的需求描述，作为整个流程的起点。
     * </p>
     */
    private String originalPrompt;

    /**
     * 图片资源字符串
     * <p>
     * 图片资源的字符串形式（如JSON或逗号分隔），用于中间传输。
     * </p>
     */
    private String imageListStr;

    /**
     * 图片资源列表
     * <p>
     * 解析后的图片资源对象列表，包含图片URL、描述等详细信息。
     * </p>
     */
    private List<ImageResource> imageList;

    /**
     * 增强后的提示词
     * <p>
     * 经过 Prompt Enhancer 节点优化、补充细节后的提示词，用于后续的代码生成。
     * </p>
     */
    private String enhancedPrompt;

    /**
     * 代码生成类型
     * <p>
     * 指定生成代码的技术栈或框架类型（例如：React, Vue, Spring Boot等）。
     * </p>
     */
    private CodeGenTypeEnum generationType;

    /**
     * 生成的代码目录
     * <p>
     * 代码生成器输出的文件存放路径。
     * </p>
     */
    private String generatedCodeDir;

    /**
     * 构建成功的目录
     * <p>
     * 项目经过构建工具（如 Maven/Gradle/Webpack）处理后的输出路径。
     * </p>
     */
    private String buildResultDir;

    /**
     * 质量检查结果
     * <p>
     * 存储代码质量检查节点（Code Quality Check）返回的评估报告。
     * </p>
     */
    private QualityResult qualityResult;

    /**
     * 错误信息
     * <p>
     * 如果工作流执行过程中发生异常，此处记录具体的错误堆栈或提示信息。
     * </p>
     */
    private String errorMessage;

    /**
     * 图片收集计划
     * <p>
     * 定义了需要收集哪些类型的图片以及相应的策略。
     * </p>
     */
    private ImageCollectionPlan imageCollectionPlan;

    /**
     * 并发图片收集的中间结果字段
     * <p>
     * 以下字段用于在并发执行图片收集任务时，分别存储不同类别的图片结果，
     * 最后会合并到主 imageList 中。
     * </p>
     */
    private List<ImageResource> contentImages;
    private List<ImageResource> illustrations;
    private List<ImageResource> diagrams;
    private List<ImageResource> logos;

    @Serial
    private static final long serialVersionUID = 1L;

    // ========== 上下文操作方法 ==========

    /**
     * 从 MessagesState 中获取 WorkflowContext
     *
     * @param state 包含状态数据的 MessagesState 对象
     * @return WorkflowContext 实例，如果不存在则返回 null
     */
    public static WorkflowContext getContext(MessagesState<String> state) {
        return (WorkflowContext) state.data().get(WORKFLOW_CONTEXT_KEY);
    }

    /**
     * 将 WorkflowContext 保存到 MessagesState 中
     * <p>
     * 返回一个包含上下文对象的 Map，用于在节点执行后更新状态。
     * </p>
     *
     * @param context 需要保存的 WorkflowContext 实例
     * @return 包含键值对的状态更新 Map
     */
    public static Map<String, Object> saveContext(WorkflowContext context) {
        return Map.of(WORKFLOW_CONTEXT_KEY, context);
    }
}