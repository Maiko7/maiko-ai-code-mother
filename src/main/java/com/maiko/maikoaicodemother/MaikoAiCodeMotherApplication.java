package com.maiko.maikoaicodemother;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.maiko.maikoaicodemother.mapper")
// http://localhost:8123/api/doc.html#/home
public class MaikoAiCodeMotherApplication {

    public static void main(String[] args) {
        SpringApplication.run(MaikoAiCodeMotherApplication.class, args);
    }

}
