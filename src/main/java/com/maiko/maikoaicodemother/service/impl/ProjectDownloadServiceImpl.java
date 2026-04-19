package com.maiko.maikoaicodemother.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.maiko.maikoaicodemother.exception.BusinessException;
import com.maiko.maikoaicodemother.exception.ErrorCode;
import com.maiko.maikoaicodemother.exception.ThrowUtils;
import com.maiko.maikoaicodemother.service.ProjectDownloadService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileFilter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Set;

/**
 * 项目下载服务实现类
 * 负责将指定目录打包为 ZIP 文件并通过 HTTP 响应返回给客户端
 */
@Service
@Slf4j
public class ProjectDownloadServiceImpl implements ProjectDownloadService {

    /**
     * 需要过滤的文件和目录名称集合
     * 用于排除构建工具、版本控制、IDE配置等不需要下载的冗余文件
     */
    private static final Set<String> IGNORED_NAMES = Set.of(
            "node_modules",   // Node.js依赖目录
            ".git",           // Git版本控制目录
            "dist",           // 构建输出目录
            "build",          // 编译输出目录
            ".DS_Store",      // macOS系统文件
            ".env",           // 环境变量配置文件（通常包含敏感信息）
            "target",         // Maven构建输出目录
            ".mvn",           // Maven配置目录
            ".idea",          // IntelliJ IDEA配置目录
            ".vscode"         // VS Code配置目录
    );

    /**
     * 需要过滤的文件扩展名集合
     * 用于排除日志、临时文件、缓存文件等非必要文件
     */
    private static final Set<String> IGNORED_EXTENSIONS = Set.of(
            ".log",           // 日志文件
            ".tmp",           // 临时文件
            ".cache"          // 缓存文件
    );

    /**
     * 检查指定路径是否允许包含在压缩包中
     * 通过递归检查路径中的每一级目录/文件名来判断是否需要过滤
     *
     * @param projectRoot 项目根目录的Path对象
     * @param fullPath    待检查文件的完整路径
     * @return true表示允许包含，false表示需要过滤
     */
    private boolean isPathAllowed(Path projectRoot, Path fullPath) {
        // 计算相对于项目根目录的相对路径
        Path relativePath = projectRoot.relativize(fullPath);
        // 遍历相对路径中的每一部分（目录或文件名）
        for (Path part : relativePath) {
            String partName = part.toString();
            // 检查当前部分是否在忽略名称列表中
            if (IGNORED_NAMES.contains(partName)) {
                return false;
            }
            // 检查当前部分的扩展名是否在忽略扩展名列表中
            if (IGNORED_EXTENSIONS.stream().anyMatch(partName::endsWith)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 将指定项目目录打包为ZIP文件并通过HTTP响应下载
     * 该方法会过滤掉预设的目录和文件类型，只保留核心源代码和资源文件
     *
     * @param projectPath       项目目录的绝对路径（E:\Code\Maiko7\AI\maiko-ai-code-mother/tmp/code_output\multi_file_401090257738149888）
     * @param downloadFileName  下载文件的名称（不含扩展名）(401090257738149888)
     * @param response          HTTP响应对象，用于向客户端返回ZIP文件
     * @throws BusinessException 当项目路径无效或打包过程出错时抛出业务异常
     */
    @Override
    public void downloadProjectAsZip(String projectPath, String downloadFileName, HttpServletResponse response) {
        // 参数校验：确保项目路径不为空
        ThrowUtils.throwIf(StrUtil.isBlank(projectPath), ErrorCode.PARAMS_ERROR, "项目路径不能为空");
        // 参数校验：确保下载文件名不为空
        ThrowUtils.throwIf(StrUtil.isBlank(downloadFileName), ErrorCode.PARAMS_ERROR, "下载文件名不能为空");

        // 创建项目目录对象并验证其存在性
        File projectDir = new File(projectPath);
        ThrowUtils.throwIf(!projectDir.exists(), ErrorCode.NOT_FOUND_ERROR, "项目目录不存在");
        ThrowUtils.throwIf(!projectDir.isDirectory(), ErrorCode.PARAMS_ERROR, "指定路径不是目录");

        // 记录开始打包的日志
        log.info("开始打包下载项目: {} -> {}.zip", projectPath, downloadFileName);

        // 配置HTTP响应头，告知浏览器这是一个需要下载的ZIP文件
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/zip");
        response.addHeader("Content-Disposition",
                String.format("attachment; filename=\"%s.zip\"", downloadFileName));

        // 创建文件过滤器，使用前面定义的isPathAllowed方法进行过滤判断
        FileFilter filter = file -> isPathAllowed(projectDir.toPath(), file.toPath());

        try {
            // 使用Hutool工具类的zip方法，直接将过滤后的目录压缩到响应输出流
            // 参数说明：输出流、字符编码、是否包含目录本身、文件过滤器、源目录
            /**
             * ZipUtil.zip 会递归遍历整个 projectDir 目录：
             * 遍历到一个文件 / 文件夹 → 调用 filter.accept(file) → 调用 isPathAllowed
             * isPathAllowed 返回true → 把这个文件 / 文件夹写入 zip
             * 返回false → 跳过，不打包
             * 遍历完所有文件后，把 zip 流写到 HTTP 响应，浏览器完成下载
             */
            ZipUtil.zip(response.getOutputStream(), StandardCharsets.UTF_8, false, filter, projectDir);
            // 记录打包成功的日志
            log.info("项目打包下载完成: {}", downloadFileName);
        } catch (Exception e) {
            // 捕获异常并记录错误日志，然后抛出业务异常
            log.error("项目打包下载异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "项目打包下载失败");
        }
    }

}