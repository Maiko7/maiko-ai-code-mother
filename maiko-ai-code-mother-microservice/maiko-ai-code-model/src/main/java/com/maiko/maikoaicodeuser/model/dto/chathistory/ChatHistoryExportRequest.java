package com.maiko.maikoaicodeuser.model.dto.chathistory;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对话历史导出请求
 */
@Data
public class ChatHistoryExportRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 应用ID
     */
    private Long appId;

    /**
     * 开始时间（可选）
     */
    private LocalDateTime startTime;

    /**
     * 结束时间（可选）
     */
    private LocalDateTime endTime;
}
