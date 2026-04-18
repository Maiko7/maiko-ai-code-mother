package com.maiko.maikoaicodemother.monitor;

import lombok.extern.slf4j.Slf4j;

/**
 * 监控上下文持有者
 * <p>
 * 基于 ThreadLocal 实现同线程内的上下文隔离与共享。
 * 主要用于在 Web 请求处理链路或异步回调中，传递用户身份、应用标识等关键追踪信息，
 * 避免在深层调用链中显式传递参数。
 * </p>
 *
 * @author Maiko7
 */
@Slf4j
public class MonitorContextHolder {

    /**
     * 线程本地变量存储容器
     */
    private static final ThreadLocal<MonitorContext> CONTEXT_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前线程的监控上下文
     * <p>
     * 通常在拦截器或过滤器入口处调用。
     * </p>
     *
     * @param context 包含用户 ID 和应用 ID 的上下文对象
     */
    public static void setContext(MonitorContext context) {
        CONTEXT_HOLDER.set(context);
    }

    /**
     * 获取当前线程的监控上下文
     * <p>
     * 供下游业务逻辑或服务层调用以获取当前请求的身份信息。
     * </p>
     *
     * @return 当前线程绑定的上下文对象，若未设置则返回 null
     */
    public static MonitorContext getContext() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 清除当前线程的监控上下文
     * <p>
     * <b>重要：</b> 必须在请求处理结束（如 doFinally）时调用此方法，
     * 以防止线程池复用导致的内存泄漏或脏数据污染。
     * </p>
     */
    public static void clearContext() {
        CONTEXT_HOLDER.remove();
    }
}