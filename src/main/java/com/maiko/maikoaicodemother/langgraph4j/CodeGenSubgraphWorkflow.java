package com.maiko.maikoaicodemother.langgraph4j;

import com.maiko.maikoaicodemother.exception.BusinessException;
import com.maiko.maikoaicodemother.exception.ErrorCode;
import com.maiko.maikoaicodemother.langgraph4j.model.QualityResult;
import com.maiko.maikoaicodemother.langgraph4j.node.*;
import com.maiko.maikoaicodemother.langgraph4j.node.concurrent.*;
import com.maiko.maikoaicodemother.langgraph4j.state.WorkflowContext;
import com.maiko.maikoaicodemother.model.enums.CodeGenTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.*;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.prebuilt.MessagesStateGraph;

import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;

/**
 * 基于子图（Subgraph）架构的代码生成工作流
 * <p>
 * 该版本引入了“子图”概念，将复杂的并行任务（如图片收集）封装成独立的微型工作流。
 * 这样做的好处是：
 * 1. **模块化**：每个收集任务（内容图、插画等）都是独立的黑盒。
 * 2. **可维护性**：修改某个子图不影响主流程。
 * 3. **复用性**：子图可以在其他工作流中被重复使用。
 * </p>
 *
 * @author Maiko7
 */
@Slf4j
public class CodeGenSubgraphWorkflow {

    /**
     * 创建内容图片收集子图
     * <p>
     * 这是一个微型工作流，只负责一件事：收集内容图片。
     * 它有自己的起点和终点，对外界隐藏了内部细节。
     * </p>
     */
    private StateGraph<MessagesState<String>> createContentImageSubgraph() {
        try {
            return new MessagesStateGraph<String>()
                    .addNode("content_collect", ContentImageCollectorNode.create())
                    .addEdge(START, "content_collect")
                    .addEdge("content_collect", END);
        } catch (GraphStateException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "内容图片子图创建失败");
        }
    }

    /**
     * 创建插画收集子图
     */
    private StateGraph<MessagesState<String>> createIllustrationSubgraph() {
        try {
            return new MessagesStateGraph<String>()
                    .addNode("illustration_collect", IllustrationCollectorNode.create())
                    .addEdge(START, "illustration_collect")
                    .addEdge("illustration_collect", END);
        } catch (GraphStateException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "插画子图创建失败");
        }
    }

    /**
     * 创建架构图生成子图
     */
    private StateGraph<MessagesState<String>> createDiagramSubgraph() {
        try {
            return new MessagesStateGraph<String>()
                    .addNode("diagram_generate", DiagramCollectorNode.create())
                    .addEdge(START, "diagram_generate")
                    .addEdge("diagram_generate", END);
        } catch (GraphStateException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "架构图子图创建失败");
        }
    }

    /**
     * 创建Logo生成子图
     */
    private StateGraph<MessagesState<String>> createLogoSubgraph() {
        try {
            return new MessagesStateGraph<String>()
                    .addNode("logo_generate", LogoCollectorNode.create())
                    .addEdge(START, "logo_generate")
                    .addEdge("logo_generate", END);
        } catch (GraphStateException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "Logo子图创建失败");
        }
    }

    /**
     * 创建主工作流
     * <p>
     * 在这里，我们将之前定义的“子图”当作普通的“节点”添加到主图中。
     * LangGraph 会自动处理子图与主图之间的状态共享。
     * </p>
     */
    public CompiledGraph<MessagesState<String>> createWorkflow() {
        try {
            // 1. 实例化各个未编译的子图
            // 这些子图将与父图完全共享同一个 WorkflowContext 状态
            StateGraph<MessagesState<String>> contentImageSubgraph = createContentImageSubgraph();
            StateGraph<MessagesState<String>> illustrationSubgraph = createIllustrationSubgraph();
            StateGraph<MessagesState<String>> diagramSubgraph = createDiagramSubgraph();
            StateGraph<MessagesState<String>> logoSubgraph = createLogoSubgraph();

            return new MessagesStateGraph<String>()
                    // --- 2. 添加常规节点 ---
                    .addNode("image_plan", ImagePlanNode.create())
                    .addNode("prompt_enhancer", PromptEnhancerNode.create())
                    .addNode("router", RouterNode.create())
                    .addNode("code_generator", CodeGeneratorNode.create())
                    .addNode("code_quality_check", CodeQualityCheckNode.create())
                    .addNode("project_builder", ProjectBuilderNode.create())

                    // --- 3. 添加子图节点 ---
                    // 这里的 .addNode 接收的是一个 StateGraph 对象，而不是一个简单的 Action
                    // 这意味着执行到这个节点时，会进入子图内部跑完它的流程
                    .addNode("content_image_subgraph", contentImageSubgraph)
                    .addNode("illustration_subgraph", illustrationSubgraph)
                    .addNode("diagram_subgraph", diagramSubgraph)
                    .addNode("logo_subgraph", logoSubgraph)

                    // --- 4. 添加聚合节点 ---
                    .addNode("image_aggregator", ImageAggregatorNode.create())

                    // --- 5. 定义边与流转逻辑 ---
                    .addEdge(START, "image_plan")

                    // 【并行分叉】从计划节点分发到各个子图
                    .addEdge("image_plan", "content_image_subgraph")
                    .addEdge("image_plan", "illustration_subgraph")
                    .addEdge("image_plan", "diagram_subgraph")
                    .addEdge("image_plan", "logo_subgraph")

                    // 【汇聚】所有子图完成后，汇聚到聚合器
                    .addEdge("content_image_subgraph", "image_aggregator")
                    .addEdge("illustration_subgraph", "image_aggregator")
                    .addEdge("diagram_subgraph", "image_aggregator")
                    .addEdge("logo_subgraph", "image_aggregator")

                    // 继续串行主流程
                    .addEdge("image_aggregator", "prompt_enhancer")
                    .addEdge("prompt_enhancer", "router")
                    .addEdge("router", "code_generator")
                    .addEdge("code_generator", "code_quality_check")

                    // 【条件路由】根据质检结果决定下一步
                    .addConditionalEdges("code_quality_check",
                            edge_async(this::routeAfterQualityCheck),
                            Map.of(
                                    "build", "project_builder",   // 通过且需构建 -> 去构建
                                    "skip_build", END,           // 通过且无需构建 -> 结束
                                    "fail", "code_generator"     // 失败 -> 回去重做
                            ))
                    .addEdge("project_builder", END)

                    .compile();
        } catch (GraphStateException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "子图工作流创建失败");
        }
    }

    /**
     * 路由函数：根据质检结果决定下一步路径
     *
     * @param state 当前状态
     * @return 路由键 ("build", "skip_build", 或 "fail")
     */
    private String routeAfterQualityCheck(MessagesState<String> state) {
        WorkflowContext context = WorkflowContext.getContext(state);
        QualityResult qualityResult = context.getQualityResult();

        if (qualityResult == null || !qualityResult.getIsValid()) {
            log.error("代码质检失败，需要重新生成代码");
            return "fail";
        }

        log.info("代码质检通过，继续后续流程");
        CodeGenTypeEnum generationType = context.getGenerationType();
        if (generationType == CodeGenTypeEnum.VUE_PROJECT) {
            return "build";
        } else {
            return "skip_build";
        }
    }

    /**
     * 执行子图工作流
     *
     * @param originalPrompt 用户原始提示词
     * @return 最终的上下文状态
     */
    public WorkflowContext executeWorkflow(String originalPrompt) {
        // 创建工作流实例
        CompiledGraph<MessagesState<String>> workflow = createWorkflow();

        // 初始化上下文
        WorkflowContext initialContext = WorkflowContext.builder()
                .originalPrompt(originalPrompt)
                .currentStep("初始化")
                .build();

        // 打印工作流图结构
        GraphRepresentation graph = workflow.getGraph(GraphRepresentation.Type.MERMAID);
        log.info("子图工作流图:\n{}", graph.content());
        log.info("开始执行子图代码生成工作流");

        WorkflowContext finalContext = null;
        int stepCounter = 1;

        // 流式执行并监控进度
        for (NodeOutput<MessagesState<String>> step : workflow.stream(
                Map.of(WorkflowContext.WORKFLOW_CONTEXT_KEY, initialContext))) {
            log.info("--- 第 {} 步完成 ---", stepCounter);
            WorkflowContext currentContext = WorkflowContext.getContext(step.state());
            if (currentContext != null) {
                finalContext = currentContext;
                log.info("当前步骤上下文: {}", currentContext);
            }
            stepCounter++;
        }
        log.info("子图代码生成工作流执行完成！");
        return finalContext;
    }
}