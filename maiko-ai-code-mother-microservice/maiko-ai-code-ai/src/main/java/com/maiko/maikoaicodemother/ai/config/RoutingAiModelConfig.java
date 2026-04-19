package com.maiko.maikoaicodemother.ai.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * 路由 AI 模型配置类
 * <p>
 * 该配置类专门用于处理与“路由”相关的 OpenAI 聊天模型设置。
 * 它通过读取配置文件中指定前缀的属性来自动绑定参数，
 * 并提供一个原型作用域的 ChatModel Bean，通常用于根据用户输入进行意图识别或任务分发。
 * </p>
 *
 * @author Maiko7
 */
@Configuration
// 绑定配置文件中以 "langchain4j.open-ai.routing-chat-model" 开头的属性
@ConfigurationProperties(prefix = "langchain4j.open-ai.routing-chat-model")
@Data
public class RoutingAiModelConfig {

    /**
     * OpenAI API 的基础 URL
     * <p>
     * 支持自定义端点，适用于使用反向代理或兼容 OpenAI 接口的第三方服务。
     * </p>
     */
    private String baseUrl;

    /**
     * OpenAI API 认证密钥
     * <p>
     * 用于身份验证，建议在生产环境中通过环境变量或密钥管理服务注入。
     * </p>
     */
    private String apiKey;

    /**
     * 使用的模型名称
     * <p>
     * 对于路由任务，通常选择响应速度快且成本较低的模型（如 gpt-3.5-turbo）。
     * </p>
     */
    private String modelName;

    /**
     * 生成内容的最大令牌数
     * <p>
     * 限制模型单次回复的长度。对于路由判断，通常不需要很长的输出。
     * </p>
     */
    private Integer maxTokens;

    /**
     * 采样温度
     * <p>
     * 控制生成的随机性（0.0 - 2.0）。
     * 对于路由/分类任务，通常建议设置为 0 或较低的值，以确保判断的一致性和准确性。
     * </p>
     */
    private Double temperature;

    /**
     * 是否记录请求日志
     * <p>
     * 默认为 false。开启后可用于调试路由逻辑的输入内容。
     * </p>
     */
    private Boolean logRequests = false;

    /**
     * 是否记录响应日志
     * <p>
     * 默认为 false。开启后可用于调试路由逻辑的输出结果。
     * </p>
     */
    private Boolean logResponses = false;

    /**
     * 创建用于路由判断的 ChatModel Bean
     * <p>
     * 该方法使用构建器模式初始化 {@link OpenAiChatModel} 实例。
     * 此模型主要用于分析用户意图，决定后续的处理流程。
     * </p>
     *
     * @return ChatModel 配置好的流式聊天模型实例
     */
    @Bean
    // 设置为原型作用域，确保每次注入时获取的是新实例，避免多线程环境下的状态共享问题
    @Scope("prototype")
    public ChatModel routingChatModelPrototype() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)           // 设置 API 密钥
                .modelName(modelName)     // 设置模型名称
                .baseUrl(baseUrl)         // 设置基础 URL
                .maxTokens(maxTokens)     // 设置最大令牌数
                .temperature(temperature) // 设置温度参数
                .logRequests(logRequests) // 设置请求日志开关
                .logResponses(logResponses) // 设置响应日志开关
                .build();                 // 构建最终对象
    }
}