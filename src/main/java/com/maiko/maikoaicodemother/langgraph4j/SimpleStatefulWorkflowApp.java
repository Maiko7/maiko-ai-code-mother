package com.maiko.maikoaicodemother.langgraph4j;

import com.maiko.maikoaicodemother.langgraph4j.state.WorkflowContext;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.prebuilt.MessagesStateGraph;

import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 简化版带状态定义的工作流应用
 * <p>
 * 该示例主要用于演示如何定义状态结构并在工作流节点中传递上下文，
 * 而不涉及复杂的业务逻辑实现。
 * </p>
 *
 * @author Maiko7
 */
@Slf4j
public class SimpleStatefulWorkflowApp {

    /**
     * 创建带状态感知的工作节点
     *
     * @param nodeName 节点名称，用于日志记录和上下文标记
     * @param message  执行时的描述信息
     * @return 异步节点动作 {@link AsyncNodeAction}
     */
    static AsyncNodeAction<MessagesState<String>> makeStatefulNode(String nodeName, String message) {
        return node_async(state -> {
            // 从状态中获取上下文
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: {} - {}", nodeName, message);

            // 更新当前步骤
            if (context != null) {
                context.setCurrentStep(nodeName);
            }

            // 保存回状态
            return WorkflowContext.saveContext(context);
        });
    }

    /**
     * 主程序入口
     *
     * @param args 命令行参数
     * @throws GraphStateException 图构建异常
     */
    public static void main(String[] args) throws GraphStateException {
        // 创建工作流图
        CompiledGraph<MessagesState<String>> workflow = new MessagesStateGraph<String>()
                // 添加节点 - 使用带状态感知的节点
                .addNode("image_collector", makeStatefulNode("image_collector", "获取图片素材"))
                .addNode("prompt_enhancer", makeStatefulNode("prompt_enhancer", "增强提示词"))
                .addNode("router", makeStatefulNode("router", "智能路由选择"))
                .addNode("code_generator", makeStatefulNode("code_generator", "网站代码生成"))
                .addNode("project_builder", makeStatefulNode("project_builder", "项目构建"))

                // 添加边定义流转顺序
                .addEdge(START, "image_collector")
                .addEdge("image_collector", "prompt_enhancer")
                .addEdge("prompt_enhancer", "router")
                .addEdge("router", "code_generator")
                .addEdge("code_generator", "project_builder")
                .addEdge("project_builder", END)

                // 编译工作流
                .compile();

        // 初始化 WorkflowContext - 只设置基本信息
        WorkflowContext initialContext = WorkflowContext.builder()
                .originalPrompt("创建一个Maiko7的个人博客网站")
                .currentStep("初始化")
                .build();

        log.info("初始输入: {}", initialContext.getOriginalPrompt());
        log.info("开始执行工作流");

        // 显示工作流图 (Mermaid 格式)
        GraphRepresentation graph = workflow.getGraph(GraphRepresentation.Type.MERMAID);
        log.info("工作流图:\n{}", graph.content());

        // 执行工作流并流式输出结果
        int stepCounter = 1;
        for (NodeOutput<MessagesState<String>> step : workflow.stream(Map.of(WorkflowContext.WORKFLOW_CONTEXT_KEY, initialContext))) {
            log.info("--- 第 {} 步完成 ---", stepCounter);
            // 显示当前状态
            WorkflowContext currentContext = WorkflowContext.getContext(step.state());
            if (currentContext != null) {
                log.info("当前步骤上下文: {}", currentContext);
            }
            stepCounter++;
        }
        log.info("工作流执行完成！");
    }
}