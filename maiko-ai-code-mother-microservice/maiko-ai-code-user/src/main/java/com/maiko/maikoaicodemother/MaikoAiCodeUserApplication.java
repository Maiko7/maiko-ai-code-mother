package com.maiko.maikoaicodemother;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.maiko.maikoaicodemother.mapper")
@ComponentScan("com.maiko")
public class MaikoAiCodeUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(MaikoAiCodeUserApplication.class, args);
    }
}