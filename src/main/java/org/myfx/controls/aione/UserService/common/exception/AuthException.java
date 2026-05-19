package org.myfx.controls.aione.UserService.common.exception;

import lombok.Getter;

/**
 * 身份认证异常：专门处理 Token 过期、签名无效等认证相关错误
 */
@Getter
public class AuthException extends RuntimeException {

    // 认证错误枚举：包含所有认证相关的错误类型
    @Getter
    public enum AuthError {
        TOKEN_EXPIRED("Token已过期"),
        INVALID_SIGNATURE("签名无效"),
        MALFORMED_TOKEN("格式错误"),
        MISSING_AUTH_HEADER("缺失认证请求头或格式无法识别"),
        TOKEN_REVOKED("Token已失效（已退出登录）"),
        OTHER("其他认证异常");

        // 获取错误描述
        // 错误描述信息
        private final String message;

        // 枚举构造器（默认私有，无需显式写private）
        AuthError(String message) {
            this.message = message;
        }

    }

    //  getter：方便后续获取具体错误类型（比如Controller返回时用）
    // 持有具体的认证错误类型（枚举实例）
    private final AuthError authError;

    // 构造方法1：直接传入认证错误类型（最常用）
    public AuthException(AuthError authError) {
        super(authError.getMessage()); // 异常消息直接用枚举的描述
        this.authError = authError;
    }

    // 构造方法2：传入认证错误类型 + 自定义补充消息（灵活扩展）
    public AuthException(AuthError authError, String extraMessage) {
        super(authError.getMessage() + "：" + extraMessage);
        this.authError = authError;
    }

    // ============================== 使用示例（方便后续参考）==============================
    // 1. Token已过期时抛出
    // throw new AuthException(AuthException.AuthError.TOKEN_EXPIRED);
    //
    // 2. 签名无效（需补充额外说明时）
    // throw new AuthException(AuthException.AuthError.INVALID_SIGNATURE, "签名与服务器密钥不匹配");
    //
    // 3. Token格式错误（比如不是JWT格式、缺少必要字段）
    // throw new AuthException(AuthException.AuthError.MALFORMED_TOKEN);
    //
    // 4. 前端未传Authorization请求头，或请求头格式错误（比如没带Bearer前缀）
    // throw new AuthException(AuthException.AuthError.MISSING_AUTH_HEADER);
    //
    // 5. 其他无法归类的认证异常
    // throw new AuthException(AuthException.AuthError.OTHER, "未知认证错误，请联系管理员");
    // ===================================================================================

}