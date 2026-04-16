package com.maiko.maikoaicodemother.task;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author: Maiko7
 * @create: 2026-04-16 10:52
 */
@SpringBootTest
class FileCleanupTaskTest {
    @Resource
    private FileCleanupTask fileCleanupTask;

    @Test
    void testManualCleanup() {
        fileCleanupTask.manualCleanup();
    }
}