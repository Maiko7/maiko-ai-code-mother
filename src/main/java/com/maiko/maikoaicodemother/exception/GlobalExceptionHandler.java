package com.maiko.maikoaicodemother.exception;


import com.maiko.maikoaicodemother.common.BaseResponse;
import com.maiko.maikoaicodemother.common.ResultUtils;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * @RestControllerAdvice ，你可以把它理解为一个全局的拦截器
 * 以前每个Controller都要写try-catch，现在你可以在这里统一捕获异常，返回统一的JSON格式
 */

/**
 *  注意!由于本项目使用的 Spring Boot 版本>=3.4、并且是 OpenAPI3 版本的Knife4j，这会导致@RestcontrollerAdvice注解不兼容，
 *  所以必须给这个类加上@Hidden注解，不被Swagger加载。
 */
@Hidden
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }
}
