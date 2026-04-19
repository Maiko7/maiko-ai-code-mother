package com.maiko.maikoaicodemother.service;

import com.maiko.maikoaicodemother.model.entity.ChatSummary;
import com.maiko.maikoaicodemother.model.entity.User;
import com.mybatisflex.core.service.IService;

/**
 * 对话总结服务层
 *
 * @author 代码卡壳Maiko7
 */
public interface ChatSummaryService extends IService<ChatSummary> {

    /**
     * 智能总结指定应用的对话历史
     *
     * @param appId     应用ID
     * @param maxRounds 最多总结的轮数（通常总结最近10轮）
     * @param loginUser 执行总结的用户
     * @return 总结记录ID
     */
    Long summarizeChatHistory(Long appId, int maxRounds, User loginUser);

    /**
     * 获取应用最新的总结记录
     *
     * @param appId 应用ID
     * @return 最新的总结记录，如果没有则返回null
     */
    ChatSummary getLatestSummary(Long appId);

    /**
     * 检查是否需要进行智能总结
     *
     * @param appId 应用ID
     * @return true表示需要总结，false表示不需要
     */
    boolean shouldSummarize(Long appId);
}
