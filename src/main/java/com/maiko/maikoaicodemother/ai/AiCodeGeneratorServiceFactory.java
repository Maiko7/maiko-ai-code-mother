package com.maiko.maikoaicodemother.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI代码生成器服务工厂配置类
 * <p>
 * 负责创建和管理AI代码生成服务的Bean实例。通过LangChain4j的AiServices工具，
 * 将ChatModel与AiCodeGeneratorService接口绑定，实现基于AI的代码生成功能。
 * </p>
 *
 * @author Maiko7
 * @create 2026-04-11 10:44
 */
@Configuration
public class AiCodeGeneratorServiceFactory {

    /**
     * LangChain4j聊天模型实例
     * <p>
     * 由Spring容器自动注入，用于与AI模型进行对话交互
     * </p>
     */
    @Resource
    private ChatModel chatModel;

    @Resource
    private StreamingChatModel streamingChatModel;

    /**
     * 创建AI代码生成器服务Bean
     * <p>
     * 使用LangChain4j的AiServices工具创建AiCodeGeneratorService的实现实例。
     * 该服务将接口方法与聊天模型绑定，支持通过自然语言描述生成HTML代码和多文件代码。
     * </p>
     *
     * 工厂类 + @Bean告诉 Spring：
     * 帮我创建这个 AI 服务，交给你管理，以后别人 @Resource 就能直接用！
     *
     * @return 配置好的AI代码生成器服务实例
     */
//    老版本
//    @Bean
//    public AiCodeGeneratorService aiCodeGeneratorService() {
//        return AiServices.create(AiCodeGeneratorService.class, chatModel);
//    }

    /**
     * 为什么从上面的老版本变成了下面的流式输出，因为结构化输出比较慢你不可能说我在那里等着吧
     * 你看GPT，千问都是流式的，就是一个字一个字的吐出来。
     */
    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        return AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .build();
    }
}
