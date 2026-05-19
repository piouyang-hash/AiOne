package org.myfx.controls.aione.AiService.common.exception;

import lombok.Getter;

/**
 * Token余额不足异常
 * 属于客户端请求错误，错误码固定为400（BAD_REQUEST），符合HTTP规范
 */
@Getter
public class TokenInsufficientException extends RuntimeException {

    /**
     * 带自定义消息的构造方法
     * @param message 异常提示消息
     */
    public TokenInsufficientException(String message) {
        super(message);
    }

}