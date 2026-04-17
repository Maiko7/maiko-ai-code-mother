package com.maiko.maikoaicodemother.langgraph4j.node.concurrent;

import com.maiko.maikoaicodemother.langgraph4j.ai.ImageCollectionPlanService;
import com.maiko.maikoaicodemother.langgraph4j.model.ImageCollectionPlan;
import com.maiko.maikoaicodemother.langgraph4j.state.WorkflowContext;
import com.maiko.maikoaicodemother.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 图片规划节点
 * <p>
 * 该节点是图片处理工作流的“大脑”和入口点。
 * 它负责分析用户的原始需求，利用 AI 制定详细的“图片收集计划”，
 * 并将计划拆解为具体的任务（如架构图生成、插画搜索等），供后续的并行分支节点使用。
 * </p>
 *
 * @author Maiko7
 */
@Slf4j
public class ImagePlanNode {

    /**
     * 创建异步节点动作
     * <p>
     * 该方法从上下文中获取用户原始提示词，调用规划服务生成任务列表，
     * 并将其存储回上下文，作为后续并发执行的依据。
     * </p>
     *
     * @return 异步节点动作实例
     */
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            // 1. 从当前状态中提取工作流上下文
            WorkflowContext context = WorkflowContext.getContext(state);

            // 2. 获取用户的原始需求/提示词
            String originalPrompt = context.getOriginalPrompt();

            try {
                // 3. 手动获取 Spring Bean
                // 获取图片收集计划服务，该服务内部通常包含 LLM 调用逻辑
                ImageCollectionPlanService planService = SpringContextUtil.getBean(ImageCollectionPlanService.class);

                // 4. 执行智能规划
                // 根据用户需求，分析需要哪些类型的图片，并生成具体的任务清单
                ImageCollectionPlan plan = planService.planImageCollection(originalPrompt);

                log.info("生成图片收集计划，准备启动并发分支");

                // 5. 更新上下文状态
                // 将生成的计划存入 Context，这是连接“规划”与“执行”的关键桥梁
                context.setImageCollectionPlan(plan);
                // 更新当前步骤描述，用于前端进度展示
                context.setCurrentStep("图片计划");

            } catch (Exception e) {
                log.error("图片计划生成失败: {}", e.getMessage(), e);
                // 生产环境中可在此处设置错误标志，防止后续空指针异常
            }

            // 6. 保存并返回新的状态
            return WorkflowContext.saveContext(context);
        });
    }
}