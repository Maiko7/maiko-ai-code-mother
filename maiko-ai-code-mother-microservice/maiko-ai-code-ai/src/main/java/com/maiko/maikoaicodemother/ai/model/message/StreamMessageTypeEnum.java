package com.maiko.maikoaicodemother.ai.model.message;

import lombok.Getter;

/**
 * 流式消息类型枚举
 * 作用：定义系统中所有合法的流式消息类型，作为消息识别的“字典”。
 */
@Getter // Lombok注解：为 value 和 text 字段自动生成 Getter 方法
public enum StreamMessageTypeEnum {

    // 枚举常量定义
    // 格式：枚举名("机器识别值", "人类可读描述")

    /**
     * AI响应：代表这是AI生成的文本内容（如回答、代码等）。
     * 前端收到此类型时，通常会将其追加显示在聊天窗口中。
     */
    AI_RESPONSE("ai_response", "AI响应"),

    /**
     * 工具请求：代表AI决定调用某个工具（如搜索、画图、查数据库）。
     * 前端收到此类型时，可能会显示“AI正在思考/使用工具...”的动画。
     */
    TOOL_REQUEST("tool_request", "工具请求"),

    /**
     * 工具执行结果：代表工具已经运行完毕，返回了结果。
     * 这个结果通常会作为上下文再次发给AI，以便AI进行下一步回答。
     */
    TOOL_EXECUTED("tool_executed", "工具执行结果");

    // 字段定义
    private final String value; // 机器识别码：用于代码逻辑判断、JSON传输（推荐用英文字符串，不易出错）
    private final String text;  // 人类描述：用于日志打印、前端展示或调试（中文说明）

    // 构造函数
    StreamMessageTypeEnum(String value, String text) {
        this.value = value;
        this.text = text;
    }

    /**
     * 根据值获取枚举（核心工具方法）
     * 场景：当后端接收到前端传来的字符串 "ai_response" 时，需要将其转换成枚举对象进行 switch 判断。
     */
    public static StreamMessageTypeEnum getEnumByValue(String value) {
        // 遍历所有枚举项
        for (StreamMessageTypeEnum typeEnum : values()) {
            if (typeEnum.getValue().equals(value)) {
                return typeEnum; // 找到匹配项则返回
            }
        }
        return null; // 没找到则返回null（实际生产中建议抛出异常或返回默认值）
    }
}