package com.maiko.maikoaicodemother.langgraph4j.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.system.SystemUtil;

import com.maiko.maikoaicodemother.exception.BusinessException;
import com.maiko.maikoaicodemother.exception.ErrorCode;
import com.maiko.maikoaicodemother.langgraph4j.model.ImageResource;
import com.maiko.maikoaicodemother.langgraph4j.model.enums.ImageCategoryEnum;
import com.maiko.maikoaicodemother.manager.CosManager;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Mermaid 架构图生成工具
 * <p>
 * 该工具用于将 Mermaid 文本代码转换为可视化的 SVG 架构图。
 * 核心原理是调用本地安装的 Mermaid CLI (mmdc) 命令行工具进行渲染，
 * 然后将生成的图片上传至对象存储（COS）以获取公网访问链接。
 * </p>
 *
 * @author Maiko7
 */
@Slf4j
@Component
public class MermaidDiagramTool {

    @Resource
    private CosManager cosManager;

    /**
     * 将 Mermaid 代码转换为架构图图片
     * 你这里为什么架构图生成一张你还返回一个List知道为什么吗？
     * 1. 返回的是空列表AI就知道没有生成图片。方便统一异常
     * 2. 所有的工具都可以放一起，每个工具类返回的都是图资源列表，那最后把这个图片统一收集合并的时候
     * 也能够有更一致的调用
     *
     * @param mermaidCode Mermaid 图表代码字符串
     * @param description 架构图的文字描述
     * @return 包含图片信息的列表（通常只有一张图）
     */
    @Tool("将 Mermaid 代码转换为架构图图片，用于展示系统结构和技术关系")
    public List<ImageResource> generateMermaidDiagram(@P("Mermaid 图表代码") String mermaidCode,
                                                      @P("架构图描述") String description) {
        if (StrUtil.isBlank(mermaidCode)) {
            return new ArrayList<>();
        }
        try {
            // 1. 调用本地 CLI 工具将代码转换为 SVG 文件
            File diagramFile = convertMermaidToSvg(mermaidCode);

            // 2. 生成唯一的存储路径并上传到 COS
            String keyName = String.format("/mermaid/%s/%s",
                    RandomUtil.randomString(5), diagramFile.getName());
            String cosUrl = cosManager.uploadFile(keyName, diagramFile);

            // 3. 上传后清理本地临时文件，防止磁盘占用
            FileUtil.del(diagramFile);

            // 4. 封装返回结果
            if (StrUtil.isNotBlank(cosUrl)) {
                return Collections.singletonList(ImageResource.builder()
                        .category(ImageCategoryEnum.ARCHITECTURE)
                        .description(description)
                        .url(cosUrl)
                        .build());
            }
        } catch (Exception e) {
            log.error("生成架构图失败: {}", e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    /**
     * 核心转换逻辑：调用 Mermaid CLI 将文本转换为 SVG
     *
     * @param mermaidCode Mermaid 源码
     * @return 生成的 SVG 文件对象
     */
    private File convertMermaidToSvg(String mermaidCode) {
        // 创建临时输入文件 (.mmd)
        File tempInputFile = FileUtil.createTempFile("mermaid_input_", ".mmd", true);
        FileUtil.writeUtf8String(mermaidCode, tempInputFile);

        // 创建临时输出文件 (.svg)
        File tempOutputFile = FileUtil.createTempFile("mermaid_output_", ".svg", true);

        // 根据操作系统环境自动选择命令 (Windows 下通常是 mmdc.cmd，Linux/Mac 下是 mmdc)
        String command = SystemUtil.getOsInfo().isWindows() ? "mmdc.cmd" : "mmdc";

        // 构建执行命令：
        // -i: 输入文件
        // -o: 输出文件
        // -b: 背景颜色 (transparent 表示透明背景)
        String cmdLine = String.format("%s -i %s -o %s -b transparent",
                command,
                tempInputFile.getAbsolutePath(),
                tempOutputFile.getAbsolutePath()
        );

        // 执行命令行指令
        RuntimeUtil.execForStr(cmdLine);

        // 校验输出文件是否生成成功
        if (!tempOutputFile.exists() || tempOutputFile.length() == 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Mermaid CLI 执行失败，请检查环境配置");
        }

        // 清理输入文件，保留输出文件供后续上传使用
        FileUtil.del(tempInputFile);
        return tempOutputFile;
    }
}