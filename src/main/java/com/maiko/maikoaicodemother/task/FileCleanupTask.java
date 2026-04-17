package com.maiko.maikoaicodemother.task;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 文件清理定时任务
 * <p>
 * 定期清理本地临时文件，防止服务器磁盘空间被占满
 * </p>
 *
 * @author Maiko7
 */
@Component
@Slf4j
public class FileCleanupTask {

    /**
     * 每天凌晨2点执行清理任务
     * cron表达式: 秒 分 时 日 月 周
     * 0 0 2 * * ? = 每天凌晨2点0分0秒执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupTempFiles() {
        log.info("========== 开始执行临时文件清理任务 ==========");

        try {
            // 1. 清理截图临时文件（超过3天的）
            cleanupScreenshotTempFiles();

            // 2. 清理代码生成临时文件（超过7天的）
            cleanupCodeOutputTempFiles();

            // 3. 清理部署临时文件（可选，谨慎使用）
            // cleanupDeployTempFiles();

            log.info("========== 临时文件清理任务执行完成 ==========");
        } catch (Exception e) {
            log.error("临时文件清理任务执行异常", e);
        }
    }

    /**
     * 清理截图临时文件
     * 目录: tmp/screenshots
     * 策略: 删除超过3天的文件夹
     */
    private void cleanupScreenshotTempFiles() {
        String screenshotsDir = System.getProperty("user.dir") + File.separator + "tmp" + File.separator + "screenshots";
        cleanupOldDirectories(screenshotsDir, 3, "截图临时文件");
    }

    /**
     * 清理代码生成临时文件
     * 目录: tmp/code_output
     * 策略: 删除超过7天的文件夹
     */
    private void cleanupCodeOutputTempFiles() {
        String codeOutputDir = System.getProperty("user.dir") + File.separator + "tmp" + File.separator + "code_output";
        cleanupOldDirectories(codeOutputDir, 7, "代码生成临时文件");
    }

    /**
     * 通用目录清理方法：根据保留天数，自动删除过期的子文件夹
     * <p>
     * 核心逻辑：扫描指定目录下的所有子文件夹，计算它们的“年龄”。
     * 如果某个子文件夹的最后修改时间距离现在超过了指定的保留天数，就把它删掉。
     * </p>
     *
     * @param dirPath       需要打扫的根目录路径（例如：.../tmp/screenshots）
     * @param daysToKeep    允许保留的天数阈值（只要文件夹比这个天数老，就会被删除）
     * @param description   业务描述（用于日志里说人话，比如“截图临时文件”）
     */
    private void cleanupOldDirectories(String dirPath, int daysToKeep, String description) {
        // 1. 创建文件对象，准备检查
        File directory = new File(dirPath);

        // 2. 安全检查：先看看地盘还在不在
        // 如果目录都不存在，或者它根本不是个目录（可能是个文件），那就别费劲了，直接下班
        if (!directory.exists() || !directory.isDirectory()) {
            log.debug("{}目录不存在，跳过清理: {}", description, dirPath);
            return;
        }

        // 3. 获取该目录下所有的【子文件夹】
        // 注意：这里只查文件夹，不管单独的文件
        // listFiles(File::isDirectory) 就像个过滤器，只把文件夹挑出来
        File[] subDirs = directory.listFiles(File::isDirectory);

        // 如果是空目录，或者获取失败，也直接返回
        if (subDirs == null || subDirs.length == 0) {
            log.debug("{}目录为空，无需清理: {}", description, dirPath);
            return;
        }

        int deletedCount = 0;
        long now = System.currentTimeMillis();

        // 4. 计算“过期红线”：把保留天数转换成毫秒
        // 比如保留3天，这里就是 3 * 24 * 60 * 60 * 1000 毫秒
        long thresholdMillis = daysToKeep * 24L * 60 * 60 * 1000;

        for (File subDir : subDirs) {
            try {
                // 5. 计算当前文件夹的“年龄”
                long lastModified = subDir.lastModified(); // 文件夹最后一次被修改的时间
                long ageInMillis = now - lastModified;     // 现在的年龄 = 当前时间 - 出生时间

                // 6. 判决时刻：如果文件夹的年龄 > 允许保留的期限，那就是“过期”了
                if (ageInMillis > thresholdMillis) {
                    // 执行删除操作（连锅端，文件夹及里面所有内容都删掉）
                    FileUtil.del(subDir);
                    deletedCount++;

                    // 记录日志：把毫秒时间戳转成人类能看懂的日期格式（如 2026-04-13）
                    log.debug("已删除过期{}: {} (最后修改: {})",
                            description,
                            subDir.getAbsolutePath(),
                            LocalDate.ofEpochDay(lastModified / 86400000)
                                    .format(DateTimeFormatter.ISO_LOCAL_DATE));
                }
            } catch (Exception e) {
                // 万一删失败了（比如文件正被占用），记个错误日志，别影响后面继续删
                log.error("删除{}失败: {}", description, subDir.getAbsolutePath(), e);
            }
        }

        // 7. 任务结束，汇报战果
        log.info("{}清理完成，共删除 {} 个过期目录", description, deletedCount);
    }
    /**
     * 手动触发清理任务（可用于测试或紧急清理）
     * 可通过 Controller 调用此方法
     */
    public void manualCleanup() {
        log.info("手动触发临时文件清理任务");
        cleanupTempFiles();
    }
}
