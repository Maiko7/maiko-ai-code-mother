package com.maiko.maikoaicodemother.langgraph4j.config;

import com.maiko.maikoaicodemother.langgraph4j.CodeGenWorkflow;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.studio.springboot.AbstractLangGraphStudioConfig;
import org.bsc.langgraph4j.studio.springboot.LangGraphFlow;
import org.springframework.context.annotation.Configuration;

/**
 * LangGraph Studio 可视化调试配置
 * <p>
 * 该配置类用于将当前项目的代码生成工作流（CodeGenWorkflow）注册到
 * LangGraph Studio Spring Boot Starter 中。
 * </p>
 * <p>
 * <b>功能说明：</b>
 * 通过继承 {@link AbstractLangGraphStudioConfig}，Spring 容器启动时会自动加载此配置。
 * 它允许开发者通过可视化的界面（通常是 Web UI）来观察图结构的定义、状态流转以及节点连接关系，
 * 从而辅助进行复杂 Agent 流程的调试和验证。
 * </p>
 *
 * @author Maiko7
 */
@Configuration
public class LangGraphStudioSampleConfig extends AbstractLangGraphStudioConfig {

    /**
     * 定义要暴露给 Studio 的工作流实例
     */
    final LangGraphFlow flow;

    /**
     * 构造函数：初始化工作流图结构
     * <p>
     * 在此处构建实际的 StateGraph，并将其包装进 LangGraphFlow 对象中。
     * </p>
     *
     * @throws GraphStateException 如果图结构构建失败（如节点命名冲突、边连接错误等）则抛出此异常
     */
    public LangGraphStudioSampleConfig() throws GraphStateException {
        // 1. 获取工作流构建器并编译成 StateGraph
        // CodeGenWorkflow 是核心业务逻辑的定义类，这里将其转换为 Studio 可识别的图结构
        var workflow = new CodeGenWorkflow().createWorkflow().stateGraph;

        // define your workflow

        // 2. 构建 Flow 对象
        this.flow = LangGraphFlow.builder()
                .title("LangGraph Studio") // 设置可视化界面的标题
                .stateGraph(workflow)      // 绑定具体的图结构
                .build();
    }

    /**
     * 向 Spring 容器提供配置好的 Flow 实例
     * <p>
     * 这是抽象父类要求的实现方法，框架会通过此方法获取工作流定义以进行展示。
     * </p>
     *
     * @return 包含工作流元数据的 Flow 对象
     */
    @Override
    public LangGraphFlow getFlow() {
        return this.flow;
    }
}