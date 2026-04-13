package com.maiko.maikoaicodemother;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
//@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.maiko.maikoaicodemother.mapper")
// http://localhost:8123/api/doc.html#/home
public class MaikoAiCodeMotherApplication {

    public static void main(String[] args) {
        SpringApplication.run(MaikoAiCodeMotherApplication.class, args);
    }

}
