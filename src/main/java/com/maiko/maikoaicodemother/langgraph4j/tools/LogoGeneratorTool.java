package com.maiko.maikoaicodemother.langgraph4j.tools;

import cn.hutool.core.util.StrUtil;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;

import com.maiko.maikoaicodemother.langgraph4j.model.ImageResource;
import com.maiko.maikoaicodemother.langgraph4j.model.enums.ImageCategoryEnum;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Logo 图片生成工具
 * <p>
 * 该工具封装了阿里云通义万相（DashScope）的文生图能力，专门用于生成网站 Logo。
 * 针对 Logo 生成的特性，对提示词进行了特定的优化和约束。
 * </p>
 *
 * @author Maiko7
 */
@Slf4j
@Component
public class LogoGeneratorTool {

    /**
     * 阿里云 DashScope API Key，从配置文件注入
     */
    @Value("${dashscope.api-key:}")
    private String dashScopeApiKey;

    /**
     * 使用的图像生成模型，默认为 wan2.2-t2i-flash（速度快，效果好）
     */
    @Value("${dashscope.image-model:wan2.2-t2i-flash}")
    private String imageModel;

    /**
     * 生成 Logo 图片
     *
     * @param description Logo 的设计描述，包含名称、行业、风格偏好等
     * @return 包含生成图片信息的列表
     */
    @Tool("根据描述生成 Logo 设计图片，用于网站品牌标识")
    public List<ImageResource> generateLogos(
            @P("Logo 设计描述，如名称、行业、风格等，尽量详细") String description) {

        List<ImageResource> logoList = new ArrayList<>();
        try {
            // 1. 构建提示词 (Prompt Engineering)
            // 关键点：明确禁止 AI 在图片中直接生成文字。
            // 原因：目前的文生图模型在处理具体文字拼写时容易出错，且 Logo 通常需要后期矢量处理，纯图形更通用。
            String logoPrompt = String.format("生成 Logo，Logo 中禁止包含任何文字！Logo 介绍：%s", description);

            // 2. 构建 API 请求参数
            ImageSynthesisParam param = ImageSynthesisParam.builder()
                    .apiKey(dashScopeApiKey)
                    .model(imageModel)
                    .prompt(logoPrompt)
                    .size("512*512") // Logo 通常使用正方形尺寸
                    .n(1)            // 设置生成数量为 1
                    // 理由：Logo 设计主观性强，AI 很难一次猜中用户心意，
                    // 生成多张反而增加筛选成本，不如让用户根据单张结果反馈调整描述。
                    .build();

            // 3. 执行调用
            ImageSynthesis imageSynthesis = new ImageSynthesis();
            ImageSynthesisResult result = imageSynthesis.call(param);

            // 4. 解析结果
            if (result != null && result.getOutput() != null && result.getOutput().getResults() != null) {
                List<Map<String, String>> results = result.getOutput().getResults();
                for (Map<String, String> imageResult : results) {
                    String imageUrl = imageResult.get("url");
                    if (StrUtil.isNotBlank(imageUrl)) {
                        logoList.add(ImageResource.builder()
                                .category(ImageCategoryEnum.LOGO) // 标记分类为 Logo
                                .description(description)
                                .url(imageUrl)
                                .build());
                    }
                }
            }
        } catch (Exception e) {
            log.error("生成 Logo 失败: {}", e.getMessage(), e);
        }
        return logoList;
    }
}