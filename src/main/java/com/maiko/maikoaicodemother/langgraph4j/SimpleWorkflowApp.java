package com.maiko.maikoaicodemother.langgraph4j;

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
 * 简化版网站生成工作流应用
 * <p>
 * 该示例演示了基于 {@link MessagesState} 的线性工作流构建过程，
 * 涵盖了从图片收集、提示词增强、路由分发到代码生成与构建的完整链路。
 * </p>
 *
 * @author Maiko7
 */
@Slf4j
public class SimpleWorkflowApp {

    /**
     * 创建工作节点的通用工厂方法
     *
     * @param message 节点执行时打印的日志信息
     * @return 异步节点动作 {@link AsyncNodeAction}
     */
    static AsyncNodeAction<MessagesState<String>> makeNode(String message) {
        return node_async(state -> {
            log.info("执行节点: {}", message);
            return Map.of("messages", message);
        });
    }

    /**
     * 主程序入口
     * <p>
     * 1. 构建并编译工作流图<br>
     * 2. 打印 Mermaid 格式的流程图定义<br>
     * 3. 以流式方式执行工作流并输出每一步的状态
     * </p>
     *
     * @param args 命令行参数
     * @throws GraphStateException 图构建或执行异常
     */
    public static void main(String[] args) throws GraphStateException {
        // 创建工作流图
        CompiledGraph<MessagesState<String>> workflow = new MessagesStateGraph<String>()
                // 添加节点
                .addNode("image_collector", makeNode("获取图片素材"))
                .addNode("prompt_enhancer", makeNode("增强提示词"))
                .addNode("router", makeNode("智能路由选择"))
                .addNode("code_generator", makeNode("网站代码生成"))
                .addNode("project_builder", makeNode("项目构建"))

                // 添加边
                .addEdge(START, "image_collector")                // 开始 -> 图片收集
                .addEdge("image_collector", "prompt_enhancer")    // 图片收集 -> 提示词增强
                .addEdge("prompt_enhancer", "router")             // 提示词增强 -> 智能路由
                .addEdge("router", "code_generator")              // 智能路由 -> 代码生成
                .addEdge("code_generator", "project_builder")     // 代码生成 -> 项目构建
                .addEdge("project_builder", END)                  // 项目构建 -> 结束

                // 编译工作流
                .compile();

        log.info("开始执行工作流");

        // 获取并打印工作流图结构 (Mermaid 格式)
        GraphRepresentation graph = workflow.getGraph(GraphRepresentation.Type.MERMAID);
        log.info("工作流图: \n{}", graph.content());

        // 执行工作流
        int stepCounter = 1;
        for (NodeOutput<MessagesState<String>> step : workflow.stream(Map.of())) {
            log.info("--- 第 {} 步完成 ---", stepCounter);
            log.info("步骤输出: {}", step);
            stepCounter++;
        }

        log.info("工作流执行完成！");
    }
}