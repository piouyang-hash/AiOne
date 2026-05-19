package org.myfx.controls.aione.ServiceCommon.exception;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.ServiceCommon.AppResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 1. @RestControllerAdvice的参数配置错误
 * 你提到注解是@RestControllerAdvice("commonGlobalExceptionHandler")，
 * 这是关键错误！@RestControllerAdvice的value/basePackages参数需要指定包路径
 * （用于限定处理器作用的控制器包），而不是随便的字符串。如果写成非包路径的值，
 * 会导致处理器只对空包或错误包下的控制器生效，书籍微服务的控制器自然不会被匹配。
 */
@RestControllerAdvice()
@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE)
public class CommonGlobalExceptionHandler {

    // 初始化时打印日志，确认处理器被加载
    @PostConstruct
    public void init() {
        log.info("===== ServiceCommon全局异常处理器已加载 =====");
    }


    @ExceptionHandler(AuthException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public AppResponse<?> handleAuthException(AuthException e) {
        // 机器可读错误码（直接使用枚举名）
        String errorCode = e.getAuthError() == null ? "AUTH_ERROR" : e.getAuthError().name();

        Map<String, Object> payload = new HashMap<>();
        payload.put("errorCode", errorCode);
        payload.put("detail", e.getMessage()); // 供前端调试/日志用（展示请谨慎）

        // 记录日志：warn 级别，包含错误码，便于排查（生产可改为 error 级别）
        log.warn("AuthException: {} - {} - errorCode={}", e.getClass().getSimpleName(), e.getMessage(), errorCode);

        // 返回统一格式：HTTP 401 + ApiResponse.code 401，message 为用户可见文案，data 带机器码
        return AppResponse.error(401, e.getMessage(), payload);
    }

    /**
     * 处理角色权限不足的ForbiddenException
     */
    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN) // 响应状态码设为403（Forbidden），更符合语义
    public AppResponse<Void> handleForbiddenException(ForbiddenException e) {
        // 记录warn级别日志（权限问题，非服务器错误）
        log.warn("ForbiddenException: {}", e.getMessage());
        return AppResponse.error(403, e.getMessage(), null); // 错误码用403，匹配HTTP状态码
    }

    @ExceptionHandler(ServiceOnlyAccessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public AppResponse<Void> handleNullParamException(ServiceOnlyAccessException e) {
        // 记录 warn 级别日志（客户端输入错误，非服务器异常）
        log.warn("ServiceOnlyAccessException: {}", e.getMessage());
        return AppResponse.error(400, e.getMessage(), null);
    }

    /**
     * 兜底异常处理器：捕获所有未被特定处理的异常（全局兜底）
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public AppResponse<Void> handleGlobalException(Exception e) {
        // 记录 ERROR 级别日志并打印堆栈（关键！便于排查未知错误）
        log.error("Unknown server exception occurred", e);

        // 开发环境可放开下面一行，便于调试
        String userMessage = "服务器内部错误：" + e.getMessage();

        return AppResponse.error(500, userMessage, null);
    }
}
