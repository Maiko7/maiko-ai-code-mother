package com.maiko.maikoaicodemother.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.maiko.maikoaicodemother.service.ChatHistoryService;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

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
@Slf4j
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

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private ChatHistoryService chatHistoryService;

    /**
     * AI 服务实例缓存
     * <p> 每次构造完appId对应的AI服务实例后，利用Caffenine缓存来存储，之后相同appId就能直接获取到AI服务实例，避免重复构造。
     * 简单来说，就是从“每次有人调用，都现场造一个新的对象”，变成了“先看看有没有现成的，没有再造，造好了存起来下次用”。
     * 缓存策略：
     * - 最大缓存 1000 个实例
     * - 写入后 30 分钟过期
     * - 访问后 10 分钟过期
     */
    private final Cache<Long, AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener((key, value, cause) -> {
                log.debug("AI 服务实例被移除，appId: {}, 原因: {}", key, cause);
            })
            .build();

    /**
     * 根据 appId 获取服务（带缓存）
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId) {
        return serviceCache.get(appId, this::createAiCodeGeneratorService);
    }

    /**
     * 创建新的 AI 服务实例
     */
    private AiCodeGeneratorService createAiCodeGeneratorService(long appId) {
        log.info("为 appId: {} 创建新的 AI 服务实例", appId);
        // 根据 appId 构建独立的对话记忆
        // 这段代码就是在说：“记个日志，然后给这个用户（appId）造一个专属的记忆盒子。盒子连着 Redis 数据库，里面只装最近的 20 句话。”
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory //MessageWindowChatMemory这是一种特定类型的记忆，叫“滑动窗口”。就像看电影一样，它只保留最近的剧情，太早的剧情会被“滑”出去忘掉。
                .builder() // 开始构建一个 LangChain4j 的“聊天记忆”对象。
                .id(appId) // 给这个记忆体打上标签。
                .chatMemoryStore(redisChatMemoryStore)
                .maxMessages(20)
                .build();
        // 从数据库中加载对话历史到记忆中
        chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 20);
        return AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .chatMemory(chatMemory)
                .build();
    }


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



    /**
     * 获取指定应用的 AI 代码生成器服务实例
     * <p>
     * 该方法会为每个 appId 创建独立的对话记忆（MessageWindowChatMemory），
     * 并基于此构建一个独立的 AiCodeGeneratorService 实例。
     * 这样可以确保不同应用之间的对话历史完全隔离，互不干扰。
     *
     * @param appId 应用的唯一标识符，用于区分不同的对话记忆
     * @return 配置好的 AiCodeGeneratorService 实例，包含独立的对话记忆
     */
//    public AiCodeGeneratorService getAiCodeGeneratorService(long appId) {
//        // 为当前 appId 构建独立的滑动窗口对话记忆
//        MessageWindowChatMemory chatMemory = MessageWindowChatMemory
//                .builder()
//                .id(appId) // 设置记忆的唯一标识为 appId，确保不同应用的记忆隔离
//                .chatMemoryStore(redisChatMemoryStore) // 使用 Redis 作为存储介质，保证数据持久化和分布式支持
//                .maxMessages(20) // 限制最多保留 20 条消息，防止上下文过长导致 token 消耗过高
//                .build();
//
//        // 使用 AiServices 构建 AI 代码生成器服务
//        return AiServices.builder(AiCodeGeneratorService.class)
//                .chatModel(chatModel) // 注入标准聊天模型，处理非流式请求
//                .streamingChatModel(streamingChatModel) // 注入流式聊天模型，处理流式响应
//                .chatMemory(chatMemory) // 绑定当前 appId 专属的对话记忆
//                .build(); // 构建并返回服务实例
//    }


    /**
     * 创建 AI 代码生成器服务的 Bean
     * 该服务使用 LangChain4j 的 AiServices 构建，支持流式和非流式响应，
     * 并为每个会话维护独立的对话记忆
     */
    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
//        return AiServices.builder(AiCodeGeneratorService.class)
//                // 配置标准聊天模型，用于处理非流式的一次性请求
//                .chatModel(chatModel)
//                // 配置流式聊天模型，用于处理流式响应，实现打字机效果
//                .streamingChatModel(streamingChatModel)
//                // 配置对话记忆提供者，根据 memoryId 为每个会话构建独立的对话记忆
//                .chatMemoryProvider(memoryId -> MessageWindowChatMemory
//                        .builder()
//                        // 设置记忆的唯一标识，用于区分不同用户或会话
//                        .id(memoryId)
//                        // 使用 Redis 作为对话记忆的存储介质，保证数据持久化和分布式支持
//                        .chatMemoryStore(redisChatMemoryStore)
//                        // 限制每个会话最多保留 20 条消息，防止上下文过长导致 token 消耗过高
//                        .maxMessages(20)
//                        // 构建 MessageWindowChatMemory 实例
//                        .build())
//                // 构建最终的 AiCodeGeneratorService 代理实例
//                .build();
//    }
        return getAiCodeGeneratorService(0);
    }

}
