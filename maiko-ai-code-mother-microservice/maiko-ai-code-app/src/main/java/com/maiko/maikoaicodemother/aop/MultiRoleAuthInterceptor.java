package com.maiko.maikoaicodemother.aop;

import cn.hutool.core.util.StrUtil;
import com.maiko.maikoaicodemother.annotation.AuthCheck;
import com.maiko.maikoaicodemother.exception.BusinessException;
import com.maiko.maikoaicodemother.exception.ErrorCode;
import com.maiko.maikoaicodemother.innerservice.InnerUserService;
import com.maiko.maikoaicodemother.model.entity.User;
import com.maiko.maikoaicodemother.model.enums.UserRoleEnum;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 多角色权限拦截器
 * <p>基于AOP实现，通过@AuthCheck注解对接口进行权限校验</p>
 * <p>支持动态角色校验，不硬编码具体角色，通过字符串对比判断用户是否具有所需权限</p>
 */
@Aspect
@Component
public class MultiRoleAuthInterceptor {

    /**
     * 权限校验拦截方法
     * <p>环绕通知处理带有@AuthCheck注解的方法，执行权限验证逻辑</p>
     *
     * @param joinPoint 连接点对象，用于获取方法执行上下文和继续执行目标方法
     * @param authCheck 权限校验注解，包含接口要求的角色信息
     * @return 目标方法的返回值
     * @throws Throwable 业务异常或目标方法抛出的异常
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        // 1. 获取接口要求的角色
        String mustRole = authCheck.mustRole();

        // 2. 获取当前请求
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        // 3. 获取当前登录用户（必须已登录）
        User loginUser = InnerUserService.getLoginUser(request);
        String userRole = loginUser.getUserRole();

        // 4. 接口不需要权限，直接放行
        if (StrUtil.isBlank(mustRole)) {
            return joinPoint.proceed();
        }

        // 到下面这里mustRole就不是空了，就肯定有角色了。
        // 5. 拿到要求的角色枚举
        UserRoleEnum requiredRole = UserRoleEnum.getEnumByValue(mustRole);
        if (requiredRole == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "权限配置错误：无效的角色类型");
        }

        // 6. 用户没有角色 → 拒绝
        if (StrUtil.isBlank(userRole)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        /**
         * 你的这个代码你说加vip，加任意角色都行？为什么 逻辑在哪里
         * 因为这段代码不写死任何角色，只做 “对比”：
         * 要求的角色 == 用户的角色 → 过
         * 不等于 → 不过
         * 它做的事情只有一件：字符串 equals 对比。它根本不关心这个角色叫什么！
         * 不像之前的UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(userRoleEnum)它写死了ADMIN
         */

        // 7. 角色不匹配 → 拒绝
        if (!requiredRole.getValue().equals(userRole)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 8. 如果被封号，直接拒绝
        /**
         * 你下面这样写是错的，为什么错？
         * requiredRole = 接口要求的角色（比如 admin /user）
         * 你这句话的意思是：“如果接口要求的角色是封号，才报错”这完全逻辑颠倒了！
         *
         * userRole = 你自己是什么身份
         * requiredRole = 接口需要什么身份
         * 封号判断：
         * 封号拦截 → 判断的是【用户自己的角色 userRole】
         * 不是接口要求的角色 requiredRole！不管接口要什么身份，只要你是 ban → 直接拦截！
         */
//        if (UserRoleEnum.BAN.equals(requiredRole)) {
//            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
//        }
        // 8. 如果被封号，直接拒绝
        if (UserRoleEnum.BAN.getValue().equals(userRole)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "账号已封禁");
        }

        // 9. 校验通过，放行
        return joinPoint.proceed();
    }
}