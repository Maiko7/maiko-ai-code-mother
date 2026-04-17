package com.maiko.maikoaicodemother.langgraph4j;

import cn.hutool.core.thread.ExecutorBuilder;
import cn.hutool.core.thread.ThreadFactoryBuilder;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;

/**
 * 并发执行的代码生成工作流
 * <p>
 * 该工作流引入了并行处理机制，主要用于图片收集阶段。
 * 通过同时搜索不同类型的图片（内容图、插画、图表、Logo），显著缩短了整个流水线的执行时间。
 * </p>
 *
 * @author Maiko7
 */
@Slf4j
public class CodeGenConcurrentWorkflow {

    /**
     * 创建并发工作流图
     * <p>
     * 定义了“分叉-并行执行-汇聚”的菱形结构：
     * 1. 先制定图片计划。
     * 2. 根据计划同时启动多个收集任务。
     * 3. 等待所有任务完成后汇总。
     * </p>
     *
     * @return 编译后的图实例 {@link CompiledGraph}
     */
    public CompiledGraph<MessagesState<String>> createWorkflow() {
        try {
            return new MessagesStateGraph<String>()
                    // --- 1. 注册节点 ---

                    // 核心流程节点
                    .addNode("image_plan", ImagePlanNode.create())       // 制定图片收集计划
                    .addNode("prompt_enhancer", PromptEnhancerNode.create()) // 增强提示词
                    .addNode("router", RouterNode.create())              // 智能路由（选择技术栈）
                    .addNode("code_generator", CodeGeneratorNode.create())   // 生成代码
                    .addNode("code_quality_check", CodeQualityCheckNode.create()) // 代码质检
                    .addNode("project_builder", ProjectBuilderNode.create())   // 项目构建

                    // 并行图片收集节点
                    .addNode("content_image_collector", ContentImageCollectorNode.create()) // 收集内容图
                    .addNode("illustration_collector", IllustrationCollectorNode.create())  // 收集插画
                    .addNode("diagram_collector", DiagramCollectorNode.create())            // 生成架构图
                    .addNode("logo_collector", LogoCollectorNode.create())                  // 生成Logo
                    .addNode("image_aggregator", ImageAggregatorNode.create())              // 聚合所有图片结果

                    // --- 2. 定义边与流转逻辑 ---

                    // 起点 -> 计划节点
                    .addEdge(START, "image_plan")

                    // 【分叉】从计划节点分发到各个并行的收集节点
                    // 这些节点会几乎同时开始执行
                    .addEdge("image_plan", "content_image_collector")
                    .addEdge("image_plan", "illustration_collector")
                    .addEdge("image_plan", "diagram_collector")
                    .addEdge("image_plan", "logo_collector")

                    // 【汇聚】所有收集节点都指向聚合器
                    // 注意：LangGraph 默认会等待上游所有分支完成后再触发下游节点
                    .addEdge("content_image_collector", "image_aggregator")
                    .addEdge("illustration_collector", "image_aggregator")
                    .addEdge("diagram_collector", "image_aggregator")
                    .addEdge("logo_collector", "image_aggregator")

                    // 聚合完成 -> 继续串行主流程
                    .addEdge("image_aggregator", "prompt_enhancer")
                    .addEdge("prompt_enhancer", "router")
                    .addEdge("router", "code_generator")
                    .addEdge("code_generator", "code_quality_check")

                    // 【条件路由】根据质检结果决定下一步走向
                    .addConditionalEdges("code_quality_check",
                            edge_async(this::routeAfterQualityCheck),
                            Map.of(
                                    "build", "project_builder",    // 质检通过且是Vue项目 -> 构建
                                    "skip_build", END,            // 质检通过且是静态页 -> 结束
                                    "fail", "code_generator"      // 质检失败 -> 回退到生成节点重试
                            ))

                    .addEdge("project_builder", END)

                    // 编译图
                    .compile();
        } catch (GraphStateException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "并发工作流创建失败");
        }
    }

    /**
     * 执行并发工作流
     * <p>
     * 配置了自定义线程池以支持真正的并行执行，并流式输出每一步的状态。
     * </p>
     *
     * @param originalPrompt 用户原始提示词
     * @return 最终的上下文状态
     */
    public WorkflowContext executeWorkflow(String originalPrompt) {
        // 1. 创建工作流实例
        CompiledGraph<MessagesState<String>> workflow = createWorkflow();

        // 2. 初始化上下文
        WorkflowContext initialContext = WorkflowContext.builder()
                .originalPrompt(originalPrompt)
                .currentStep("初始化")
                .build();

        // 3. 打印工作流图结构（Mermaid格式）
        GraphRepresentation graph = workflow.getGraph(GraphRepresentation.Type.MERMAID);
        log.info("并发工作流图:\n{}", graph.content());
        log.info("开始执行并发代码生成工作流");

        WorkflowContext finalContext = null;
        int stepCounter = 1;

        // 4. 配置并发执行环境
        // 使用 Hutool 构建一个固定大小的线程池，专门用于处理图片收集的并行任务
        ExecutorService pool = ExecutorBuilder.create()
                .setCorePoolSize(10)
                .setMaxPoolSize(20)
                .setWorkQueue(new LinkedBlockingQueue<>(100))
                .setThreadFactory(ThreadFactoryBuilder.create().setNamePrefix("Parallel-Image-Collect").build())
                .build();

        // 将线程池绑定到特定的节点（这里是 image_plan 及其后续分支）
        RunnableConfig runnableConfig = RunnableConfig.builder()
                .addParallelNodeExecutor("image_plan", pool)
                .build();

        // 5. 流式执行并监控进度
        for (NodeOutput<MessagesState<String>> step : workflow.stream(
                Map.of(WorkflowContext.WORKFLOW_CONTEXT_KEY, initialContext),
                runnableConfig)) {

            log.info("--- 第 {} 步完成 ---", stepCounter);
            WorkflowContext currentContext = WorkflowContext.getContext(step.state());
            if (currentContext != null) {
                finalContext = currentContext;
                log.info("当前步骤上下文: {}", currentContext);
            }
            stepCounter++;
        }

        log.info("并发代码生成工作流执行完成！");
        return finalContext;
    }

    /**
     * 路由函数：根据质检结果决定下一步路径
     * <p>
     * 这是一个条件边（Conditional Edge）的判断逻辑。
     * </p>
     *
     * @param state 当前状态
     * @return 路由键 ("build", "skip_build", 或 "fail")
     */
    private String routeAfterQualityCheck(MessagesState<String> state) {
        WorkflowContext context = WorkflowContext.getContext(state);
        QualityResult qualityResult = context.getQualityResult();

        // 情况1：质检失败（或为空），返回 "fail"，触发重试循环
        if (qualityResult == null || !qualityResult.getIsValid()) {
            log.error("代码质检失败，需要重新生成代码");
            return "fail";
        }

        // 情况2：质检通过，根据项目类型决定是否需要构建
        log.info("代码质检通过，继续后续流程");
        CodeGenTypeEnum generationType = context.getGenerationType();

        if (generationType == CodeGenTypeEnum.VUE_PROJECT) {
            return "build"; // Vue项目需要 npm build
        } else {
            return "skip_build"; // 静态HTML直接结束
        }
    }
}