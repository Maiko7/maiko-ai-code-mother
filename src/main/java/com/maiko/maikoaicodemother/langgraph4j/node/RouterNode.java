package com.maiko.maikoaicodemother.langgraph4j.node;

import com.maiko.maikoaicodemother.ai.AiCodeGenTypeRoutingService;
import com.maiko.maikoaicodemother.langgraph4j.state.WorkflowContext;
import com.maiko.maikoaicodemother.model.enums.CodeGenTypeEnum;
import com.maiko.maikoaicodemother.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 智能路由工作节点
 * <p>
 * 该节点充当“交通指挥官”，负责根据用户的原始需求提示词，
 * 分析并决定应该采用哪种技术栈（如 Vue、React 或纯 HTML）来生成代码。
 * </p>
 *
 * @author Maiko7
 */
@Slf4j
public class RouterNode {

    /**
     * 创建智能路由节点的异步动作
     *
     * @return 异步节点动作 {@link AsyncNodeAction}
     */
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            // 1. 获取当前工作流上下文
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 智能路由");

            CodeGenTypeEnum generationType;

            try {
                // 2. 获取 AI 路由服务 Bean
                AiCodeGenTypeRoutingService routingService = SpringContextUtil.getBean(AiCodeGenTypeRoutingService.class);

                // 3. 根据原始提示词进行智能路由判断
                // AI 会分析用户需求复杂度，决定是用简单的 HTML 还是复杂的 Vue/React 框架
                generationType = routingService.routeCodeGenType(context.getOriginalPrompt());

                log.info("AI智能路由完成，选择类型: {} ({})", generationType.getValue(), generationType.getText());
            } catch (Exception e) {
                // 4. 异常兜底：如果 AI 判断失败，默认使用最通用的 HTML 类型，防止流程中断
                log.error("AI智能路由失败，使用默认HTML类型: {}", e.getMessage());
                generationType = CodeGenTypeEnum.HTML;
            }

            // 5. 更新上下文状态
            context.setCurrentStep("智能路由");
            // 将选定的代码生成类型存入上下文，后续节点（如代码生成、项目构建）将依据此类型执行
            context.setGenerationType(generationType);

            // 6. 保存并返回更新后的上下文
            return WorkflowContext.saveContext(context);
        });
    }
}