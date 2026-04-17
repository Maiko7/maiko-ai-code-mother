package com.maiko.maikoaicodemother.langgraph4j.node.concurrent;

import com.maiko.maikoaicodemother.langgraph4j.model.ImageCollectionPlan;
import com.maiko.maikoaicodemother.langgraph4j.model.ImageResource;
import com.maiko.maikoaicodemother.langgraph4j.state.WorkflowContext;
import com.maiko.maikoaicodemother.langgraph4j.tools.MermaidDiagramTool;
import com.maiko.maikoaicodemother.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.util.ArrayList;
import java.util.List;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 架构图收集节点
 * <p>
 * 该节点负责根据预定义的“图片收集计划”，批量生成系统架构相关的 Mermaid 图表。
 * 它通过调用 MermaidDiagramTool 工具将文本代码转换为可视化的 SVG 图片。
 * </p>
 *
 * @author Maiko7
 */
@Slf4j
public class DiagramCollectorNode {

    /**
     * 创建异步节点动作
     * <p>
     * 该方法返回一个符合 LangGraph4j 规范的异步节点处理器。
     * 使用 node_async 包装器是为了支持非阻塞的流式处理。
     * </p>
     *
     * @return 异步节点动作实例
     */
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            // 1. 从当前状态中提取工作流上下文
            WorkflowContext context = WorkflowContext.getContext(state);
            List<ImageResource> diagrams = new ArrayList<>();

            try {
                // 2. 获取图片收集计划
                // ImageCollectionPlan 包含了所有需要生成的图表任务列表（如：系统架构图、数据流图等）
                ImageCollectionPlan plan = context.getImageCollectionPlan();

                if (plan != null && plan.getDiagramTasks() != null) {
                    // 3. 手动获取 Spring Bean
                    // 由于节点类通常由静态工厂方法创建，不直接受 Spring 管理，
                    // 因此需要通过工具类手动从容器中获取工具 Bean。
                    MermaidDiagramTool diagramTool = SpringContextUtil.getBean(MermaidDiagramTool.class);

                    log.info("开始并发生成架构图，任务数: {}", plan.getDiagramTasks().size());

                    // 4. 遍历任务并执行生成
                    // 注意：这里目前是串行执行，如果需要真正的并发，可以使用 CompletableFuture 或 Parallel Stream
                    for (ImageCollectionPlan.DiagramTask task : plan.getDiagramTasks()) {
                        // 调用 Mermaid 工具生成图片
                        // 传入 mermaidCode (源码) 和 description (描述)
                        List<ImageResource> images = diagramTool.generateMermaidDiagram(
                                task.mermaidCode(), task.description());
                        if (images != null) {
                            diagrams.addAll(images);
                        }
                    }
                    log.info("架构图生成完成，共生成 {} 张图片", diagrams.size());
                }
            } catch (Exception e) {
                log.error("架构图生成失败: {}", e.getMessage(), e);
                // 生产环境中建议将错误信息也记录到 Context 中以便前端展示
            }

            // 5. 更新上下文状态
            // 将生成的架构图列表存入上下文，供后续节点（如代码生成器）使用
            context.setDiagrams(diagrams);
            // 更新当前步骤描述，用于前端进度展示
            context.setCurrentStep("架构图生成");

            // 6. 保存并返回新的状态
            // 这一步至关重要，它将修改后的 Context 重新序列化回 State Map 中
            return WorkflowContext.saveContext(context);
        });
    }
}