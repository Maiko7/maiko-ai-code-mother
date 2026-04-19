package com.maiko.maikoaicodemother.utils;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.maiko.maikoaicodemother.exception.BusinessException;
import com.maiko.maikoaicodemother.exception.ErrorCode;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.util.UUID;

/**
 * 网页截图工具类 (ThreadLocal 修正版)
 *
 * 【核心改进】：
 * 废弃了全局静态 WebDriver 单例，改用 ThreadLocal。
 * 原因：防止多线程并发时，不同请求抢占同一个浏览器实例，导致“张冠李戴”（A请求截到了B请求的图）。
 */
@Slf4j
public class WebScreenshotUtils {

    /**
     * ThreadLocal 变量：为每个线程绑定一个独立的 WebDriver 实例
     * 就像给每个厨师配了一把专属菜刀，而不是大家共用一把。
     * 防止多线程的问题。比如webDriver假设有两个线程，第一个截图page1第二个截图page2最终可能会导致第一个请求截错了，截成了page2，
     * 因为假设第一个线程进来它打开的是page1，这个时候第二个线程进来了，由于用的是同一个WebDriver有可能之前打开的页面被覆盖掉了。变成了page2。
     */
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    /**
     * 获取当前线程的 WebDriver
     * 如果当前线程没有，就创建一个新的；如果有，直接复用。
     */
    private static WebDriver getDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver == null) {
            log.debug("当前线程首次请求截图，初始化新的 ChromeDriver");
            driver = initChromeDriver(1600, 900);
            driverThreadLocal.set(driver);
        }
        return driver;
    }

    /**
     * 清理当前线程的 WebDriver
     * 任务完成后必须调用，防止内存泄漏。
     */
    private static void removeDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            try {
                log.debug("正在关闭并移除当前线程的 ChromeDriver");
                driver.quit();
            } catch (Exception e) {
                log.error("关闭 WebDriver 时发生异常", e);
            } finally {
                // 无论如何，必须从 ThreadLocal 中移除引用
                driverThreadLocal.remove();
            }
        }
    }

    /**
     * 生成网页截图的主流程
     *
     * @param webUrl 需要截图的网页URL
     * @return 压缩后的截图文件路径，如果失败返回 null
     */
    public static String saveWebPageScreenshot(String webUrl) {
        // 1. 参数校验
        if (StrUtil.isBlank(webUrl)) {
            log.error("网页URL不能为空");
            return null;
        }

        // 获取当前线程专属的浏览器
        WebDriver currentDriver = getDriver();

        try {
            // 2. 准备存储目录
            String rootPath = System.getProperty("user.dir") + File.separator + "tmp" + File.separator + "screenshots"
                    + File.separator + UUID.randomUUID().toString().substring(0, 8);
            FileUtil.mkdir(rootPath);

            final String IMAGE_SUFFIX = ".png";
            String imageSavePath = rootPath + File.separator + RandomUtil.randomNumbers(5) + IMAGE_SUFFIX;

            // 3. 浏览器执行动作：打开网页
            // 【安全】：因为 currentDriver 是当前线程私有的，不用担心被别人篡改 URL
            log.info("线程 [{}] 正在访问网页: {}", Thread.currentThread().getName(), webUrl);
            currentDriver.get(webUrl);

            // 4. 等待页面资源加载
            waitForPageLoad(currentDriver);

            // 5. 执行截图
            byte[] screenshotBytes = ((TakesScreenshot) currentDriver).getScreenshotAs(OutputType.BYTES);

            // 6. 保存原始图片
            saveImage(screenshotBytes, imageSavePath);
            log.info("原始截图保存成功: {}", imageSavePath);

            // 7. 图片压缩
            final String COMPRESSION_SUFFIX = "_compressed.jpg";
            String compressedImagePath = rootPath + File.separator + RandomUtil.randomNumbers(5) + COMPRESSION_SUFFIX;

            compressImage(imageSavePath, compressedImagePath);
            log.info("压缩图片保存成功: {}", compressedImagePath);

            // 8. 清理现场（本地文件）
            FileUtil.del(imageSavePath);

            return compressedImagePath;

        } catch (Exception e) {
            log.error("网页截图失败: {}", webUrl, e);
            return null;
        } finally {
            // 【关键一步】：无论成功还是失败，都要把浏览器关掉并从 ThreadLocal 移除
            // 因为我们是在异步线程或短生命周期的业务中调用，不需要长期持有浏览器实例
            removeDriver();
        }
    }

    /**
     * 初始化 Chrome 浏览器驱动
     */
    private static WebDriver initChromeDriver(int width, int height) {
        try {
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments(String.format("--window-size=%d,%d", width, height));
            options.addArguments("--disable-extensions");
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

            WebDriver driver = new ChromeDriver(options);
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

            log.info("Chrome 浏览器驱动初始化成功");
            return driver;

        } catch (Exception e) {
            log.error("初始化 Chrome 浏览器失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "初始化 Chrome 浏览器失败");
        }
    }

    private static void saveImage(byte[] imageBytes, String imagePath) {
        try {
            FileUtil.writeBytes(imageBytes, imagePath);
        } catch (Exception e) {
            log.error("保存图片失败: {}", imagePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存图片失败");
        }
    }

    private static void compressImage(String originalImagePath, String compressedImagePath) {
        final float COMPRESSION_QUALITY = 0.3f;
        try {
            ImgUtil.compress(
                    FileUtil.file(originalImagePath),
                    FileUtil.file(compressedImagePath),
                    COMPRESSION_QUALITY
            );
        } catch (Exception e) {
            log.error("压缩图片失败: {} -> {}", originalImagePath, compressedImagePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "压缩图片失败");
        }
    }

    private static void waitForPageLoad(WebDriver driver) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(webDriver ->
                    ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete")
            );
            Thread.sleep(2000);
            log.info("页面加载完成，准备截图");
        } catch (Exception e) {
            log.error("等待页面加载超时或出现异常，将继续尝试截图（画面可能不完整）", e);
        }
    }
}
//package com.maiko.maikoaicodemother.utils;
//
//import cn.hutool.core.img.ImgUtil;
//import cn.hutool.core.io.FileUtil;
//import cn.hutool.core.util.RandomUtil;
//import cn.hutool.core.util.StrUtil;
//import com.maiko.maikoaicodemother.exception.BusinessException;
//import com.maiko.maikoaicodemother.exception.ErrorCode;
//import io.github.bonigarcia.wdm.WebDriverManager;
//import jakarta.annotation.PreDestroy;
//import lombok.extern.slf4j.Slf4j;
//import org.openqa.selenium.JavascriptExecutor;
//import org.openqa.selenium.OutputType;
//import org.openqa.selenium.TakesScreenshot;
//import org.openqa.selenium.WebDriver;
//import org.openqa.selenium.chrome.ChromeDriver;
//import org.openqa.selenium.chrome.ChromeOptions;
//import org.openqa.selenium.support.ui.WebDriverWait;
//
//import java.io.File;
//import java.time.Duration;
//import java.util.UUID;
//
///**
// * 网页截图工具类
// *
// * 核心功能：
// * 使用 Selenium 操控一个“无头浏览器”（没有界面的 Chrome），
// * 专门用于给 AI 生成的网页链接生成预览缩略图。
// */
//@Slf4j
//public class WebScreenshotUtils {
//
//    /**
//     * 浏览器驱动实例
//     * 注意：这里使用了静态代码块和静态变量，意味着整个 Spring 应用只会有一个浏览器实例（单例模式）。
//     * 这样做是为了节省资源，避免频繁启动和关闭浏览器。
//     */
//    private static final WebDriver webDriver;
//
//    /**
//     * 静态代码块：在类加载时只执行一次
//     */
//    static {
//        final int DEFAULT_WIDTH = 1600;  // 默认截图宽度
//        final int DEFAULT_HEIGHT = 900;  // 默认截图高度
//        // 初始化浏览器，并设置好窗口大小
//        webDriver = initChromeDriver(DEFAULT_WIDTH, DEFAULT_HEIGHT);
//    }
//
//    /**
//     * Spring 容器销毁前的回调方法
//     * 当服务器关闭或重启时，自动执行此方法，确保浏览器进程被正确关闭，防止残留僵尸进程
//     */
//    @PreDestroy
//    public void destroy() {
//        log.info("正在关闭 Selenium 浏览器驱动...");
//        webDriver.quit();
//    }
//
//    /**
//     * 初始化 Chrome 浏览器驱动
//     *
//     * @param width  窗口宽度
//     * @param height 窗口高度
//     * @return WebDriver 实例
//     */
//    private static WebDriver initChromeDriver(int width, int height) {
//        try {
//            // 1. 自动管理驱动版本
//            // 自动下载并匹配当前机器上 Chrome 浏览器版本的 ChromeDriver，避免版本不兼容问题
//            WebDriverManager.chromedriver().setup();
//
//            // 2. 配置 Chrome 选项（关键步骤）
//            ChromeOptions options = new ChromeOptions();
//
//            // 【无头模式】
//            // 核心参数：不显示浏览器界面，在后台运行。
//            // 作用：服务器通常没有显示器，且为了节省资源，必须在“无头”模式下运行。
//            options.addArguments("--headless");
//
//            // 【禁用 GPU】
//            // 作用：在无头模式下，某些系统（尤其是 Linux）可能会出现 GPU 相关的问题，禁用它可以避免报错。
//            options.addArguments("--disable-gpu");
//
//            // 【禁用沙盒模式】
//            // 作用：在 Docker 容器或某些受限的 Linux 环境中运行时，必须添加此参数，否则会启动失败。
//            options.addArguments("--no-sandbox");
//
//            // 【禁用 /dev/shm 使用】
//            // 作用：解决在 Docker 环境中因共享内存太小（默认 64MB）导致浏览器崩溃的问题。
//            options.addArguments("--disable-dev-shm-usage");
//
//            // 【设置窗口大小】
//            // 作用：强制设置浏览器视口大小，确保每次截图的分辨率一致（1600x900）。
//            options.addArguments(String.format("--window-size=%d,%d", width, height));
//
//            // 【禁用扩展】
//            // 作用：禁用所有浏览器插件，保证环境纯净，避免插件干扰截图。
//            options.addArguments("--disable-extensions");
//
//            // 【设置用户代理】
//            // 作用：伪装成 Windows 10 上的 Chrome 浏览器，防止某些网站针对移动端或爬虫返回不同的页面结构。
//            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
//
//            // 3. 创建驱动实例
//            WebDriver driver = new ChromeDriver(options);
//
//            // 4. 设置超时时间
//            // 页面加载超时：如果 30 秒内页面没加载完，抛出异常
//            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
//            // 隐式等待：查找页面元素时，最多等待 10 秒
//            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
//
//            log.info("Chrome 浏览器驱动初始化成功");
//            return driver;
//
//        } catch (Exception e) {
//            log.error("初始化 Chrome 浏览器失败", e);
//            // 抛出业务异常，提示系统错误
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "初始化 Chrome 浏览器失败");
//        }
//    }
//
//    /**
//     * 生成网页截图的主流程
//     *
//     * @param webUrl 需要截图的网页URL
//     * @return 压缩后的截图文件路径，如果失败返回 null
//     */
//    public static String saveWebPageScreenshot(String webUrl) {
//        // 1. 参数校验：防止空指针异常
//        if (StrUtil.isBlank(webUrl)) {
//            log.error("网页URL不能为空");
//            return null;
//        }
//
//        try {
//            // 2. 准备存储目录
//            // 在当前项目根目录下创建临时文件夹：./tmp/screenshots/{随机UUID前8位}/
//            // 使用 UUID 是为了防止并发截图时文件名冲突
//            String rootPath = System.getProperty("user.dir") + File.separator + "tmp" + File.separator + "screenshots"
//                    + File.separator + UUID.randomUUID().toString().substring(0, 8);
//            FileUtil.mkdir(rootPath);
//
//            // 3. 定义原始图片路径
//            final String IMAGE_SUFFIX = ".png";
//            // 使用随机数字命名，防止覆盖
//            String imageSavePath = rootPath + File.separator + RandomUtil.randomNumbers(5) + IMAGE_SUFFIX;
//
//            // 4. 浏览器执行动作：打开网页
//            log.info("正在访问网页: {}", webUrl);
//            webDriver.get(webUrl);
//
//            // 5. 等待页面资源加载
//            // 这一步很关键，否则截到的可能是白屏或加载动画
//            waitForPageLoad(webDriver);
//
//            // 6. 执行截图
//            // 将浏览器视窗内容转换为字节数组（内存中）
//            byte[] screenshotBytes = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.BYTES);
//
//            // 7. 保存原始高清大图
//            saveImage(screenshotBytes, imageSavePath);
//            log.info("原始截图保存成功: {}", imageSavePath);
//
//            // 8. 图片压缩
//            // 为了节省带宽和存储空间，我们将 PNG 转为低质量的 JPG
//            final String COMPRESSION_SUFFIX = "_compressed.jpg";
//            String compressedImagePath = rootPath + File.separator + RandomUtil.randomNumbers(5) + COMPRESSION_SUFFIX;
//
//            compressImage(imageSavePath, compressedImagePath);
//            log.info("压缩图片保存成功: {}", compressedImagePath);
//
//            // 9. 清理现场
//            // 删除几百 KB 的原始 PNG，只保留几十 KB 的压缩 JPG
//            FileUtil.del(imageSavePath);
//
//            // 10. 返回最终文件路径
//            return compressedImagePath;
//
//        } catch (Exception e) {
//            log.error("网页截图失败: {}", webUrl, e);
//            return null;
//        }
//    }
//
//    /**
//     * 保存图片到文件
//     *
//     * @param imageBytes 图片二进制数据
//     * @param imagePath  保存路径
//     */
//    private static void saveImage(byte[] imageBytes, String imagePath) {
//        try {
//            // 使用 Hutool 工具类直接将字节写入文件
//            FileUtil.writeBytes(imageBytes, imagePath);
//        } catch (Exception e) {
//            log.error("保存图片失败: {}", imagePath, e);
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存图片失败");
//        }
//    }
//
//    /**
//     * 压缩图片
//     * 目的：AI生成的网页截图通常很大（PNG格式），作为缩略图展示时不需要这么高的质量。
//     * 将其转换为 JPG 并降低画质，可以大幅减小文件体积（例如从 500KB 降到 50KB）。
//     *
//     * @param originalImagePath   原图路径
//     * @param compressedImagePath 压缩后路径
//     */
//    private static void compressImage(String originalImagePath, String compressedImagePath) {
//        // 压缩质量系数：0.0f - 1.0f
//        // 0.3f 表示 30% 的质量，对于网页缩略图来说已经足够清晰了
//        final float COMPRESSION_QUALITY = 0.3f;
//
//        try {
//            ImgUtil.compress(
//                    FileUtil.file(originalImagePath), // 输入文件
//                    FileUtil.file(compressedImagePath), // 输出文件
//                    COMPRESSION_QUALITY // 质量参数
//            );
//        } catch (Exception e) {
//            log.error("压缩图片失败: {} -> {}", originalImagePath, compressedImagePath, e);
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "压缩图片失败");
//        }
//    }
//
//    /**
//     * 等待页面加载完成
//     * 这是一个“防御性”编程，确保 DOM 树完全构建好再截图
//     *
//     * @param driver 浏览器驱动
//     */
//    private static void waitForPageLoad(WebDriver driver) {
//        try {
//            // 1. 显式等待：最多等 10 秒
//            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
//
//            // 2. 检查 JS 状态
//            // 通过执行 JavaScript 获取 document.readyState
//            // 当状态变为 "complete" 时，说明 HTML 已解析完毕
//           wait.until(webDriver ->
//               ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete")
//           );
//
//            // 3. 额外缓冲时间
//            // 即使 DOM 好了，有些图片、CSS 动画或者 AI 渲染的内容可能还在慢加载
//            // 强制再睡 2 秒，确保画面稳定
//            Thread.sleep(2000);
//
//            log.info("页面加载完成，准备截图");
//        } catch (Exception e) {
//            // 注意：这里不抛异常，因为有时候网络慢，我们不想让整个程序卡死
//            // 哪怕没加载完，也尽量截一张图出来
//            log.error("等待页面加载超时或出现异常，将继续尝试截图（画面可能不完整）", e);
//        }
//    }
//
//
//}