package org.myfx.controls.aione.UserService.common.exception;

import lombok.Getter;

/**
 * 业务异常：用户名已存在（注册时专用）
 */
@Getter
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}