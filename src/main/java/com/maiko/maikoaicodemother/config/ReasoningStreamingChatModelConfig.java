package com.maiko.maikoaicodemother.config;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * 推理流式聊天模型配置类
 * <p>
 * 该配置类专门用于处理与“推理”相关的 OpenAI 流式聊天模型设置。
 * 它通过读取配置文件（如 application.yml）中指定前缀的属性来自动绑定参数，
 * 并提供一个原型作用域的 Bean 供其他组件注入使用。
 * </p>
 *
 * @author Maiko7
 */
@Configuration
// 绑定配置文件中以 "langchain4j.open-ai.reasoning-streaming-chat-model" 开头的属性
@ConfigurationProperties(prefix = "langchain4j.open-ai.reasoning-streaming-chat-model")
@Data
public class ReasoningStreamingChatModelConfig {

    /**
     * OpenAI API 的基础 URL
     * <p>
     * 如果使用官方 API，通常为默认值；如果使用代理或兼容接口，可在此处修改。
     * </p>
     */
    private String baseUrl;

    /**
     * OpenAI API 认证密钥
     * <p>
     * 用于向 API 服务证明身份，建议在生产环境中通过环境变量注入。
     * </p>
     */
    private String apiKey;

    /**
     * 使用的模型名称
     * <p>
     * 例如：gpt-3.5-turbo, gpt-4, o1-preview 等。
     * </p>
     */
    private String modelName;

    /**
     * 生成内容的最大令牌数
     * <p>
     * 限制模型单次回复的长度，超出部分将被截断。
     * </p>
     */
    private Integer maxTokens;

    /**
     * 采样温度
     * <p>
     * 控制生成的随机性（0.0 - 2.0）。
     * 对于推理任务，通常建议使用较低的温度以获得更确定的结果。
     * </p>
     */
    private Double temperature;

    /**
     * 是否记录请求日志
     * <p>
     * 默认为 false，开启后可在控制台查看发送给 API 的详细数据。
     * </p>
     */
    private Boolean logRequests = false;

    /**
     * 是否记录响应日志
     * <p>
     * 默认为 false，开启后可在控制台查看从 API 接收到的详细数据。
     * </p>
     */
    private Boolean logResponses = false;

    /**
     * 创建推理流式聊天模型的 Bean
     * <p>
     * 该方法使用构建器模式初始化 {@link OpenAiStreamingChatModel} 实例。
     * </p>
     *
     * @return StreamingChatModel 配置好的流式聊天模型实例
     */
    @Bean
    // 设置为原型作用域，确保每次注入时获取的是新实例，避免状态共享问题
    @Scope("prototype")
    public StreamingChatModel reasoningStreamingChatModelPrototype() {
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)           // 设置 API 密钥
                .baseUrl(baseUrl)         // 设置基础 URL
                .modelName(modelName)     // 设置模型名称
                .maxTokens(maxTokens)     // 设置最大令牌数
                .temperature(temperature) // 设置温度参数
                .logRequests(logRequests) // 设置请求日志开关
                .logResponses(logResponses) // 设置响应日志开关
                .build();                 // 构建最终对象
    }
}