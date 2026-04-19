package com.maiko.maikoaicodemother.core.handler;

import com.maiko.maikoaicodemother.model.entity.User;
import com.maiko.maikoaicodemother.model.enums.CodeGenTypeEnum;
import com.maiko.maikoaicodemother.service.ChatHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 流处理器执行器
 *
 * 职责：
 * 充当“工厂”或“分发器”的角色。
 * 根据业务类型（CodeGenTypeEnum），决定使用哪种具体的流处理策略。
 */
@Slf4j
@Component
public class StreamHandlerExecutor {

    // 注入复杂的 JSON 处理器
    // 注意：这里使用了 @Resource 注入，因为 JsonMessageStreamHandler 也是 @Component
    @Resource
    private JsonMessageStreamHandler jsonMessageStreamHandler;

    /**
     * 执行流处理
     *
     * @param originFlux         原始数据流（来自 AI 的 Flux<String>）
     * @param chatHistoryService 聊天历史服务（用于持久化）
     * @param appId              应用ID
     * @param loginUser          登录用户
     * @param codeGenType        代码生成类型（决定走哪条逻辑分支）
     * @return 处理后的流（最终返回给 Controller 的 Flux<String>）
     */
    public Flux<String> doExecute(Flux<String> originFlux,
                                  ChatHistoryService chatHistoryService,
                                  long appId, User loginUser, CodeGenTypeEnum codeGenType) {

        // 使用 Java Switch 表达式进行策略分发
        return switch (codeGenType) {
            case VUE_PROJECT ->
                // 场景：Vue 项目生成
                // 特点：包含工具调用（写文件）、JSON 格式复杂。
                // 策略：使用已经注入的 jsonMessageStreamHandler 进行处理。
                    jsonMessageStreamHandler.handle(originFlux, chatHistoryService, appId, loginUser);

            case HTML, MULTI_FILE ->
                // 场景：HTML 页面生成 或 多文件纯文本生成
                // 特点：纯文本流，不需要解析 JSON，不需要处理工具调用。
                // 策略：直接 new 一个轻量级的 SimpleTextStreamHandler 进行处理。
                // 注意：SimpleTextStreamHandler 没有加 @Component，因为它无状态且轻量，直接 new 即可。
                    new SimpleTextStreamHandler().handle(originFlux, chatHistoryService, appId, loginUser);
        };
    }
}