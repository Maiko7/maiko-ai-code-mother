package com.maiko.maikoaicodemother.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.maiko.maikoaicodemother.ai.ChatSummaryAiService;
import com.maiko.maikoaicodemother.exception.BusinessException;
import com.maiko.maikoaicodemother.exception.ErrorCode;
import com.maiko.maikoaicodemother.mapper.ChatSummaryMapper;
import com.maiko.maikoaicodemother.model.entity.ChatHistory;
import com.maiko.maikoaicodemother.model.entity.ChatSummary;
import com.maiko.maikoaicodemother.model.entity.User;
import com.maiko.maikoaicodemother.model.enums.ChatHistoryMessageTypeEnum;
import com.maiko.maikoaicodemother.service.AppService;
import com.maiko.maikoaicodemother.service.ChatHistoryService;
import com.maiko.maikoaicodemother.service.ChatSummaryService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 对话总结服务层实现
 *
 * @author 代码卡壳Maiko7
 */
@Service
@Slf4j
public class ChatSummaryServiceImpl extends ServiceImpl<ChatSummaryMapper, ChatSummary> implements ChatSummaryService {

    @Resource
    @Lazy
    private ChatHistoryService chatHistoryService;

    @Resource
    @Lazy
    private AppService appService;

    @Resource
    @Lazy
    private ChatSummaryAiService chatSummaryAiService;

    /**
     * 触发总结的轮数阈值
     */
    private static final int SUMMARY_TRIGGER_ROUNDS = 10;

    /**
     * 总结后保留的最近完整对话轮数
     */
    private static final int KEEP_RECENT_ROUNDS = 5;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long summarizeChatHistory(Long appId, int maxRounds, User loginUser) {
        // 1. 参数校验
        if (appId == null || appId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        }
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 2. 查询需要总结的对话历史（最近的maxRounds轮，即2*maxRounds条消息）
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("appId", appId)
                .eq("isSummarized", 0)  // 只总结未被总结的消息
                .orderBy("createTime", true)  // 按时间正序
                .limit(maxRounds * 2);  // 每轮包含用户消息和AI回复

        List<ChatHistory> historyList = chatHistoryService.list(queryWrapper);

        if (CollUtil.isEmpty(historyList)) {
            log.info("应用 {} 没有需要总结的对话历史", appId);
            return null;
        }

        // 3. 格式化对话历史为文本
        String conversationText = formatConversationForSummary(historyList);

        if (StrUtil.isBlank(conversationText)) {
            log.warn("应用 {} 的对话历史格式化为空", appId);
            return null;
        }

        try {
            // 4. 调用AI进行总结
            log.info("开始对应用 {} 的 {} 条对话进行AI总结", appId, historyList.size());
            String summaryContent = chatSummaryAiService.summarizeConversation(conversationText);

            if (StrUtil.isBlank(summaryContent)) {
                log.error("应用 {} 的AI总结结果为空", appId);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI总结失败");
            }

            // 5. 保存总结记录
            ChatSummary summary = ChatSummary.builder()
                    .appId(appId)
                    .summaryContent(summaryContent)
                    .summarizedRounds(historyList.size() / 2)  // 轮数 = 消息数/2
                    .startRoundId(historyList.get(0).getId())
                    .endRoundId(historyList.get(historyList.size() - 1).getId())
                    .userId(loginUser.getId())
                    .build();

            boolean saveResult = this.save(summary);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存总结记录失败");
            }

            // 6. 标记原始对话为已总结（但不删除，保留最近几轮）
            markMessagesAsSummarized(historyList, summary.getId());

            log.info("应用 {} 的对话总结完成，总结ID: {}, 涵盖轮数: {}", 
                    appId, summary.getId(), summary.getSummarizedRounds());

            return summary.getId();

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("应用 {} 的AI总结过程失败", appId, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI总结失败: " + e.getMessage());
        }
    }

    @Override
    public ChatSummary getLatestSummary(Long appId) {
        if (appId == null || appId <= 0) {
            return null;
        }

        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("appId", appId)
                .orderBy("createTime", false)  // 按时间倒序
                .limit(1);

        List<ChatSummary> summaries = this.list(queryWrapper);
        return CollUtil.isNotEmpty(summaries) ? summaries.get(0) : null;
    }

    @Override
    public boolean shouldSummarize(Long appId) {
        if (appId == null || appId <= 0) {
            return false;
        }

        // 查询未被总结的对话轮数
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("appId", appId)
                .eq("isSummarized", 0);

        long unsummarizedCount = chatHistoryService.count(queryWrapper);
        long unsummarizedRounds = unsummarizedCount / 2;  // 每轮包含2条消息

        // 如果未总结的轮数达到阈值，触发总结
        boolean shouldSummarize = unsummarizedRounds >= SUMMARY_TRIGGER_ROUNDS;

        if (shouldSummarize) {
            log.info("应用 {} 达到总结阈值：未总结轮数={}", appId, unsummarizedRounds);
        }

        return shouldSummarize;
    }

    /**
     * 格式化对话历史为AI可理解的文本
     */
    private String formatConversationForSummary(List<ChatHistory> historyList) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < historyList.size(); i++) {
            ChatHistory chat = historyList.get(i);
            String role = ChatHistoryMessageTypeEnum.USER.getValue().equals(chat.getMessageType()) 
                    ? "用户" : "AI";
            
            sb.append(role).append(": ").append(chat.getMessage());
            
            // 添加分隔符（最后一条不加）
            if (i < historyList.size() - 1) {
                sb.append("\n\n");
            }
        }

        return sb.toString();
    }

    /**
     * 标记对话消息为已总结
     * 策略：保留最近KEEP_RECENT_ROUNDS轮完整对话，之前的标记为已总结
     */
    private void markMessagesAsSummarized(List<ChatHistory> historyList, Long summaryId) {
        if (CollUtil.isEmpty(historyList)) {
            return;
        }

        // 计算需要标记的消息数量
        int totalMessages = historyList.size();
        int messagesToKeep = KEEP_RECENT_ROUNDS * 2;  // 保留最近5轮（10条消息）
        int messagesToMark = totalMessages - messagesToKeep;

        // 如果总消息数不超过保留数量，则不标记
        if (messagesToMark <= 0) {
            log.info("应用对话未达到标记阈值，总数={}, 保留={}", totalMessages, messagesToKeep);
            return;
        }

        // 标记较早的消息为已总结（前面的消息）
        for (int i = 0; i < messagesToMark; i++) {
            ChatHistory chat = historyList.get(i);
            chat.setIsSummarized(1);
            chat.setSummaryId(summaryId);
            chatHistoryService.updateById(chat);
        }

        log.info("标记 {} 条消息为已总结，保留最近 {} 条完整对话", messagesToMark, messagesToKeep);
    }
}
