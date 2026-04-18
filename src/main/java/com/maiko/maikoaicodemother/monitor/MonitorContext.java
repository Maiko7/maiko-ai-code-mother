package com.maiko.maikoaicodemother.monitor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 监控上下文信息载体
 * <p>
 * 用于在拦截器、监听器和业务服务之间传递关键的追踪标识（如用户ID、应用ID）。
 * 实现 Serializable 接口以支持在线程间或分布式环境下的安全传递。
 * </p>
 *
 * @author Maiko7
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitorContext implements Serializable {

    /**
     * 当前请求关联的用户 ID
     */
    private String userId;

    /**
     * 当前请求关联的应用 ID
     */
    private String appId;

    /**
     * 序列化版本 UID，确保类结构变更时的兼容性
     */
    @Serial
    private static final long serialVersionUID = 1L;
}