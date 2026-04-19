package com.maiko.maikoaicodemother;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
public class MaikoAiCodeScreenshotApplication {
    public static void main(String[] args) {
        SpringApplication.run(MaikoAiCodeScreenshotApplication.class, args);
    }
}