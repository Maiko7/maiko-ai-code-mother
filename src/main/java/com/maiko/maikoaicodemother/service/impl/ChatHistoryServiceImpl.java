package com.maiko.maikoaicodemother.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.maiko.maikoaicodemother.constant.UserConstant;
import com.maiko.maikoaicodemother.exception.ErrorCode;
import com.maiko.maikoaicodemother.exception.ThrowUtils;
import com.maiko.maikoaicodemother.mapper.ChatHistoryMapper;
import com.maiko.maikoaicodemother.model.dto.chathistory.ChatHistoryQueryRequest;
import com.maiko.maikoaicodemother.model.entity.App;
import com.maiko.maikoaicodemother.model.entity.ChatHistory;
import com.maiko.maikoaicodemother.model.entity.User;
import com.maiko.maikoaicodemother.model.enums.ChatHistoryMessageTypeEnum;
import com.maiko.maikoaicodemother.service.AppService;
import com.maiko.maikoaicodemother.service.ChatHistoryService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史服务层实现
 * @author: Maiko7
 * @create: 2026-04-12 21:25
 */
@Service
@Slf4j
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    @Resource
    @Lazy
    private AppService appService;

    /**
     * 保存对话消息。什么时候添加呢？
     * 1. AI的完整回复，回复完了肯定要保存AI的会话消息
     * @param appId       应用 id
     * @param message     消息
     * @param messageType 消息类型
     * @param userId      用户 id
     * @return
     */
    @Override
    public boolean addChatMessage(Long appId, String message, String messageType, Long userId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "消息内容不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(messageType), ErrorCode.PARAMS_ERROR, "消息类型不能为空");
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        // 验证消息类型是否有效
        ChatHistoryMessageTypeEnum messageTypeEnum = ChatHistoryMessageTypeEnum.getEnumByValue(messageType);
        ThrowUtils.throwIf(messageTypeEnum == null, ErrorCode.PARAMS_ERROR, "不支持的消息类型: " + messageType);
        // 版本1 ChatHistory chatHistory = new ChatHistory(); chatHistory.setId();chatHistory.serMessage(message);....
        // ChatHistory它打上了@Builder注解，所以可以这样创建。构造器模式
        ChatHistory chatHistory = ChatHistory.builder()
                .appId(appId)
                .message(message)
                .messageType(messageType)
                .userId(userId)
                .build();
        return this.save(chatHistory);
    }

    @Override
    public boolean deleteByAppId(Long appId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("appId", appId);
        return this.remove(queryWrapper);
    }

    @Override
    public Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                                      LocalDateTime lastCreateTime,
                                                      User loginUser) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(pageSize <= 0 || pageSize > 50, ErrorCode.PARAMS_ERROR, "页面大小必须在1-50之间");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 验证权限：只有应用创建者和管理员可以查看
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
        boolean isCreator = app.getUserId().equals(loginUser.getId());
        ThrowUtils.throwIf(!isAdmin && !isCreator, ErrorCode.NO_AUTH_ERROR, "无权查看该应用的对话历史");
        // 构建查询条件
        ChatHistoryQueryRequest queryRequest = new ChatHistoryQueryRequest();
        queryRequest.setAppId(appId);
        queryRequest.setLastCreateTime(lastCreateTime);
        QueryWrapper queryWrapper = this.getQueryWrapper(queryRequest);
        // 查询数据
        return this.page(Page.of(1, pageSize), queryWrapper);
    }

    @Override
    public int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount) {
        try {
            // 直接构造查询条件，起始点为 1 而不是 0，用于排除最新的用户消息
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .eq(ChatHistory::getAppId, appId)
                    .orderBy(ChatHistory::getCreateTime, false)
                    .limit(1, maxCount);
            List<ChatHistory> historyList = this.list(queryWrapper);
            if (CollUtil.isEmpty(historyList)) {
                return 0;
            }
            // 反转列表，确保按时间正序（老的在前，新的在后）
            historyList = historyList.reversed();
            // 按时间顺序添加到记忆中
            int loadedCount = 0;
            // 先清理历史缓存，防止重复加载
            chatMemory.clear();
            for (ChatHistory history : historyList) {
                if (ChatHistoryMessageTypeEnum.USER.getValue().equals(history.getMessageType())) {
                    chatMemory.add(UserMessage.from(history.getMessage()));
                    loadedCount++;
                } else if (ChatHistoryMessageTypeEnum.AI.getValue().equals(history.getMessageType())) {
                    chatMemory.add(AiMessage.from(history.getMessage()));
                    loadedCount++;
                }
            }
            log.info("成功为 appId: {} 加载了 {} 条历史对话", appId, loadedCount);
            return loadedCount;
        } catch (Exception e) {
            log.error("加载历史对话失败，appId: {}, error: {}", appId, e.getMessage(), e);
            // 加载失败不影响系统运行，只是没有历史上下文
            return 0;
        }
    }


    /**
     * 获取查询包装类
     *
     * @param chatHistoryQueryRequest
     * @return
     */
    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (chatHistoryQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chatHistoryQueryRequest.getId();
        String message = chatHistoryQueryRequest.getMessage();
        String messageType = chatHistoryQueryRequest.getMessageType();
        Long appId = chatHistoryQueryRequest.getAppId();
        Long userId = chatHistoryQueryRequest.getUserId();
        LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime();
        // 排序字段
        String sortField = chatHistoryQueryRequest.getSortField();
        // 排序顺序
        String sortOrder = chatHistoryQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.eq("id", id)
                .like("message", message)
                .eq("messageType", messageType)
                .eq("appId", appId)
                .eq("userId", userId);
        // 游标查询逻辑 - 只使用 createTime 作为游标
        if (lastCreateTime != null) {
            /**
             * 筛选出createTim（创建时间）早于（小于）lastCreateTime（上一次的时间）的所有记录。
             *
             * 结合“游标分页”场景的理解：
             * 这行代码正是实现高效分页（游标分页）的核心！
             * 场景：你正在看聊天记录，第一页最后一条消息的时间是 12:00:00。
             * 动作：当你加载第二页时，你会把 12:00:00 传给后端作为 lastCreateTime。
             * 执行：queryWrapper.lt("createTime", "12:00:00")
             * 结果：数据库只会查找 12:00:00 之前 的消息。
             */
            queryWrapper.lt("createTime", lastCreateTime);
        }
        // 排序
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            // 默认按创建时间降序排列
            queryWrapper.orderBy("createTime", false);
        }
        return queryWrapper;
    }

    @Override
    public String exportChatHistoryToMarkdown(Long appId, LocalDateTime startTime, LocalDateTime endTime, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        // 2. 验证权限：只有应用创建者和管理员可以导出
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
        boolean isCreator = app.getUserId().equals(loginUser.getId());
        ThrowUtils.throwIf(!isAdmin && !isCreator, ErrorCode.NO_AUTH_ERROR, "无权导出该应用的对话历史");

        // 3. 构建查询条件，按时间范围筛选
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("appId", appId)
                .orderBy("createTime", true); // 按时间正序排列

        // 添加时间范围过滤
        if (startTime != null) {
            queryWrapper.ge("createTime", startTime);
        }
        if (endTime != null) {
            queryWrapper.le("createTime", endTime);
        }

        // 4. 查询所有符合条件的对话历史
        List<ChatHistory> historyList = this.list(queryWrapper);

        // 5. 生成Markdown内容
        StringBuilder markdown = new StringBuilder();

        // 标题
        markdown.append("# ").append(app.getAppName()).append(" - 对话历史\n\n");

        // 导出信息
        markdown.append("**导出时间**: ").append(LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");

        if (startTime != null || endTime != null) {
            markdown.append("**时间范围**: ");
            if (startTime != null) {
                markdown.append(startTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            markdown.append(" ~ ");
            if (endTime != null) {
                markdown.append(endTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            markdown.append("\n\n");
        }

        markdown.append("**对话轮数**: ").append(app.getTotalRounds()).append(" 轮\n\n");

        markdown.append("---\n\n");

        // 如果没有对话历史
        if (CollUtil.isEmpty(historyList)) {
            markdown.append("*暂无对话记录*\n");
            return markdown.toString();
        }

        // 6. 格式化对话内容
        int roundNumber = 0;
        ChatHistory lastMessage = null;

        for (int i = 0; i < historyList.size(); i++) {
            ChatHistory current = historyList.get(i);

            // 如果是用户消息，开始新的一轮
            if (ChatHistoryMessageTypeEnum.USER.getValue().equals(current.getMessageType())) {
                roundNumber++;
                markdown.append("## 第").append(roundNumber).append("轮\n\n");

                // 添加时间戳
                String timeStr = current.getCreateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                markdown.append("*").append(timeStr).append("*\n\n");

                // 用户消息
                markdown.append("### 用户\n\n");
                markdown.append(current.getMessage()).append("\n\n");

                lastMessage = current;
            }
            // 如果是AI消息，且上一条是用户消息，则配对显示
            else if (ChatHistoryMessageTypeEnum.AI.getValue().equals(current.getMessageType())
                    && lastMessage != null
                    && ChatHistoryMessageTypeEnum.USER.getValue().equals(lastMessage.getMessageType())) {

                markdown.append("### AI\n\n");
                markdown.append(current.getMessage()).append("\n\n");

                // 添加分隔线（最后一轮不加）
                if (i < historyList.size() - 1) {
                    markdown.append("---\n\n");
                }

                lastMessage = null; // 重置，准备下一轮
            }
        }

        // 7. 添加总结
        markdown.append("---\n\n");
        markdown.append("*共 ").append(historyList.size()).append(" 条消息，")
                .append(roundNumber).append(" 轮对话*\n");

        log.info("成功导出应用 {} 的对话历史，共 {} 条消息，{} 轮", appId, historyList.size(), roundNumber);

        return markdown.toString();
    }


}
