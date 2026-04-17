package com.maiko.maikoaicodemother.langgraph4j.node;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.maiko.maikoaicodemother.langgraph4j.model.ImageResource;
import com.maiko.maikoaicodemother.langgraph4j.state.WorkflowContext;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.util.List;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 提示词增强工作节点
 * <p>
 * 该节点负责将“原始用户提示词”与“收集到的图片资源”进行合并，
 * 构造出一个包含完整上下文（需求+素材）的增强版提示词，供后续代码生成使用。
 * </p>
 *
 * @author Maiko7
 */
@Slf4j
public class PromptEnhancerNode {

    /**
     * 创建提示词增强节点的异步动作
     *
     * @return 异步节点动作 {@link AsyncNodeAction}
     */
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            // 1. 获取当前工作流上下文
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 提示词增强");

            // 2. 获取原始提示词和图片列表
            String originalPrompt = context.getOriginalPrompt();
            String imageListStr = context.getImageListStr(); // 备用：字符串形式的图片列表
            List<ImageResource> imageList = context.getImageList(); // 主要：对象形式的图片列表

            // 3. 构建增强后的提示词
            StringBuilder enhancedPromptBuilder = new StringBuilder();
            // 首先追加原始用户需求
            enhancedPromptBuilder.append(originalPrompt);

            // 4. 如果有图片资源，则添加图片信息到提示词中
            if (CollUtil.isNotEmpty(imageList) || StrUtil.isNotBlank(imageListStr)) {
                enhancedPromptBuilder.append("\n\n## 可用素材资源\n");
                enhancedPromptBuilder.append("请在生成网站使用以下图片资源，将这些图片合理地嵌入到网站的相应位置中。\n");

                if (CollUtil.isNotEmpty(imageList)) {
                    // 遍历图片对象列表，格式化输出
                    for (ImageResource image : imageList) {
                        enhancedPromptBuilder.append("- ")
                                .append(image.getCategory().getText()) // 分类（如：插画、Logo）
                                .append("：")
                                .append(image.getDescription())      // 描述
                                .append("（")
                                .append(image.getUrl())              // 链接
                                .append("）\n");
                    }
                } else {
                    // 如果没有对象列表，直接追加备用字符串
                    enhancedPromptBuilder.append(imageListStr);
                }
            }

            String enhancedPrompt = enhancedPromptBuilder.toString();

            // 5. 更新上下文状态
            context.setCurrentStep("提示词增强");
            context.setEnhancedPrompt(enhancedPrompt);

            log.info("提示词增强完成，增强后长度: {} 字符", enhancedPrompt.length());

            // 6. 保存并返回更新后的上下文
            return WorkflowContext.saveContext(context);
        });
    }
}