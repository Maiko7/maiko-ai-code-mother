package com.maiko.maikoaicodemother.monitor;

import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.output.TokenUsage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * AI 模型调用监听器
 * <p>
 * 基于 LangChain4j 的事件监听机制，拦截 AI 对话模型的请求、响应和异常事件。
 * 负责采集全链路监控数据（耗时、Token、状态），并委托给指标收集器进行统计。
 * </p>
 *
 * @author Maiko7
 */
@Component
@Slf4j
public class AiModelMonitorListener implements ChatModelListener {

    /**
     * 用于存储请求开始时间的属性键
     */
    private static final String REQUEST_START_TIME_KEY = "request_start_time";

    /**
     * 用于在请求和响应间传递监控上下文的属性键
     */
    private static final String MONITOR_CONTEXT_KEY = "monitor_context";

    /**
     * 注入具体的指标收集器组件
     */
    @Resource
    private AiModelMetricsCollector aiModelMetricsCollector;

    /**
     * 请求开始时触发
     * <p>
     * 1. 记录当前时间戳以便后续计算耗时。
     * 2. 获取当前线程的监控上下文（用户/应用信息）。
     * 3. 发送“开始”信号指标。
     * </p>
     *
     * @param requestContext 包含请求详情和可共享的属性映射
     */
    @Override
    public void onRequest(ChatModelRequestContext requestContext) {
        Instant startTime = Instant.now();
        requestContext.attributes().put(REQUEST_START_TIME_KEY, startTime);

        MonitorContext monitorContext = MonitorContextHolder.getContext();

        if (monitorContext == null) {
            log.debug("监控上下文为空，跳过监控记录");
            return;
        }

        requestContext.attributes().put(MONITOR_CONTEXT_KEY, monitorContext);

        String userId = monitorContext.getUserId();
        String appId = monitorContext.getAppId();
        String modelName = requestContext.chatRequest().modelName();

        aiModelMetricsCollector.recordRequest(userId, appId, modelName, "started");
    }

//    @Override
//    public void onRequest(ChatModelRequestContext requestContext) {
//        // 1. 记录开始时间
//        Instant startTime = Instant.now();
//        requestContext.attributes().put(REQUEST_START_TIME_KEY, startTime);
//
//        // 2. 获取业务上下文（注意：这里假设 MonitorContextHolder 已正确实现 ThreadLocal 管理）
//        MonitorContext monitorContext = MonitorContextHolder.getContext();
//
//        // 3. 将上下文存入属性，供 onResponse 或 onError 使用（跨方法传递数据）
//        requestContext.attributes().put(MONITOR_CONTEXT_KEY, monitorContext);
//
//        // 4. 提取关键信息
//        String userId = monitorContext.getUserId();
//        String appId = monitorContext.getAppId();
//        String modelName = requestContext.chatRequest().modelName();
//
//        // 5. 记录请求发起指标
//        aiModelMetricsCollector.recordRequest(userId, appId, modelName, "started");
//    }

    /**
     * 请求成功返回时触发
     * <p>
     * 从上下文中恢复信息，记录成功状态、响应耗时以及 Token 消耗详情。
     * </p>
     *
     * @param responseContext 包含响应结果和之前存储的属性
     */
    @Override
    public void onResponse(ChatModelResponseContext responseContext) {
        // 1. 恢复属性中的上下文信息
        Map<Object, Object> attributes = responseContext.attributes();
        MonitorContext context = (MonitorContext) attributes.get(MONITOR_CONTEXT_KEY);

        if (context == null) {
            log.warn("监控上下文丢失，无法记录成功指标");
            return;
        }

        String userId = context.getUserId();
        String appId = context.getAppId();
        String modelName = responseContext.chatResponse().modelName();

        // 2. 记录成功指标
        aiModelMetricsCollector.recordRequest(userId, appId, modelName, "success");

        // 3. 记录响应耗时
        recordResponseTime(attributes, userId, appId, modelName);

        // 4. 记录 Token 用量（输入/输出/总计）
        recordTokenUsage(responseContext, userId, appId, modelName);
    }

    /**
     * 请求发生错误时触发
     * <p>
     * 捕获异常信息，记录失败状态和错误原因，同时也会记录直到报错时的耗时。
     * </p>
     *
     * @param errorContext 包含原始请求和异常对象
     */
    @Override
    public void onError(ChatModelErrorContext errorContext) {
        // 1. 恢复上下文
        Map<Object, Object> attributes = errorContext.attributes();
        MonitorContext context = (MonitorContext) attributes.get(MONITOR_CONTEXT_KEY);

        if (context == null) {
            log.warn("监控上下文丢失，无法记录错误指标");
            return;
        }

        String userId = context.getUserId();
        String appId = context.getAppId();
        String modelName = errorContext.chatRequest().modelName();
        String errorMessage = errorContext.error().getMessage();

        // 2. 记录失败指标
        aiModelMetricsCollector.recordRequest(userId, appId, modelName, "error");
        aiModelMetricsCollector.recordError(userId, appId, modelName, errorMessage);

        // 3. 记录耗时（即使是失败的请求也有参考价值）
        recordResponseTime(attributes, userId, appId, modelName);
    }

    /**
     * 计算并记录响应耗时
     *
     * @param attributes 存储了开始时间的属性映射
     */
    private void recordResponseTime(Map<Object, Object> attributes, String userId, String appId, String modelName) {
        Instant startTime = (Instant) attributes.get(REQUEST_START_TIME_KEY);
        if (startTime != null) {
            Duration responseTime = Duration.between(startTime, Instant.now());
            aiModelMetricsCollector.recordResponseTime(userId, appId, modelName, responseTime);
        }
    }

    /**
     * 解析并记录 Token 使用情况
     *
     * @param responseContext 响应上下文，包含元数据
     */
    private void recordTokenUsage(ChatModelResponseContext responseContext, String userId, String appId, String modelName) {
        TokenUsage tokenUsage = responseContext.chatResponse().metadata().tokenUsage();
        if (tokenUsage != null) {
            // 分别记录输入、输出和总 Token 数
            aiModelMetricsCollector.recordTokenUsage(userId, appId, modelName, "input", tokenUsage.inputTokenCount());
            aiModelMetricsCollector.recordTokenUsage(userId, appId, modelName, "output", tokenUsage.outputTokenCount());
            aiModelMetricsCollector.recordTokenUsage(userId, appId, modelName, "total", tokenUsage.totalTokenCount());
        }
    }
}