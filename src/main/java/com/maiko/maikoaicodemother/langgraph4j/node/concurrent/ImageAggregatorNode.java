package com.maiko.maikoaicodemother.langgraph4j.node.concurrent;

import com.maiko.maikoaicodemother.langgraph4j.model.ImageResource;
import com.maiko.maikoaicodemother.langgraph4j.state.WorkflowContext;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.util.ArrayList;
import java.util.List;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 图片汇总节点
 * <p>
 * 该节点充当并行处理流程的“汇聚点”。
 * 在所有独立的图片收集任务（内容图、插画、架构图、Logo）完成后，
 * 负责将分散在上下文不同字段中的图片资源整合到一个统一的列表中，
 * 供后续的最终代码生成步骤使用。
 * </p>
 *
 * @author Maiko7
 */
@Slf4j
public class ImageAggregatorNode {

    /**
     * 创建异步节点动作
     * <p>
     * 该方法执行数据的归并操作，不涉及外部 API 调用，主要用于状态同步。
     * </p>
     *
     * @return 异步节点动作实例
     */
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            // 1. 从当前状态中提取工作流上下文
            WorkflowContext context = WorkflowContext.getContext(state);
            List<ImageResource> allImages = new ArrayList<>();

            log.info("开始聚合并发收集的图片");

            // 2. 多源数据合并
            // 按照图片类别，依次将各个中间字段的数据添加到总列表中
            // 这种设计允许不同的收集器独立运行，互不干扰，最后统一汇总

            // 添加普通内容图片
            if (context.getContentImages() != null) {
                allImages.addAll(context.getContentImages());
            }
            // 添加 UnDraw 风格插画
            if (context.getIllustrations() != null) {
                allImages.addAll(context.getIllustrations());
            }
            // 添加 Mermaid 架构图
            if (context.getDiagrams() != null) {
                allImages.addAll(context.getDiagrams());
            }
            // 添加 Logo 标识
            if (context.getLogos() != null) {
                allImages.addAll(context.getLogos());
            }

            log.info("图片聚合完成，总共 {} 张图片", allImages.size());

            // 3. 更新最终状态
            // 将合并后的列表设置到 Context 的主图片列表中
            context.setImageList(allImages);
            // 更新当前步骤描述，用于前端进度展示
            context.setCurrentStep("图片聚合");

            // 4. 保存并返回新的状态
            return WorkflowContext.saveContext(context);
        });
    }
}