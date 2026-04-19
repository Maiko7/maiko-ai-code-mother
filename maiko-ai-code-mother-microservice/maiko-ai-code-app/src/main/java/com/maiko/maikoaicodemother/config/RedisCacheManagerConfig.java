package com.maiko.maikoaicodemother.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.Resource;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis 缓存管理器配置类
 * <p>
 * 该类用于配置 Spring Cache 的 Redis 实现。它定义了 {@link CacheManager} Bean，
 * 负责管理缓存的生命周期、序列化策略以及过期时间。
 * 通过自定义配置，解决了默认序列化不支持 Java 8 时间类型的问题，
 * 并实现了针对不同业务场景的差异化过期策略。
 * </p>
 *
 * @author Maiko7
 */
@Configuration
public class RedisCacheManagerConfig {

    /**
     * 注入 Redis 连接工厂
     * <p>
     * 由 Spring Boot 自动配置提供，用于创建与 Redis 服务器的连接。
     * </p>
     */
    @Resource
    private RedisConnectionFactory redisConnectionFactory;

    /**
     * 自定义 CacheManager Bean
     * <p>
     * 该方法构建并返回一个配置好的 {@link RedisCacheManager}。
     * 主要配置包括：
     * 1. 注册 JavaTimeModule 以支持 LocalDateTime 等时间类型的序列化。
     * 2. 设置默认的缓存过期时间为 30 分钟。
     * 3. 禁止缓存 null 值以防止缓存穿透。
     * 4. 指定 Key 使用 String 序列化，Value 使用 JSON 序列化。
     * 5. 针对特定缓存 "good_app_page" 单独设置 5 分钟的短过期时间。
     * </p>
     *
     * @return CacheManager 配置好的缓存管理器实例
     */
    @Bean
    public CacheManager cacheManager() {
        // 配置 ObjectMapper 以支持 Java 8 时间类型（如 LocalDateTime）的序列化与反序列化
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // 构建默认缓存配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)) // 设置默认过期时间为 30 分钟
                .disableCachingNullValues() // 禁用 null 值缓存，避免缓存穿透问题
                // key 使用 String 序列化器
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()));
//                // value 使用 JSON 序列化器（支持复杂对象）但是要注意开启后需要给序列化增加默认类型配置，否则无法反序列化
//                .serializeValuesWith(RedisSerializationContext.SerializationPair
//                        .fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapper)));

        // 构建 RedisCacheManager
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig) // 应用默认配置
                // 针对特定业务缓存 "good_app_page" 覆盖默认配置，设置较短的过期时间（5分钟）
                .withCacheConfiguration("good_app_page",
                        defaultConfig.entryTtl(Duration.ofMinutes(5)))
                .build();
    }
}