package com.maiko.maikoaicodemother.core.handler;

import com.maiko.maikoaicodemother.model.entity.User;
import com.maiko.maikoaicodemother.model.enums.ChatHistoryMessageTypeEnum;
import com.maiko.maikoaicodemother.service.ChatHistoryService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * 简单文本流处理器
 *
 * 适用场景：
 * 1. HTML 类型：直接返回 HTML 代码片段。
 * 2. MULTI_FILE 类型：返回纯文本或多文件内容的简单拼接。
 *
 * 特点：
 * 不做任何 JSON 解析，不做工具调用识别，只负责“搬运”和“记录”。
 */
@Slf4j
public class SimpleTextStreamHandler {

    /**
     * 处理传统流
     *
     * @param originFlux         原始流（AI 返回的原始字符串片段）
     * @param chatHistoryService 聊天历史服务（用于最后存库）
     * @param appId              应用ID
     * @param loginUser          登录用户
     * @return 处理后的流（直接透传给前端）
     */
    public Flux<String> handle(Flux<String> originFlux,
                               ChatHistoryService chatHistoryService,
                               long appId, User loginUser) {
        // 【核心容器】用于在内存中拼凑完整的 AI 回复
        // 注意：因为是在 handle 方法内定义的，所以它是线程安全的（每个请求都有自己的实例）
        StringBuilder aiResponseBuilder = new StringBuilder();

        return originFlux
                .map(chunk -> {
                    // 1. 【记录】将每一个到来的片段（chunk）追加到容器中
                    // AI 的回复会被拆成很多小块，这里把它们像拼图一样拼起来
                    aiResponseBuilder.append(chunk);

                    // 2. 【转发】直接返回原始片段
                    // 这里不做任何修改，前端收到什么就是什么（比如打字机效果）
                    return chunk;
                })
                .doOnComplete(() -> {
                    // 3. 【持久化】当流传输结束（AI 说完了）
                    // 此时 aiResponseBuilder 里已经有了完整的文本
                    String aiResponse = aiResponseBuilder.toString();
                    chatHistoryService.addChatMessage(appId, aiResponse, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
                    log.info("AI 简单文本回复完成，已存入历史，长度: {}", aiResponse.length());
                })
                .doOnError(error -> {
                    // 4. 【异常处理】如果流传输中途断开或报错
                    // 记录一条错误消息到历史，防止前端一直在“转圈”等待
                    String errorMessage = "AI回复失败: " + error.getMessage();
                    chatHistoryService.addChatMessage(appId, errorMessage, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
                    log.error("AI 简单文本回复失败", error);
                });
    }
}