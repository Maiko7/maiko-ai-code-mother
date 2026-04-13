package com.maiko.maikoaicodemother.service;

import com.maiko.maikoaicodemother.model.dto.chathistory.ChatHistoryQueryRequest;
import com.maiko.maikoaicodemother.model.entity.ChatHistory;
import com.maiko.maikoaicodemother.model.entity.User;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层
 * @author: Maiko7
 * @create: 2026-04-12 21:25
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 添加对话历史
     *
     * @param appId       应用 id
     * @param message     消息
     * @param messageType 消息类型
     * @param userId      用户 id
     * @return 是否成功
     */
    boolean addChatMessage(Long appId, String message, String messageType, Long userId);

    /**
     * 根据应用ID删除对话历史
     * @param appId 应用ID
     * @return 是否成功
     */
    boolean deleteByAppId(Long appId);

    /**
     * 分页查询某APP的对话历史
     * @param appId 应用ID
     * @param pageSize 页大小
     * @param lastCreateTime 最后创建时间
     * @param loginUser 登录用户
     * @return 分页结果
     */
    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                               LocalDateTime lastCreateTime,
                                               User loginUser);

    int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount);

    /**
     * 获取查询条件
     * @param chatHistoryQueryRequest 查询条件
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    /**
     * 导出对话历史为Markdown格式
     *
     * @param appId     应用ID
     * @param startTime 开始时间（可选）
     * @param endTime   结束时间（可选）
     * @param loginUser 登录用户
     * @return Markdown格式的对话历史
     */
    String exportChatHistoryToMarkdown(Long appId, LocalDateTime startTime, LocalDateTime endTime, User loginUser);
}
