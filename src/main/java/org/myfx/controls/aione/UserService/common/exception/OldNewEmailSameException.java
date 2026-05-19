package org.myfx.controls.aione.UserService.common.exception;

/**
 * 自定义异常：新邮箱与旧邮箱一致时抛出
 */
public class OldNewEmailSameException extends RuntimeException {

    // 带消息的构造方法（与OldNewPasswordSameException风格一致）
    public OldNewEmailSameException(String message) {
        super(message);
    }
}