package com.maiko.maikoaicodemother.monitor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * AI 模型调用指标收集器
 * <p>
 * 基于 Micrometer 实现对 AI 模型调用过程的深度监控。
 * 通过缓存机制复用 Meter 对象，避免高基数（High Cardinality）场景下的内存泄漏。
 * </p>
 *
 * @author Maiko7
 */
@Component
@Slf4j
public class AiModelMetricsCollector {

    /**
     * 注入 Spring Boot Actuator 的注册中心
     */
    @Resource
    private MeterRegistry meterRegistry;

    /**
     * 缓存已创建的指标对象，按类型分离存储
     * <p>
     * Key 格式：userId_appId_modelName_tagValue
     * 使用 ConcurrentHashMap 保证多线程环境下的安全性。
     * </p>
     */
    private final ConcurrentMap<String, Counter> requestCountersCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Counter> errorCountersCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Counter> tokenCountersCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Timer> responseTimersCache = new ConcurrentHashMap<>();

    /**
     * 记录 AI 模型的请求次数
     *
     * @param userId    用户 ID
     * @param appId     应用 ID
     * @param modelName 模型名称 (如 gpt-4)
     * @param status    请求状态 (如 success, fail)
     */
    public void recordRequest(String userId, String appId, String modelName, String status) {
        String key = String.format("%s_%s_%s_%s", userId, appId, modelName, status);

        // computeIfAbsent 保证同一组标签只创建一次 Counter
        Counter counter = requestCountersCache.computeIfAbsent(key, k ->
                Counter.builder("ai_model_requests_total")
                        .description("AI模型总请求次数")
                        .tag("user_id", userId)
                        .tag("app_id", appId)
                        .tag("model_name", modelName)
                        .tag("status", status)
                        .register(meterRegistry)
        );
        counter.increment();
    }

    /**
     * 记录 AI 模型调用的错误信息
     *
     * @param userId       用户 ID
     * @param appId        应用 ID
     * @param modelName    模型名称
     * @param errorMessage 具体的错误信息摘要
     */
    public void recordError(String userId, String appId, String modelName, String errorMessage) {
        String key = String.format("%s_%s_%s_%s", userId, appId, modelName, errorMessage);

        Counter counter = errorCountersCache.computeIfAbsent(key, k ->
                Counter.builder("ai_model_errors_total")
                        .description("AI模型错误次数")
                        .tag("user_id", userId)
                        .tag("app_id", appId)
                        .tag("model_name", modelName)
                        .tag("error_message", errorMessage)
                        .register(meterRegistry)
        );
        counter.increment();
    }

    /**
     * 记录 Token 消耗量
     *
     * @param userId    用户 ID
     * @param appId     应用 ID
     * @param modelName 模型名称
     * @param tokenType Token 类型 (prompt, completion, total)
     * @param tokenCount 消耗数量
     */
    public void recordTokenUsage(String userId, String appId, String modelName,
                                 String tokenType, long tokenCount) {
        String key = String.format("%s_%s_%s_%s", userId, appId, modelName, tokenType);

        Counter counter = tokenCountersCache.computeIfAbsent(key, k ->
                Counter.builder("ai_model_tokens_total")
                        .description("AI模型Token消耗总数")
                        .tag("user_id", userId)
                        .tag("app_id", appId)
                        .tag("model_name", modelName)
                        .tag("token_type", tokenType)
                        .register(meterRegistry)
        );
        // 累加具体的数值
        counter.increment(tokenCount);
    }

    /**
     * 记录 AI 模型的响应耗时
     *
     * @param userId   用户 ID
     * @param appId    应用 ID
     * @param modelName 模型名称
     * @param duration 耗时时长
     */
    public void recordResponseTime(String userId, String appId, String modelName, Duration duration) {
        String key = String.format("%s_%s_%s", userId, appId, modelName);

        Timer timer = responseTimersCache.computeIfAbsent(key, k ->
                Timer.builder("ai_model_response_duration_seconds")
                        .description("AI模型响应时间")
                        .tag("user_id", userId)
                        .tag("app_id", appId)
                        .tag("model_name", modelName)
                        .register(meterRegistry)
        );
        timer.record(duration);
    }
}