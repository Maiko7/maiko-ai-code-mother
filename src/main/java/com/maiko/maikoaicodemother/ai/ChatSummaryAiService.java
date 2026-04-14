package com.maiko.maikoaicodemother.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * AI对话总结服务接口
 * <p>提供对话历史的智能总结功能</p>
 */

public interface ChatSummaryAiService {

    /**
     * 对对话历史进行智能总结
     *
     * @param conversationHistory 对话历史文本
     * @return 总结后的内容（会议纪要格式）
     */
    @SystemMessage(fromResource = "prompt/chat-summary-system-prompt.txt")
    String summarizeConversation(@UserMessage String conversationHistory);
}
