package com.maiko.maikoaicodemother.langgraph4j;

import com.maiko.maikoaicodemother.langgraph4j.node.*;
import com.maiko.maikoaicodemother.langgraph4j.state.WorkflowContext;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.prebuilt.MessagesStateGraph;

import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;

/**
 * 工作流应用（模拟状态流转）
 * <p>
 * 这是一个简单的线性工作流演示，用于测试各个节点的基本功能。
 * 它不包含复杂的并行逻辑或条件分支，适合用于调试单个节点的输入输出。
 *
 * 和SimpleWorkflowApp区别就是：创建节点改成了ImageCollectorNode.create()，实现了代码的关注点分离。
 * </p>
 *
 * @author Maiko7
 */
@Slf4j
public class WorkflowApp {

    /**
     * 主方法：构建并运行工作流
     *
     * @param args 命令行参数
     * @throws GraphStateException 图构建异常
     */
    public static void main(String[] args) throws GraphStateException {
        // --- 1. 构建工作流图 ---

        CompiledGraph<MessagesState<String>> workflow = new MessagesStateGraph<String>()
                // 注册节点：按顺序添加各个处理步骤
                .addNode("image_collector", ImageCollectorNode.create())   // 图片收集
                .addNode("prompt_enhancer", PromptEnhancerNode.create())   // 提示词增强
                .addNode("router", RouterNode.create())                    // 智能路由
                .addNode("code_generator", CodeGeneratorNode.create())     // 代码生成
                .addNode("project_builder", ProjectBuilderNode.create())   // 项目构建

                // 定义边：连接节点，形成线性链路
                .addEdge(START, "image_collector")                         // 起点 -> 图片收集
                .addEdge("image_collector", "prompt_enhancer")             // 图片收集 -> 提示词增强
                .addEdge("prompt_enhancer", "router")                      // 提示词增强 -> 路由
                .addEdge("router", "code_generator")                       // 路由 -> 代码生成
                .addEdge("code_generator", "project_builder")              // 代码生成 -> 项目构建
                .addEdge("project_builder", END)                           // 项目构建 -> 终点

                // 编译工作流，使其变为可执行状态
                .compile();

        // --- 2. 初始化上下文 ---

        // 创建初始的 WorkflowContext，只设置最基本的信息
        WorkflowContext initialContext = WorkflowContext.builder()
                .originalPrompt("创建一个Maiko7的个人博客网站") // 模拟用户输入
                .currentStep("初始化")
                .build();

        log.info("初始输入: {}", initialContext.getOriginalPrompt());
        log.info("开始执行工作流");

        // --- 3. 可视化工作流结构 ---

        // 获取 Mermaid 格式的图表字符串，方便在日志中查看流程结构
        GraphRepresentation graph = workflow.getGraph(GraphRepresentation.Type.MERMAID);
        log.info("工作流图:\n{}", graph.content());

        // --- 4. 流式执行工作流 ---

        int stepCounter = 1;
        // 使用 stream 方法逐步执行，每执行完一个节点就会返回一个 NodeOutput
        for (NodeOutput<MessagesState<String>> step : workflow.stream(Map.of(WorkflowContext.WORKFLOW_CONTEXT_KEY, initialContext))) {
            log.info("--- 第 {} 步完成 ---", stepCounter);

            // 从当前状态中提取更新后的 WorkflowContext
            WorkflowContext currentContext = WorkflowContext.getContext(step.state());
            if (currentContext != null) {
                log.info("当前步骤上下文: {}", currentContext);
            }
            stepCounter++;
        }

        log.info("工作流执行完成！");
    }
}