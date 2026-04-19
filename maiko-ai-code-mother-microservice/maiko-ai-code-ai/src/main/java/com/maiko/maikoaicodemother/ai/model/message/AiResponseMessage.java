package com.maiko.maikoaicodemother.ai.model.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * AI 响应消息
 * 作用：具体承载 AI 生成的文本内容（如回答、代码等）。
 * 继承自 StreamMessage，拥有 type 字段。
 */
@EqualsAndHashCode(callSuper = true) // Lombok注解：生成 equals 和 hashCode 方法时，包含父类（StreamMessage）的字段
@Data // Lombok注解：自动生成 Getter/Setter、toString 等方法
@NoArgsConstructor // Lombok注解：生成无参构造函数（用于 JSON 反序列化）
public class AiResponseMessage extends StreamMessage {

    /**
     * 消息内容字段
     * 作用：存储 AI 实际生成的文本数据。
     * 例如："你好，我是AI助手。" 或 "public class HelloWorld { ... }"
     */
    private String data;

    /**
     * 有参构造函数
     * 作用：创建一个 AI 响应消息时，自动设置 type 为 "ai_response"，并填充 data。
     * @param data AI 生成的具体内容
     */
    public AiResponseMessage(String data) {
        // 调用父类构造函数，设置 type 字段为枚举定义的 "ai_response"
        super(StreamMessageTypeEnum.AI_RESPONSE.getValue());
        // 设置本类的 data 字段
        this.data = data;
    }
}