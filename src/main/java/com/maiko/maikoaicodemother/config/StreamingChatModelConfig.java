package com.maiko.maikoaicodemother.config;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * 流式聊天模型配置类
 * 用于配置和创建 LangChain4j 的 OpenAI 流式聊天模型 Bean
 */
@Configuration
@ConfigurationProperties(prefix = "langchain4j.open-ai.streaming-chat-model")
@Data
public class StreamingChatModelConfig {

    /**
     * OpenAI API 的基础 URL
     * 对应配置文件中的 langchain4j.open-ai.streaming-chat-model.base-url
     */
    private String baseUrl;

    /**
     * OpenAI API 密钥
     * 对应配置文件中的 langchain4j.open-ai.streaming-chat-model.api-key
     */
    private String apiKey;

    /**
     * 使用的模型名称
     * 例如: gpt-3.5-turbo, gpt-4 等
     * 对应配置文件中的 langchain4j.open-ai.streaming-chat-model.model-name
     */
    private String modelName;

    /**
     * 生成内容的最大令牌数
     * 对应配置文件中的 langchain4j.open-ai.streaming-chat-model.max-tokens
     */
    private Integer maxTokens;

    /**
     * 生成内容的随机性/创造性程度 (0.0 - 2.0)
     * 值越高输出越随机，值越低输出越确定
     * 对应配置文件中的 langchain4j.open-ai.streaming-chat-model.temperature
     */
    private Double temperature;

    /**
     * 是否记录请求日志
     * 对应配置文件中的 langchain4j.open-ai.streaming-chat-model.log-requests
     */
    private boolean logRequests;

    /**
     * 是否记录响应日志
     * 对应配置文件中的 langchain4j.open-ai.streaming-chat-model.log-responses
     */
    private boolean logResponses;

    /**
     * 创建流式聊天模型的 Bean
     * 使用原型作用域，每次请求都会创建新的实例
     *
     * @return StreamingChatModel 流式聊天模型实例
     */
    @Bean
    @Scope("prototype")
    public StreamingChatModel streamingChatModelPrototype() {
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)           // 设置 API 密钥
                .baseUrl(baseUrl)         // 设置基础 URL
                .modelName(modelName)     // 设置模型名称
                .maxTokens(maxTokens)     // 设置最大令牌数
                .temperature(temperature) // 设置温度参数
                .logRequests(logRequests) // 设置是否记录请求日志
                .logResponses(logResponses) // 设置是否记录响应日志
                .build();                 // 构建模型实例
    }
}