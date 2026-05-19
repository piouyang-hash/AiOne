package org.myfx.controls.aione.UserService.common.exception;

import lombok.Getter;

/**
 * 新旧密码一致异常（专属场景：修改密码时，新密码与数据库中存储的旧密码完全相同）
 * 属于客户端请求错误，错误码固定为400（BAD_REQUEST），符合HTTP规范
 */
@Getter
public class OldNewPasswordSameException extends RuntimeException {
    /**
     * 带自定义消息的构造方法（灵活适配不同提示场景）
     * @param message 自定义异常提示消息
     */
    public OldNewPasswordSameException(String message) {
        super(message);
    }

}
