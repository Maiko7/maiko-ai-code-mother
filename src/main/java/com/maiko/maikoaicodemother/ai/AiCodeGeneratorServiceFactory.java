package com.maiko.maikoaicodemother.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.maiko.maikoaicodemother.ai.tools.*;
import com.maiko.maikoaicodemother.exception.BusinessException;
import com.maiko.maikoaicodemother.exception.ErrorCode;
import com.maiko.maikoaicodemother.model.enums.CodeGenTypeEnum;
import com.maiko.maikoaicodemother.service.ChatHistoryService;
import com.maiko.maikoaicodemother.utils.SpringContextUtil;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.swing.*;
import java.time.Duration;

/**
 * 【类定义】AI代码生成服务工厂
 *
 * 核心职责：
 * 1. 服务实例的“大管家”：负责创建和管理 AiCodeGeneratorService 实例。
 * 2. 上下文隔离：确保不同的 appId（项目）拥有独立的对话记忆，互不干扰。
 * 3. 性能优化：利用 Caffeine 缓存服务实例，避免重复创建带来的开销。
 * 4. 差异化配置：针对普通代码生成和复杂的 Vue 项目生成，提供不同的模型和工具配置。
 */
@Configuration
@Slf4j
public class AiCodeGeneratorServiceFactory {

    // ================= 依赖注入 =================

    /** 基础聊天模型（非流式） */
    @Resource(name = "openAiChatModel")
    private ChatModel chatModel;


    /** 流式聊天模型（用于普通代码生成，如 HTML/Multi-file） */
    @Resource(name = "streamingChatModelPrototype")
    private StreamingChatModel openAiStreamingChatModel;

    /** 推理流式聊天模型（用于复杂任务，如 Vue 项目生成） */
    @Resource(name = "reasoningStreamingChatModelPrototype")
    private StreamingChatModel reasoningStreamingChatModel;

    /** Redis 聊天记忆存储，实现对话历史的持久化 */
    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    /** 业务层服务，用于加载历史聊天记录 */
    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private ToolManager toolManager;

    /**
     * 【性能优化】本地缓存：存储已创建的 AI 服务实例
     *
     * 策略：
     * - 最大缓存 1000 个实例。
     * - 写入后 30 分钟过期，访问后 10 分钟过期（防止内存泄漏）。
     *
     * 收益：
     * 同一个 appId 的请求不需要每次都重新构建 AiServices，直接复用，大幅提升响应速度。
     */
    private final Cache<String, AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener((key, value, cause) -> {
                log.debug("AI 服务实例被移除，缓存键: {}, 原因: {}", key, cause);
            })
            .build();

    // ================= 公共接口 =================

    /**
     * 获取服务实例（兼容旧逻辑）
     * 默认使用 HTML 生成类型。
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId) {
        return getAiCodeGeneratorService(appId, CodeGenTypeEnum.HTML);
    }

    /**
     * 【核心方法】获取 AI 代码生成服务实例
     *
     * 流程：
     * 1. 构建缓存 Key (appId + 类型)。
     * 2. 尝试从 Caffeine 缓存获取。
     * 3. 如果缓存未命中，则调用 createAiCodeGeneratorService 方法创建新实例并放入缓存。
     *
     * @param appId 项目唯一ID，用于隔离对话记忆
     * @param codeGenType 代码生成类型，决定使用哪种模型配置
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId, CodeGenTypeEnum codeGenType) {
        String cacheKey = buildCacheKey(appId, codeGenType);
        // Caffeine 的 get 方法支持原子性地加载数据，线程安全
        return serviceCache.get(cacheKey, key -> createAiCodeGeneratorService(appId, codeGenType));
    }

    // ================= 内部构建逻辑 =================

    /**
     * 创建新的 AI 服务实例
     * 注意：此方法仅在缓存未命中时被调用。
     */
    private AiCodeGeneratorService createAiCodeGeneratorService(long appId, CodeGenTypeEnum codeGenType) {
        // 1. 创建独立的对话记忆
        // 每个 appId 对应一个独立的 MessageWindowChatMemory，通过 Redis 实现持久化共享
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory
                .builder()
                .id(appId)
                .chatMemoryStore(redisChatMemoryStore)
                .maxMessages(20) // 限制上下文窗口大小，节省 Token
                .build();

        // 2. 加载历史聊天记录
        // 将数据库中存储的该项目的历史对话加载到内存中，实现“续聊”功能
        chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 20);

        // 3. 根据类型构建不同的服务配置
        return switch (codeGenType) {
            case VUE_PROJECT -> {
                StreamingChatModel reasoningStreamingChatModel = SpringContextUtil.getBean("reasoningStreamingChatModelPrototype", StreamingChatModel.class);
                // 【场景：Vue 项目生成】
                // 特点：任务复杂，需要 AI 自主规划，需要调用工具写文件。
                // 配置：使用推理模型 + 注册 FileWriteTool 工具。
                yield AiServices.builder(AiCodeGeneratorService.class)
                        .chatModel(chatModel)
                        // 强推理模型
                        .streamingChatModel(reasoningStreamingChatModel)
                        // 绑定记忆
                        .chatMemoryProvider(memoryId -> chatMemory)
                        // 赋予 AI 写文件的能力
                        .tools(toolManager.getAllTools())
                        // 处理幻觉：如果 AI 瞎编工具名，给它一个明确的错误反馈
                        .hallucinatedToolNameStrategy(req -> ToolExecutionResultMessage.from(
                                req, "Error: 不存在该工具: " + req.name()
                        ))
                        .build();
            }
            case HTML, MULTI_FILE -> {
                // 【场景：普通代码生成】
                // 特点：任务简单，只需输出代码，无需复杂工具调用。
                // 配置：使用基础流式模型，响应更快。

                // 使用多例模式的StreamingChatModel解决并发问题
                StreamingChatModel openAiStreamingChatModel = SpringContextUtil.getBean("streamingChatModelPrototype", StreamingChatModel.class);
                yield AiServices.builder(AiCodeGeneratorService.class)
                        .chatModel(chatModel)
                        .streamingChatModel(openAiStreamingChatModel)
                        .chatMemory(chatMemory) // 绑定记忆
                        .build();
            }
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR,
                    "不支持的代码生成类型: " + codeGenType.getValue());
        };
    }

    /**
     * Spring Bean 初始化
     * 向容器注册一个默认的 AiCodeGeneratorService。
     * 注意：这里使用了 appId=0 的默认实例，主要用于系统初始化或无状态的简单调用。
     */
    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        return getAiCodeGeneratorService(0);
    }

    // ================= 工具方法 =================

    /**
     * 构建缓存唯一 Key
     * 格式：appId_类型值 (例如：1001_HTML)
     */
    private String buildCacheKey(long appId, CodeGenTypeEnum codeGenType) {
        return appId + "_" + codeGenType.getValue();
    }
}