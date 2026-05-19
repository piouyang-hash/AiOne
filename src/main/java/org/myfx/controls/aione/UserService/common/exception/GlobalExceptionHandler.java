package org.myfx.controls.aione.UserService.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.ServiceCommon.AppResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 全局异常处理器：统一拦截并处理自定义异常，自动返回ApiResponse格式
 */
@RestControllerAdvice
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {
    
    @ExceptionHandler(NullParamException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public AppResponse<Void> handleNullParamException(NullParamException e) {
        // 记录 warn 级别日志（客户端输入错误，非服务器异常）
        log.warn("NullParamException: {}", e.getMessage());
        return AppResponse.error(400, e.getMessage(), null);
    }

    /**
     * 处理用户无头像异常
     */
    @ExceptionHandler(UserHasNoAvatarException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 或HttpStatus.NOT_FOUND，根据业务选择
    public AppResponse<Void> handleUserHasNoAvatarException(UserHasNoAvatarException e) {
        log.warn("UserHasNoAvatarException: {}", e.getMessage());
        return AppResponse.error(400, e.getMessage(), null); // 错误码可自定义（如400/404）
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public AppResponse<Void> handleNoResourceFoundException(NoResourceFoundException e) {
        log.warn("NoResourceFoundException: {}", e.getMessage());
        return AppResponse.error(404, "试图访问静态资源，但资源不存在（路径可能错误或文件未上传）", null);
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)  // 用户不存在对应404状态码（资源未找到）
    public AppResponse<Void> handleUserNotFoundException(UserNotFoundException e) {
        log.warn("UserNotFoundException: {}", e.getMessage());  // 记录警告日志（非错误，可能是正常业务场景）
        // 返回错误响应：code=404，消息直接使用异常中的描述
        return AppResponse.error(404, e.getMessage(), null);
    }

    @ExceptionHandler(LoginException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public AppResponse<?> handleLoginException(LoginException e) {
        // 机器可读错误码（数字或字符串），以及人类可读message
        Map<String,Object> payload = new HashMap<>();
        payload.put("errorCode", "INVALID_CREDENTIALS"); // 或数字 1001
        payload.put("detail", e.getMessage()); // 用户可读的文字

        // 记录异常日志（便于排查问题）
        log.warn("LoginException: {} - {}", e.getClass().getSimpleName(), e.getMessage());

        // ApiResponse.error(int code, String message, T data)
        // 我们仍把 message 用作“简短用户消息”（兼容现状）
        return AppResponse.error(401, e.getMessage(), payload);
    }

    @ExceptionHandler(RegisterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 注册错误多为客户端输入问题，使用 400 状态码
    public AppResponse<?> handleRegisterException(RegisterException e) {
        // 构建错误响应 payload（机器可读码 + 人类可读详情）
        Map<String, Object> payload = new HashMap<>();
        // 错误码使用枚举名称（如"USERNAME_EXISTS"），保持与登录异常处理器风格一致
        payload.put("errorCode", e.getRegisterError().name());
        payload.put("detail", e.getMessage()); // 携带具体错误描述（如"用户名已被占用"）

        // 记录异常日志（便于排查问题）
        log.warn("RegisterException: {} - {}", e.getClass().getSimpleName(), e.getMessage());

        // 返回统一格式的错误响应（状态码400，消息为异常描述，数据为payload）
        return AppResponse.error(400, e.getMessage(), payload);
    }

    @ExceptionHandler(VipException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // VIP错误多为业务逻辑问题（如重复开通），用 400 状态码
    public AppResponse<?> handleVipException(VipException e) {
        // 构建错误响应 payload（机器可读码 + 人类可读详情）
        Map<String, Object> payload = new HashMap<>();
        // 错误码使用枚举名称（如"ALREADY_VIP"），与注册异常处理器风格统一
        payload.put("errorCode", e.getVipError().name());
        payload.put("detail", e.getMessage()); // 携带具体错误描述（如"用户已开通VIP，无需重复操作"）

        // 记录异常日志（便于排查问题，级别为warn，非error，因为多为业务预期内错误）
        log.warn("VipException: {} - {}", e.getClass().getSimpleName(), e.getMessage());

        // 返回统一格式的错误响应（状态码400，消息为异常描述，数据为payload）
        return AppResponse.error(400, e.getMessage(), payload);
    }

    @ExceptionHandler(IllegalParamException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public AppResponse<Void> handleIllegalParamException(IllegalParamException e) {
        log.warn("IllegalParamException: {}", e.getMessage());
        return AppResponse.error(400, e.getMessage(), null);
    }

    @ExceptionHandler(InvalidImageFormatException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public AppResponse<Void> handleInvalidImageFormatException(InvalidImageFormatException e) {
        log.warn("InvalidImageFormatException: {}", e.getMessage());
        return AppResponse.error(400, e.getMessage(), null);
    }

    @ExceptionHandler(OldNewPasswordSameException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 客户端请求错误，返回400
    public AppResponse<Void> handleOldNewPasswordSame(OldNewPasswordSameException e) {
        // 日志记录（可选，方便后端排查）
        log.warn("OldNewPasswordSameException：{}", e.getMessage());
        return AppResponse.error(400, e.getMessage(), null);
    }

    // 新增：处理邮箱一致异常（格式完全对应）
    @ExceptionHandler(OldNewEmailSameException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 同样返回400客户端错误
    public AppResponse<Void> handleOldNewEmailSame(OldNewEmailSameException e) {
        // 日志级别、格式与密码异常保持一致
        log.warn("OldNewEmailSameException：{}", e.getMessage());
        // 响应格式统一：错误码400 + 异常消息 + null数据
        return AppResponse.error(400, e.getMessage(), null);
    }

    // 专门处理“用户已存在”异常
    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 明确返回 400 状态码
    public AppResponse<Void> handleUserAlreadyExists(UserAlreadyExistsException e) {
        log.warn("注册失败：{}", e.getMessage()); // 日志明确是注册场景
        return AppResponse.error(400, e.getMessage(), null);
    }

    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN) // 权限不足用 403 状态码（比400更贴合语义）
    public AppResponse<Void> handleSecurityException(SecurityException e) {
        log.warn("SecurityException: {}", e.getMessage()); // 记录警告日志
        // 返回错误响应：code=403（禁止访问），消息用异常信息
        return AppResponse.error(403, e.getMessage(), null);
    }

    /**
     * 兜底异常处理：显示详细异常信息（方便开发排查）
     * - 记录完整异常堆栈到 error 日志（方便查错）
     * - 返回统一格式的错误信息给前端（避免泄露敏感信息）
     */
    @ExceptionHandler(Exception.class)
    public AppResponse<Void> handleOtherException(Exception e) {
        // 记录完整错误（error 级别，包含堆栈）
        log.error("Unhandled exception caught: {}", e.getMessage(), e);

        // 生成简短的关键堆栈信息供返回（避免太长）
        String stackTrace = Arrays.stream(e.getStackTrace())
                .limit(3)
                .map(stack -> stack.getClassName() + "." + stack.getMethodName() + "() 第" + stack.getLineNumber() + "行")
                .collect(Collectors.joining("；"));

        String detailMsg = "系统繁忙，请稍后再试【异常类型：" + e.getClass().getName()
                + "；异常消息：" + (e.getMessage() == null ? "无具体异常消息" : e.getMessage())
                + "；关键堆栈：" + stackTrace + "】";

        // 返回 500 错误
        return AppResponse.error(500, detailMsg, null);
    }
}