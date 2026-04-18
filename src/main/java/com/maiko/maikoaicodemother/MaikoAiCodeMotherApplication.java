package com.maiko.maikoaicodemother;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
//@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.maiko.maikoaicodemother.mapper")
@EnableScheduling // 启用定时任务
@EnableCaching
// http://localhost:8123/api/doc.html#/home
public class MaikoAiCodeMotherApplication {

    public static void main(String[] args) {
        SpringApplication.run(MaikoAiCodeMotherApplication.class, args);
    }

}
