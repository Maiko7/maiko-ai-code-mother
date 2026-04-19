package com.maiko.maikoaicodemother.ai.model.message;

import dev.langchain4j.agent.tool.ToolExecutionRequest; // 引入 LangChain4j 的工具请求对象
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 工具调用消息
 * 作用：封装 AI 决定调用外部工具时发出的具体指令。
 * 继承自 StreamMessage，拥有 type 字段。
 */
@Data // Lombok注解：自动生成 Getter/Setter、toString 等方法
@EqualsAndHashCode(callSuper = true) // Lombok注解：生成 equals/hashCode 时包含父类字段
@NoArgsConstructor // Lombok注解：生成无参构造函数
public class ToolRequestMessage extends StreamMessage {

    /**
     * 请求唯一标识ID
     * 作用：用于追踪这次调用。当工具执行完返回结果时，会用这个ID告诉AI是哪次请求的结果。
     */
    private String id;

    /**
     * 工具名称
     * 作用：告诉系统需要调用哪个工具（例如："get_weather" 或 "search_web"）。
     */
    private String name;

    /**
     * 工具参数
     * 作用：AI 思考后得出的参数（例如："{'city': 'Beijing'}"）。
     * 这是一个 JSON 格式的字符串，系统需要解析它来执行具体的工具逻辑。
     */
    private String arguments;

    /**
     * 转换构造函数
     * 作用：接收 LangChain4j 的 ToolExecutionRequest 对象，将其转换为本项目的消息格式。
     * @param toolExecutionRequest LangChain4j 框架提供的原始请求对象
     */
    public ToolRequestMessage(ToolExecutionRequest toolExecutionRequest) {
        // 1. 调用父类构造函数，设置 type 为 "tool_request"
        super(StreamMessageTypeEnum.TOOL_REQUEST.getValue());

        // 2. 从 request 对象中提取关键信息
        this.id = toolExecutionRequest.id();
        this.name = toolExecutionRequest.name();
        this.arguments = toolExecutionRequest.arguments();
    }
}