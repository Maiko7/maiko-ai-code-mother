package com.maiko.maikoaicodemother.ai.model.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 流式消息响应基类
 * 作用：定义所有流式消息的统一结构，作为其他具体消息类的父类。
 */
@Data // Lombok注解：自动生成Getter/Setter、toString、equals、hashCode等方法，简化POJO代码
@AllArgsConstructor // Lombok注解：自动生成包含所有字段（type）的全参构造函数
@NoArgsConstructor // Lombok注解：自动生成无参构造函数（主要用于JSON反序列化框架如Jackson/Gson实例化对象）
public class StreamMessage {
    /**
     * 消息类型标识
     * 作用：用于区分当前流式数据的具体业务含义。
     * 场景：在WebSocket或SSE流中，前端需要根据这个type字段判断是显示文本、调用工具还是结束连接。
     */
    private String type;
}