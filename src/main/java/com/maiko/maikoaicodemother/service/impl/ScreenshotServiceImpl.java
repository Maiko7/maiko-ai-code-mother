package com.maiko.maikoaicodemother.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.maiko.maikoaicodemother.exception.ErrorCode;
import com.maiko.maikoaicodemother.exception.ThrowUtils;
import com.maiko.maikoaicodemother.manager.CosManager;
import com.maiko.maikoaicodemother.service.ScreenshotService;
import com.maiko.maikoaicodemother.utils.WebScreenshotUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 截图服务实现类
 *
 * 核心职责：
 * 1. 调用底层工具生成网页截图。
 * 2. 将截图上传到云端存储（COS）。
 * 3. 返回云端访问链接。
 * 4. 【关键】确保本地临时文件被彻底清理，防止服务器磁盘爆满。
 */
@Service
@Slf4j
public class ScreenshotServiceImpl implements ScreenshotService {

    @Resource
    private CosManager cosManager; // 腾讯云对象存储管理器

    /**
     * 生成截图并上传的主流程
     *
     * @param webUrl 需要截图的网页地址
     * @return 云端图片的访问链接
     */
    @Override
    public String generateAndUploadScreenshot(String webUrl) {
        // 1. 参数校验
        ThrowUtils.throwIf(StrUtil.isBlank(webUrl), ErrorCode.PARAMS_ERROR, "网页URL不能为空");
        log.info("开始生成网页截图，URL: {}", webUrl);

        // 2. 调用底层工具生成本地截图
        // 这一步会在服务器本地创建一个临时文件夹和一张图片
        String localScreenshotPath = WebScreenshotUtils.saveWebPageScreenshot(webUrl);

        // 如果生成失败（返回null或空），直接抛异常中断流程
        ThrowUtils.throwIf(StrUtil.isBlank(localScreenshotPath), ErrorCode.OPERATION_ERROR, "本地截图生成失败");

        try {
            // 3. 尝试上传到云端
            String cosUrl = uploadScreenshotToCos(localScreenshotPath);

            // 如果上传失败，抛异常
            ThrowUtils.throwIf(StrUtil.isBlank(cosUrl), ErrorCode.OPERATION_ERROR, "截图上传对象存储失败");

            log.info("网页截图生成并上传成功: {} -> {}", webUrl, cosUrl);
            return cosUrl; // 返回给前端

        } finally {
            // 4. 【兜底清理】
            // 无论上面是成功还是失败，finally 里的代码一定会执行
            // 必须把刚才生成的本地临时文件删掉，否则服务器硬盘迟早被撑爆
            cleanupLocalFile(localScreenshotPath);
        }
    }

    /**
     * 上传截图到对象存储 (COS)
     *
     * @param localScreenshotPath 本地截图路径
     * @return 对象存储访问URL，失败返回null
     */
    private String uploadScreenshotToCos(String localScreenshotPath) {
        if (StrUtil.isBlank(localScreenshotPath)) {
            return null;
        }

        File screenshotFile = new File(localScreenshotPath);

        // 双重检查：确保文件真的存在
        if (!screenshotFile.exists()) {
            log.error("截图文件不存在: {}", localScreenshotPath);
            return null;
        }

        // 1. 生成唯一的文件名
        // 使用 UUID 的前8位防止重名
        String fileName = UUID.randomUUID().toString().substring(0, 8) + "_compressed.jpg";

        // 2. 生成 COS 的对象键（相当于云端的文件路径）
        String cosKey = generateScreenshotKey(fileName);

        // 3. 调用 COS 管理器上传
        return cosManager.uploadFile(cosKey, screenshotFile);
    }

    /**
     * 生成截图的对象存储键
     *
     * 目的：按日期归档，方便管理
     * 格式示例：/screenshots/2026/04/16/a1b2c3d4_compressed.jpg
     */
    private String generateScreenshotKey(String fileName) {
        // 获取今天的日期，格式化为 yyyy/MM/dd
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        // 修改后：直接去掉斜杠 上面的是/screenshots/2026/04/16/a1b2c3d4_compressed.jpg 你看看这多少层级
        // 下面这个就是/screenshots/20260416/a1b2c3d4_compressed.jpg
//        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 拼接完整路径
        return String.format("/screenshots/%s/%s", datePath, fileName);
    }

    /**
     * 清理本地文件
     *
     * @param localFilePath 本地文件路径
     */
    private void cleanupLocalFile(String localFilePath) {
        File localFile = new File(localFilePath);

        // 只有文件存在时才删除
        if (localFile.exists()) {
            // 【关键点】
            // 这里不仅删除了文件本身，还删除了它的父目录（因为 WebScreenshotUtils 会为每张图建一个随机UUID文件夹）
            // 如果不删父目录，tmp/screenshots 下会堆积成千上万个空文件夹
            File parentDir = localFile.getParentFile();
            FileUtil.del(parentDir);

            log.info("本地截图文件及目录已清理: {}", localFilePath);
        }
    }
}