package com.maiko.maikoaicodemother.ai;

import com.maiko.maikoaicodemother.ai.model.HtmlCodeResult;
import com.maiko.maikoaicodemother.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author: Maiko7
 * @create: 2026-04-11 10:54
 */
@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Test
    void generateHtmlCode() {
//        String result = aiCodeGeneratorService.generateHtmlCode("做个不会写代码的Maiko博客，不超过20行");
        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode("做个不会写代码的Maiko博客，不超过20行");
        Assertions.assertNotNull(result);
    }

    @Test
    void generateMultiFileCode() {
        MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode("做个不会写代码的Maiko留言板，不超过50行");
        Assertions.assertNotNull(result);
    }
}