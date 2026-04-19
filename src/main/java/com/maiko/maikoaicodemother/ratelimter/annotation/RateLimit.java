package com.maiko.maikoaicodemother.ratelimter.annotation;

import com.maiko.maikoaicodemother.ratelimter.enums.RateLimitType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义限流注解
 * <p>
 * 用于标记需要进行流量控制的方法。结合 AOP 切面和 Redisson 实现分布式限流。
 * 支持配置限流的维度（IP/用户/接口）、速率以及提示信息。
 * </p>
 *
 * @author Maiko7
 */
@Target({ElementType.METHOD}) // 仅允许在方法上使用
@Retention(RetentionPolicy.RUNTIME) // 运行时保留，以便 AOP 反射读取
public @interface RateLimit {

    /**
     * 限流 Key 的前缀
     * <p>
     * 用于区分不同的业务场景。如果不填，则默认只根据 limitType 生成 Key。
     * 例如：填写 "login" 后，生成的 Redis Key 可能为 "rate_limit:login:..."
     * </p>
     */
    String key() default "";

    /**
     * 限流阈值：每个时间窗口内允许的最大请求数
     * <p>
     * 配合 rateInterval 使用。
     * 默认值：10，表示在指定时间窗口内最多允许 10 次请求。
     * </p>
     */
    int rate() default 10;

    /**
     * 时间窗口大小（单位：秒）
     * <p>
     * 配合 rate 使用。
     * 默认值：1，表示以 1 秒为一个滑动窗口来计算请求频率。
     * </p>
     */
    int rateInterval() default 1;

    /**
     * 限流维度类型
     * <p>
     * 决定是根据 IP、用户ID 还是 API 接口来进行限流统计。
     * 默认值：USER，即针对当前登录用户进行限流。
     * </p>
     */
    RateLimitType limitType() default RateLimitType.USER;

    /**
     * 触发限流时的异常提示信息
     * <p>
     * 当请求超过设定的阈值时，抛出的业务异常中包含的消息内容。
     * </p>
     */
    String message() default "请求过于频繁，请稍后再试";
}