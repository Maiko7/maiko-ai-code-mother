package com.maiko.maikoaicodemother.langgraph4j.node;

import com.maiko.maikoaicodemother.core.builder.VueProjectBuilder;
import com.maiko.maikoaicodemother.exception.BusinessException;
import com.maiko.maikoaicodemother.exception.ErrorCode;
import com.maiko.maikoaicodemother.langgraph4j.state.WorkflowContext;
import com.maiko.maikoaicodemother.model.enums.CodeGenTypeEnum;
import com.maiko.maikoaicodemother.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.io.File;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 项目构建节点
 * <p>
 * 该节点负责调用构建工具（如 npm/maven）对生成的源代码进行编译和打包，
 * 生成最终可部署的生产环境文件（如 dist 目录）。
 * </p>
 *
 * @author Maiko7
 */
@Slf4j
public class ProjectBuilderNode {

    /**
     * 创建项目构建节点的异步动作
     *
     * @return 异步节点动作 {@link AsyncNodeAction}
     */
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            // 1. 获取当前工作流上下文
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 项目构建");

            // 2. 获取必要的参数
            String generatedCodeDir = context.getGeneratedCodeDir();
            CodeGenTypeEnum generationType = context.getGenerationType();
            String buildResultDir;

            // 3. 针对 Vue 项目类型进行构建
            try {
                // 从 Spring 容器中获取 Vue 项目构建器
                VueProjectBuilder vueBuilder = SpringContextUtil.getBean(VueProjectBuilder.class);

                // 执行 Vue 项目构建（通常包括 npm install 安装依赖 + npm run build 打包）
                boolean buildSuccess = vueBuilder.buildProject(generatedCodeDir);

                if (buildSuccess) {
                    // 构建成功，指向 dist 目录（生产文件存放处）
                    buildResultDir = generatedCodeDir + File.separator + "dist";
                    log.info("Vue 项目构建成功，dist 目录: {}", buildResultDir);
                } else {
                    // 构建返回失败，抛出业务异常
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Vue 项目构建失败");
                }
            } catch (Exception e) {
                log.error("Vue 项目构建异常: {}", e.getMessage(), e);
                // 异常捕获：为了防止流程中断，这里选择降级处理，返回原始代码目录
                buildResultDir = generatedCodeDir;
            }

            // 4. 更新上下文状态
            context.setCurrentStep("项目构建");
            context.setBuildResultDir(buildResultDir);

            log.info("项目构建节点完成，最终目录: {}", buildResultDir);

            // 5. 保存并返回更新后的上下文
            return WorkflowContext.saveContext(context);
        });
    }
}