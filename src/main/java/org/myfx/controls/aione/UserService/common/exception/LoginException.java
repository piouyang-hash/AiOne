package org.myfx.controls.aione.UserService.common.exception;

import lombok.Getter;

/**
 * 登录相关异常类
 * 用于封装登录过程中可能出现的各种错误（密码错误、账号被封禁等）
 * 🚫 安全设计：不暴露“用户名不存在”类信息，防止暴力枚举攻击
 */
@Getter
public class LoginException extends RuntimeException {

    /**
     * 登录错误类型枚举：涵盖常见登录失败的原因
     */
    @Getter
    public enum LoginError {
        INVALID_CREDENTIALS("用户名或密码错误"), // 模糊错误，防止泄露账号存在性
        ACCOUNT_LOCKED("账号已被锁定或禁用"),
        INVALID_INPUT("输入格式错误（如用户名为空或密码太短）"),
        INVALID_VERIFICATION_CODE("验证码错误或已过期"), // 新增：验证码错误/过期
        FORBIDDEN_ADMIN("无管理员权限，无法登录后台系统"), // 新增
        APP_TYPE_INVALID("应用类型不合法"), // 可选：新增应用类型校验
        OTHER("其他登录异常");

        // 错误描述信息
        private final String message;

        // 构造器
        LoginError(String message) {
            this.message = message;
        }
    }

    /**
     * 持有具体的登录错误类型
     */
    private final LoginError loginError;

    /**
     * 构造方法1：仅传入错误类型（常规使用）
     */
    public LoginException(LoginError loginError) {
        super(loginError.getMessage());
        this.loginError = loginError;
    }

    /**
     * 构造方法2：传入错误类型 + 补充描述（灵活扩展）
     */
    public LoginException(LoginError loginError, String extraMessage) {
        super(loginError.getMessage() + "：" + extraMessage);
        this.loginError = loginError;
    }

    // ============================== 使用示例（方便参考）==============================
    //
    // 1️⃣ 用户名或密码错误（安全推荐用法）：
    // throw new LoginException(LoginError.INVALID_CREDENTIALS);
    //
    // 2️⃣ 账号被封禁：
    // throw new LoginException(LoginError.ACCOUNT_LOCKED, "封禁原因：多次违规操作");
    //
    // 3️⃣ 输入格式错误（前端参数校验失败）：
    // throw new LoginException(LoginError.INVALID_INPUT, "密码长度不足6位");
    //
    // 4️⃣ 其他未知登录异常（系统错误）：
    // throw new LoginException(LoginError.OTHER, "数据库异常或服务器错误");
    // ================================================================================
}