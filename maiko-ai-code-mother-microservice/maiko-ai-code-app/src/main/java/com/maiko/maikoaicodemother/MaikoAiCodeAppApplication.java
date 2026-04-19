package com.maiko.maikoaicodemother;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * @author: Maiko7
 * @create: 2026-04-19 10:08
 */
@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
@MapperScan("com.maiko.maikoaicodemother.mapper")
@EnableDubbo
@EnableCaching
public class MaikoAiCodeAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(MaikoAiCodeAppApplication.class, args);
    }
}
