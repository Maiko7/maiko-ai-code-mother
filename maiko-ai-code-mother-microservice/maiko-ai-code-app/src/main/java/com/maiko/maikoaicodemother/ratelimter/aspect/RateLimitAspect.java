package com.maiko.maikoaicodemother.ratelimter.aspect;

import com.maiko.maikoaicodemother.exception.BusinessException;
import com.maiko.maikoaicodemother.exception.ErrorCode;
import com.maiko.maikoaicodemother.innerservice.InnerUserService;
import com.maiko.maikoaicodemother.model.entity.User;
import com.maiko.maikoaicodemother.ratelimter.annotation.RateLimit;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.Duration;

/**
 * 基于 Redisson 的分布式限流切面
 * <p>
 * 该组件利用 AOP 技术拦截标记了 {@link RateLimit} 注解的方法。
 * 通过 Redisson 实现的令牌桶算法，在分布式环境下对 API 接口、用户或 IP 进行精准的流量控制。
 * </p>
 *
 * @author Maiko7
 */
@Aspect
@Component
@Slf4j
public class RateLimitAspect {

    /**
     * 注入 Redisson 客户端，用于操作分布式限流器
     */
    @Resource
    private RedissonClient redissonClient;

    /**
     * 注入用户服务，用于获取当前登录用户信息（USER 维度限流时使用）
     */
    @Resource
    @Lazy
    private InnerUserService userService;

    /**
     * 前置通知：在目标方法执行前进行限流检查
     * <p>
     * 1. 生成唯一的限流 Key。
     * 2. 获取并配置限流器参数（速率、时间窗口）。
     * 3. 尝试获取令牌，若失败则抛出业务异常。
     * </p>
     *
     * @param point      连接点，包含方法签名等上下文信息
     * @param rateLimit  限流注解实例，包含限流配置参数
     */
    @Before("@annotation(rateLimit)")
    public void doBefore(JoinPoint point, RateLimit rateLimit) {
        // 1. 根据注解配置和请求上下文生成唯一的限流 Key
        String key = generateRateLimitKey(point, rateLimit);

        // 2. 获取对应的分布式限流器
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);

        // 3. 设置限流器的过期时间，防止无用 Key 长期占用内存
        rateLimiter.expire(Duration.ofHours(1));

        // 4. 尝试设置限流规则（幂等操作，如果已存在则不会覆盖）
        // RateType.OVERALL 表示所有客户端共享配额
        rateLimiter.trySetRate(RateType.OVERALL, rateLimit.rate(), rateLimit.rateInterval(), RateIntervalUnit.SECONDS);

        // 5. 尝试获取令牌（非阻塞模式）
        // 如果获取失败（返回 false），说明触发限流
        if (!rateLimiter.tryAcquire(1)) {
            log.warn("触发限流 - Key: {}, 限制速率: {}", key, rateLimit.rate());
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST, rateLimit.message());
        }
    }

    /**
     * 生成限流键 (Key)
     * <p>
     * 根据限流类型（API/USER/IP）拼接不同的 Redis Key，确保不同维度的流量被独立统计。
     * 逻辑优先级：自定义前缀 -> 限流类型标识 -> 具体标识值（方法名/用户ID/IP）。
     * </p>
     *
     * @param point      连接点
     * @param rateLimit  限流注解
     * @return 唯一的限流字符串键
     */
    private String generateRateLimitKey(JoinPoint point, RateLimit rateLimit) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append("rate_limit:");

        // 添加自定义前缀（如果注解中指定了）
        if (!rateLimit.key().isEmpty()) {
            keyBuilder.append(rateLimit.key()).append(":");
        }

        // 根据限流类型生成具体的后缀
        switch (rateLimit.limitType()) {
            case API:
                // 接口级别：使用 "类名.方法名" 作为标识
                MethodSignature signature = (MethodSignature) point.getSignature();
                Method method = signature.getMethod();
                keyBuilder.append("api:").append(method.getDeclaringClass().getSimpleName())
                        .append(".").append(method.getName());
                break;
            case USER:
                // 用户级别：优先使用用户 ID
                try {
                    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                    if (attributes != null) {
                        HttpServletRequest request = attributes.getRequest();
                        User loginUser = InnerUserService.getLoginUser(request);
                        keyBuilder.append("user:").append(loginUser.getId());
                    } else {
                        // 无法获取请求上下文，降级为 IP 限流
                        keyBuilder.append("ip:").append(getClientIP());
                    }
                } catch (BusinessException e) {
                    // 未登录或获取用户失败，降级为 IP 限流
                    keyBuilder.append("ip:").append(getClientIP());
                }
                break;
            case IP:
                // IP 级别：直接使用客户端 IP
                keyBuilder.append("ip:").append(getClientIP());
                break;
            default:
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的限流类型");
        }
        return keyBuilder.toString();
    }

    /**
     * 获取客户端真实 IP 地址
     * <p>
     * 处理多级代理情况，依次检查 X-Forwarded-For, X-Real-IP 等头部信息。
     * 如果有多个 IP（逗号分隔），取第一个作为真实 IP。
     * </p>
     *
     * @return 客户端 IP 字符串
     */
    private String getClientIP() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "unknown";
        }
        HttpServletRequest request = attributes.getRequest();

        // 获取代理转发的 IP
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 处理多级代理产生的逗号分隔 IP 列表，取第一个真实 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "unknown";
    }
}