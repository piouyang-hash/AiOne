package org.myfx.controls.aione.UserService.common.exception;

import lombok.Getter;

/**
 * 注册相关异常类
 * 用于封装注册过程中可能出现的各种错误（用户名已存在、邮箱已注册等）
 */
@Getter
public class RegisterException extends RuntimeException {

    /**
     * 注册错误类型枚举：涵盖常见注册失败的原因
     */
    @Getter
    public enum RegisterError {
        USERNAME_EXISTS("用户名已被占用，请更换其他用户名"),
        EMAIL_EXISTS("该邮箱已被注册，请使用其他邮箱"),
        INVALID_VERIFICATION_CODE("验证码错误或已过期"),
        OTHER("其他注册异常");

        // 错误描述信息
        private final String message;

        // 构造器
        RegisterError(String message) {
            this.message = message;
        }
    }

    /**
     * 持有具体注册错误类型
     */
    private final RegisterError registerError;

    /**
     * 构造方法 1：仅传入错误类型（常规使用）
     */
    public RegisterException(RegisterError registerError) {
        super(registerError.getMessage());
        this.registerError = registerError;
    }

    /**
     * 构造方法 2：传入错误类型 + 补充描述（灵活扩展）
     */
    public RegisterException(RegisterError registerError, String extraMessage) {
        super(registerError.getMessage() + "：" + extraMessage);
        this.registerError = registerError;
    }

    // ============================== 使用示例 ==============================
    //
    // 1️⃣ 用户名已存在：
    // throw new RegisterException(RegisterError.USERNAME_EXISTS);
    //
    // 2️⃣ 邮箱已注册：
    // throw new RegisterException(RegisterError.EMAIL_EXISTS);
    //
    // 3️⃣ 验证码错误：
    // throw new RegisterException(RegisterError.INVALID_VERIFICATION_CODE);
    //
    // 4️⃣ 其他注册异常（如系统错误）：
    // throw new RegisterException(RegisterError.OTHER, "数据库连接失败");
    //
    // ====================================================================
}