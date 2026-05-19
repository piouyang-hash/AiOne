package org.myfx.controls.aione.ServiceCommon.exception;

import lombok.Getter;

/**
 * 身份认证异常：专门处理 Token 过期、签名无效等认证相关错误
 */
@Getter
public class AuthException extends RuntimeException {

    // 认证错误枚举：包含所有认证相关的错误类型
    @Getter
    public enum AuthError {
        TOKEN_EXPIRED("ServiceCommon抛出的身份校验错误：Token已过期（超过设定的有效期，需重新获取令牌）"),
        INVALID_SIGNATURE("ServiceCommon抛出的身份校验错误：签名无效（Token可能被篡改，或签名密钥不匹配）"),
        MALFORMED_TOKEN("ServiceCommon抛出的身份校验错误：Token格式错误（不符合JWT标准结构，如缺少.分隔符、字符序列异常）"),
        MISSING_AUTH_HEADER("ServiceCommon抛出的身份校验错误：缺失认证请求头或格式无法识别（需按规范携带Authorization: Bearer <token>）"),
        TOKEN_REVOKED("ServiceCommon抛出的身份校验错误：Token已失效（已主动退出登录或被管理员强制吊销，需重新登录）"),
        REQUIRE_ACCESS_TOKEN("ServiceCommon抛出的身份校验错误：仅允许访问令牌(AccessToken)访问业务接口，刷新令牌(RefreshToken)无权调用"),
        OTHER("ServiceCommon抛出的身份校验错误：其他认证异常（未明确分类的JWT校验问题，建议检查令牌完整性和有效性）");

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