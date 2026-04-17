package com.maiko.maikoaicodemother.langgraph4j.node;

import com.maiko.maikoaicodemother.langgraph4j.ai.ImageCollectionPlanService;
import com.maiko.maikoaicodemother.langgraph4j.model.ImageCollectionPlan;
import com.maiko.maikoaicodemother.langgraph4j.model.ImageResource;
import com.maiko.maikoaicodemother.langgraph4j.state.WorkflowContext;
import com.maiko.maikoaicodemother.langgraph4j.tools.ImageSearchTool;
import com.maiko.maikoaicodemother.langgraph4j.tools.LogoGeneratorTool;
import com.maiko.maikoaicodemother.langgraph4j.tools.MermaidDiagramTool;
import com.maiko.maikoaicodemother.langgraph4j.tools.UndrawIllustrationTool;
import com.maiko.maikoaicodemother.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 图片收集节点（并发执行版）
 * <p>
 * 该节点负责根据用户需求，规划并并发收集所需的各类图片资源（如内容图、插画、架构图、Logo等）。
 * </p>
 *
 * @author Maiko7
 */
@Slf4j
public class ImageCollectorNode {

    /**
     * 创建图片收集节点的异步动作
     *
     * @return 异步节点动作 {@link AsyncNodeAction}
     */
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            // 1. 获取当前工作流上下文
            WorkflowContext context = WorkflowContext.getContext(state);
            String originalPrompt = context.getOriginalPrompt();
            List<ImageResource> collectedImages = new ArrayList<>();

            try {
                // 2. 获取图片收集计划服务，分析用户提示词，生成图片收集计划
                /**
                 * 你这里你是静态方法，而ImageCollectionPlanService是一个Bean。你怎么才能获取到呢？
                 */
                ImageCollectionPlanService planService = SpringContextUtil.getBean(ImageCollectionPlanService.class);
                ImageCollectionPlan plan = planService.planImageCollection(originalPrompt);
                log.info("获取到图片收集计划，开始并发执行");

                // 3. 准备并发任务容器
                List<CompletableFuture<List<ImageResource>>> futures = new ArrayList<>();

                // --- 3.1 并发执行内容图片搜索 ---
                if (plan.getContentImageTasks() != null) {
                    ImageSearchTool imageSearchTool = SpringContextUtil.getBean(ImageSearchTool.class);
                    for (ImageCollectionPlan.ImageSearchTask task : plan.getContentImageTasks()) {
                        // 为每个搜索任务创建一个异步线程
                        futures.add(CompletableFuture.supplyAsync(() ->
                                imageSearchTool.searchContentImages(task.query())));
                    }
                }

                // --- 3.2 并发执行插画图片搜索 ---
                if (plan.getIllustrationTasks() != null) {
                    UndrawIllustrationTool illustrationTool = SpringContextUtil.getBean(UndrawIllustrationTool.class);
                    for (ImageCollectionPlan.IllustrationTask task : plan.getIllustrationTasks()) {
                        futures.add(CompletableFuture.supplyAsync(() ->
                                illustrationTool.searchIllustrations(task.query())));
                    }
                }

                // --- 3.3 并发执行架构图生成 ---
                if (plan.getDiagramTasks() != null) {
                    MermaidDiagramTool diagramTool = SpringContextUtil.getBean(MermaidDiagramTool.class);
                    for (ImageCollectionPlan.DiagramTask task : plan.getDiagramTasks()) {
                        futures.add(CompletableFuture.supplyAsync(() ->
                                diagramTool.generateMermaidDiagram(task.mermaidCode(), task.description())));
                    }
                }

                // --- 3.4 并发执行 Logo 生成 ---
                if (plan.getLogoTasks() != null) {
                    LogoGeneratorTool logoTool = SpringContextUtil.getBean(LogoGeneratorTool.class);
                    for (ImageCollectionPlan.LogoTask task : plan.getLogoTasks()) {
                        futures.add(CompletableFuture.supplyAsync(() ->
                                logoTool.generateLogos(task.description())));
                    }
                }

                // 4. 等待所有并发任务完成
                CompletableFuture<Void> allTasks = CompletableFuture.allOf(
                        futures.toArray(new CompletableFuture[0]));
                allTasks.join(); // 阻塞当前线程直到所有任务结束

                // 5. 收集所有任务的结果
                for (CompletableFuture<List<ImageResource>> future : futures) {
                    List<ImageResource> images = future.get();
                    if (images != null) {
                        collectedImages.addAll(images);
                    }
                }
                log.info("并发图片收集完成，共收集到 {} 张图片", collectedImages.size());

            } catch (Exception e) {
                log.error("图片收集失败: {}", e.getMessage(), e);
            }

            // 6. 更新上下文状态
            context.setCurrentStep("图片收集");
            context.setImageList(collectedImages);

            // 7. 保存并返回更新后的上下文
            return WorkflowContext.saveContext(context);
        });
    }
}