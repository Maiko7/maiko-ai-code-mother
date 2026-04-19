package com.maiko.maikoaicodemother.ai.model.message;

import dev.langchain4j.service.tool.ToolExecution; // 引入 LangChain4j 的工具执行对象
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 工具执行结果消息
 * 作用：封装工具（如搜索、画图、查数据库）执行后的具体结果。
 * 继承自 StreamMessage，拥有 type 字段。
 */
@Data // Lombok注解：自动生成 Getter/Setter、toString 等方法
@EqualsAndHashCode(callSuper = true) // Lombok注解：生成 equals/hashCode 时包含父类字段
@NoArgsConstructor // Lombok注解：生成无参构造函数
public class ToolExecutedMessage extends StreamMessage {

    /**
     * 工具调用的唯一标识ID
     * 作用：用于追踪这一次具体的工具调用，通常由 AI 模型生成。
     */
    private String id;

    /**
     * 工具名称
     * 作用：标识执行的是哪个工具（例如："search_weather"）。
     */
    private String name;

    /**
     * 工具参数
     * 作用：记录调用工具时传入的具体参数（例如："北京"）。
     */
    private String arguments;

    /**
     * 工具执行结果
     * 作用：这是核心字段，存储工具运行后的返回值（例如："晴，25度"）。
     * 这个结果通常会被反馈给 AI，作为上下文生成最终回答。
     */
    private String result;

    /**
     * 转换构造函数
     * 作用：接收 LangChain4j 的 ToolExecution 对象，将其转换为本项目的消息格式。
     * @param toolExecution LangChain4j 框架提供的原始工具执行对象
     */
    public ToolExecutedMessage(ToolExecution toolExecution) {
        // 1. 调用父类构造函数，设置 type 为 "tool_executed"
        super(StreamMessageTypeEnum.TOOL_EXECUTED.getValue());

        // 2. 从 toolExecution 对象中提取请求信息（id, name, arguments）
        this.id = toolExecution.request().id();
        this.name = toolExecution.request().name();
        this.arguments = toolExecution.request().arguments();

        // 3. 提取工具执行的最终结果
        this.result = toolExecution.result();
    }
}