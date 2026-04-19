package com.maiko.maikoaicodemother.core.builder;

import cn.hutool.core.util.RuntimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Vue 项目构建器
 * 负责调用系统 npm 命令异步构建 Vue 前端项目
 */
@Slf4j
@Component
public class VueProjectBuilder {

    /**
     * 异步构建 Vue 项目
     * 使用虚拟线程在后台执行，不阻塞主线程
     *
     * @param projectPath 项目根目录路径
     */
    public void buildProjectAsync(String projectPath) {
        // 使用 Java 21 虚拟线程创建后台任务
        // 线程命名格式：vue-builder-时间戳，便于日志追踪
        /**
         * 展开式
         * // 1. 先造一个“线程建造器”
         * Thread.Builder builder = Thread.ofVirtual();
         *
         * // 2. 给建造器起个名字（方便出事了查日志）
         * builder.name("vue-builder-" + System.currentTimeMillis());
         *
         * // 3. 造出那个“任务”（Runnable）
         * // 以前用 () -> { ... } 简写，现在展开写
         * Runnable task = new Runnable() {
         *     @Override
         *     public void run() {
         *         try {
         *             // 执行实际的构建逻辑
         *             buildProject(projectPath);
         *         } catch (Exception e) {
         *             // 捕获并记录构建过程中的异常，防止线程意外终止
         *             log.error("异步构建 Vue 项目时发生异常: {}", e.getMessage(), e);
         *         }
         *     }
         * };
         *
         * // 4. 真正开始造车（创建线程对象）
         * // 注意：这里还没启动，只是创建
         * Thread virtualThread = builder.start(task);
         * // 注意：其实 builder.start(task) 是一步到位的，相当于创建并启动。
         * // 如果非要完全拆开，是 builder.unstarted(task).start();
         */
        Thread.ofVirtual().name("vue-builder-" + System.currentTimeMillis())
                .start(() -> {
                    try {
                        // 执行实际的构建逻辑
                        buildProject(projectPath);
                    } catch (Exception e) {
                        // 捕获并记录构建过程中的异常，防止线程意外终止
                        log.error("异步构建 Vue 项目时发生异常: {}", e.getMessage(), e);
                    }
                });
    }

    /**
     * 构建 Vue 项目核心流程
     *
     * @param projectPath 项目根目录路径
     * @return 是否构建成功
     */
    public boolean buildProject(String projectPath) {
        File projectDir = new File(projectPath);

        // 1. 校验项目目录是否存在
        if (!projectDir.exists() || !projectDir.isDirectory()) {
            log.error("项目目录不存在：{}", projectPath);
            return false;
        }

        // 2. 校验 package.json 是否存在（确保是 npm 项目）
        File packageJsonFile = new File(projectDir, "package.json");
        if (!packageJsonFile.exists()) {
            log.error("项目目录中没有 package.json 文件：{}", projectPath);
            return false;
        }

        log.info("开始构建 Vue 项目：{}", projectPath);

        // 3. 执行 npm install 安装依赖
        if (!executeNpmInstall(projectDir)) {
            log.error("npm install 执行失败：{}", projectPath);
            return false;
        }

        // 4. 执行 npm run build 打包构建
        if (!executeNpmBuild(projectDir)) {
            log.error("npm run build 执行失败：{}", projectPath);
            return false;
        }

        // 5. 验证构建产物 dist 目录是否生成
        File distDir = new File(projectDir, "dist");
        if (!distDir.exists() || !distDir.isDirectory()) {
            log.error("构建完成但 dist 目录未生成：{}", projectPath);
            return false;
        }

        log.info("Vue 项目构建成功，dist 目录：{}", projectPath);
        return true;
    }

    /**
     * 执行 npm install 命令
     * 超时时间：300秒（5分钟）
     */
    private boolean executeNpmInstall(File projectDir) {
        log.info("执行 npm install...");
        // 构造命令：Windows下使用 npm.cmd，Linux/Mac下使用 npm
        String command = String.format("%s install", buildCommand("npm"));
        return executeCommand(projectDir, command, 300);
    }

    /**
     * 执行 npm run build 命令
     * 超时时间：180秒（3分钟）
     */
    private boolean executeNpmBuild(File projectDir) {
        log.info("执行 npm run build...");
        String command = String.format("%s run build", buildCommand("npm"));
        return executeCommand(projectDir, command, 180);
    }

    /**
     * 根据操作系统类型构造可执行命令
     * Windows 系统需要 .cmd 后缀
     *
     * @param baseCommand 基础命令名（如 npm）
     * @return 适配操作系统的命令
     */
    private String buildCommand(String baseCommand) {
        if (isWindows()) {
            return baseCommand + ".cmd";
        }
        return baseCommand;
    }

    /**
     * 判断当前操作系统是否为 Windows
     *
     * @return true 表示 Windows 系统
     */
    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    /**
     * 通用命令执行器
     *
     * @param workingDir     工作目录（命令在此目录下执行）
     * @param command        要执行的命令字符串
     * @param timeoutSeconds 超时时间（秒）
     * @return 是否执行成功（退出码为0）
     */
    private boolean executeCommand(File workingDir, String command, int timeoutSeconds) {
        try {
            log.info("在目录 {} 中执行命令: {}", workingDir.getAbsolutePath(), command);

            // 使用 Hutool 工具类执行系统命令
            // split("\\s+") 将命令按空格分割成数组，避免参数解析错误
            Process process = RuntimeUtil.exec(
                    null,
                    workingDir,
                    command.split("\\s+")
            );

            // 等待命令执行完成，设置超时保护
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                // 超时则强制终止进程
                log.error("命令执行超时（{}秒），强制终止进程", timeoutSeconds);
                process.destroyForcibly();
                return false;
            }

            // 获取进程退出码，0 表示成功
            int exitCode = process.exitValue();
            if (exitCode == 0) {
                log.info("命令执行成功: {}", command);
                return true;
            } else {
                log.error("命令执行失败，退出码: {}", exitCode);
                return false;
            }
        } catch (Exception e) {
            log.error("执行命令失败: {}, 错误信息: {}", command, e.getMessage());
            return false;
        }
    }

}