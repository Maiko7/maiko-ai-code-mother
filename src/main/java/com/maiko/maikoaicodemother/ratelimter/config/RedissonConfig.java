package com.maiko.maikoaicodemother.ratelimter.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 配置类
 * <p>
 * 用于创建 RedissonClient Bean，提供分布式锁、分布式集合等高级 Redis 功能。
 *
 * @author Maiko7
 */
@Configuration
public class RedissonConfig {

    // 从 application.yml 中读取 Redis 基础配置
    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private Integer redisPort;

    @Value("${spring.data.redis.password}")
    private String redisPassword;

    @Value("${spring.data.redis.database}")
    private Integer redisDatabase;

    /**
     * 创建 RedissonClient 实例
     *
     * @return RedissonClient Bean
     */
    @Bean
    public RedissonClient redissonClient() {
        // 1. 创建配置对象
        Config config = new Config();

        // 2. 拼接 Redis 地址，注意必须包含协议前缀 "redis://"
        String address = "redis://" + redisHost + ":" + redisPort;

        // 3. 配置单机模式（Single Server Mode）
        SingleServerConfig singleServerConfig = config.useSingleServer()
                .setAddress(address)                  // 设置 Redis 地址
                .setDatabase(redisDatabase)           // 设置使用的数据库索引 (0-15)
                .setConnectionMinimumIdleSize(1)      // 设置连接池最小空闲连接数
                .setConnectionPoolSize(10)            // 设置连接池最大连接数
                .setIdleConnectionTimeout(30000)      // 空闲连接超时时间 (毫秒)
                .setConnectTimeout(5000)              // 连接超时时间 (毫秒)
                .setTimeout(3000)                     // 命令等待响应的超时时间 (毫秒)
                .setRetryAttempts(3)                  // 命令失败重试次数
                .setRetryInterval(1500);              // 重试间隔时间 (毫秒)

        // 4. 密码处理：如果有密码则设置，防止无密码时覆盖默认值
        if (redisPassword != null && !redisPassword.isEmpty()) {
            singleServerConfig.setPassword(redisPassword);
        }

        // 5. 创建并返回 RedissonClient 实例
        return Redisson.create(config);
    }
}