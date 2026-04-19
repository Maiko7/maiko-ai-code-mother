package com.maiko.maikoaicodemother.ai.tools;

import cn.hutool.json.JSONObject;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 退出工具类
 * <p>
 * 当 AI 判断任务已完成，或者不需要继续调用其他工具时，
 * 会调用此工具来终止循环，并输出最终结果。
 */
@Slf4j
@Component
public class ExitTool extends BaseTool {

    /**
     * 获取工具的英文名称
     *
     * @return 工具标识符 "exit"
     */
    @Override
    public String getToolName() {
        return "exit";
    }

    /**
     * 获取工具的中文显示名称
     *
     * @return 显示给用户的名称 "退出工具调用"
     */
    @Override
    public String getDisplayName() {
        return "退出工具调用";
    }

    /**
     * 退出工具执行方法
     * <p>
     * 这是实际被 AI 调用的逻辑。
     * 当任务完成或无需继续使用工具时调用此方法。
     *
     * @return 返回提示信息，告诉系统不要继续调用工具，可以输出最终结果了
     */
    @Tool("当任务已完成或无需继续调用工具时，使用此工具退出操作，防止循环")
    public String exit() {
        log.info("AI 请求退出工具调用");
        return "不要继续调用工具，可以输出最终结果了";
    }

    /**
     * 生成工具执行后的结果格式
     * <p>
     * 用于记录日志或保存到数据库的格式化字符串。
     *
     * @param arguments 工具参数（此处未使用）
     * @return 格式化的执行结束标记
     */
    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        return "\n\n[执行结束]\n\n";
    }
}