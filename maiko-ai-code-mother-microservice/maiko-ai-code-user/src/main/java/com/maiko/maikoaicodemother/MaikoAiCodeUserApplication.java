package com.maiko.maikoaicodemother;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDubbo
@MapperScan("com.maiko.maikoaicodemother.mapper")
@ComponentScan("com.maiko")
public class MaikoAiCodeUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(MaikoAiCodeUserApplication.class, args);
    }
}