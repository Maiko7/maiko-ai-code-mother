package com.maiko.maikoaicodemother.core.handler;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.maiko.maikoaicodemother.ai.model.message.*;
import com.maiko.maikoaicodemother.ai.tools.BaseTool;
import com.maiko.maikoaicodemother.ai.tools.ToolManager;
import com.maiko.maikoaicodemother.core.builder.VueProjectBuilder;
import com.maiko.maikoaicodemother.model.entity.User;
import com.maiko.maikoaicodemother.model.enums.ChatHistoryMessageTypeEnum;
import com.maiko.maikoaicodemother.service.ChatHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.HashSet;
import java.util.Set;

/**
 * JSON 消息流处理器
 *
 * 职责：
 * 1. 接收 AI 发来的原始 JSON 流（Flux<String>）。
 * 2. 解析 JSON，区分是“普通文本”还是“工具调用”。
 * 3. 【持久化】在后台默默拼凑完整内容，用于存入数据库（作为 AI 的记忆）。
 * 4. 【展示】将内容转换为前端需要的格式（如 Markdown）并返回给前端展示。
 */
@Slf4j
@Component
public class JsonMessageStreamHandler {

    @Resource
    private VueProjectBuilder vueProjectBuilder;

    @Resource
    private ToolManager toolManager;
    /**
     * 处理主入口
     *
     * @param originFlux         原始数据流（来自 AI 的 SSE 或 WebSocket 消息）
     * @param chatHistoryService 数据库服务（用于保存历史记录）
     * @param appId              当前对话的应用ID
     * @param loginUser          当前用户（用于记录是谁的对话）
     * @return 处理后的字符串流（直接写给前端）
     */
    public Flux<String> handle(Flux<String> originFlux,
                               ChatHistoryService chatHistoryService,
                               long appId, User loginUser) {

        // 【内存暂存区】用于在流式传输过程中，一点点拼凑完整的 AI 回复
        // 目的：因为流是分片的，我们需要最后把完整的句子存进数据库
        StringBuilder chatHistoryStringBuilder = new StringBuilder();

        // 【去重工具】用于记录已经处理过的工具调用 ID
        // 原因：流式传输中，同一个工具请求可能会被拆成多个包发过来，我们只需要处理第一次出现的包
        Set<String> seenToolIds = new HashSet<>();

        return originFlux
                // --- 1. 转换阶段 (Map) ---
                .map(chunk -> {
                    // 解析每一个 JSON 片段，并根据类型决定返回什么内容给前端
                    return handleJsonMessageChunk(chunk, chatHistoryStringBuilder, seenToolIds);
                })
                // --- 2. 过滤阶段 (Filter) ---
                .filter(StrUtil::isNotEmpty) // 剔除处理过程中产生的空字符串（比如工具调用的中间片段），避免前端闪烁
                // --- 3. 完成阶段 (Complete) ---
                .doOnComplete(() -> {
                    // 当流结束（AI 说完了），将内存中拼凑好的完整内容存入数据库
                    String aiResponse = chatHistoryStringBuilder.toString();
                    chatHistoryService.addChatMessage(appId, aiResponse, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
//                    String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + "/vue_project_" + appId;
//                    vueProjectBuilder.buildProjectAsync(projectPath);
                })
                // --- 4. 错误阶段 (Error) ---
                .doOnError(error -> {
                    // 如果传输中途报错，也要记录一条错误消息，防止对话状态丢失
                    String errorMessage = "AI回复失败: " + error.getMessage();
                    chatHistoryService.addChatMessage(appId, errorMessage, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
                });
    }

    /**
     * 解析单个 JSON 消息块的具体逻辑
     */
    private String handleJsonMessageChunk(String chunk, StringBuilder chatHistoryStringBuilder, Set<String> seenToolIds) {
        // 1. 先解析出消息头，判断类型
        StreamMessage streamMessage = JSONUtil.toBean(chunk, StreamMessage.class);
        StreamMessageTypeEnum typeEnum = StreamMessageTypeEnum.getEnumByValue(streamMessage.getType());

        switch (typeEnum) {
            case AI_RESPONSE -> {
                // --- 场景 A：AI 正在输出普通文本 ---
                AiResponseMessage aiMessage = JSONUtil.toBean(chunk, AiResponseMessage.class);
                String data = aiMessage.getData();

                // 动作 1：存入内存（为了后续持久化）
                chatHistoryStringBuilder.append(data);

                // 动作 2：直接返回给前端展示
                return data;
            }
            case TOOL_REQUEST -> {
                // --- 场景 B：AI 请求调用工具（比如决定要写文件了） ---
                ToolRequestMessage toolRequestMessage = JSONUtil.toBean(chunk, ToolRequestMessage.class);
                String toolId = toolRequestMessage.getId();
                String toolName = toolRequestMessage.getName();
                // 逻辑：检查这个工具调用是否已经处理过（去重）
                if (toolId != null && !seenToolIds.contains(toolId)) {
                    seenToolIds.add(toolId); // 标记为已处理
                    BaseTool tool = toolManager.getTool(toolName);
                    return tool.generateToolRequestResponse();

//                    // 返回给前端：给用户一个提示，告诉用户“我正在选择工具...”
//                    // 注意：这里不存入 chatHistoryStringBuilder，因为我们只想要最终的代码结果，不需要中间的过程提示
//                    return "\n\n[选择工具] 写入文件\n\n";
                } else {
                    // 如果是重复的片段，直接吞掉，不发给前端
                    return "";
                }
            }
            case TOOL_EXECUTED -> {
                // --- 场景 C：工具执行完毕，返回结果（比如文件写好了，返回代码内容） ---
                ToolExecutedMessage toolExecutedMessage = JSONUtil.toBean(chunk, ToolExecutedMessage.class);

                // 从参数中提取文件路径和内容
                JSONObject jsonObject = JSONUtil.parseObj(toolExecutedMessage.getArguments());
                // 根据工具名称获取工具实例
                String toolName = toolExecutedMessage.getName();
                BaseTool tool = toolManager.getTool(toolName);
                String result = tool.generateToolExecutedResult(jsonObject);

//                String relativeFilePath = jsonObject.getStr("relativeFilePath");
//                String suffix = FileUtil.getSuffix(relativeFilePath); // 获取后缀名用于高亮
//                String content = jsonObject.getStr("content");
//
//                // 组装 Markdown 格式的代码块
//                String result = String.format("""
//                        [工具调用] 写入文件 %s
//                        ```%s
//                        %s
//                        ```
//                        """, relativeFilePath, suffix, content);

                // 格式化输出（加换行）
                String output = String.format("\n\n%s\n\n", result);

                // 动作 1：存入内存（为了后续持久化）
                chatHistoryStringBuilder.append(output);

                // 动作 2：返回给前端展示（用户能看到生成的代码）
                return output;
            }
            default -> {
                log.error("不支持的消息类型: {}", typeEnum);
                return "";
            }
        }
    }
}