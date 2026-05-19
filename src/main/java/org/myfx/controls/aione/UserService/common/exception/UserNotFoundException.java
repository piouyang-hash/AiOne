package org.myfx.controls.aione.UserService.common.exception;

/**
 * 用户不存在异常：当查询的用户不存在时抛出
 */
public class UserNotFoundException extends RuntimeException {

    // 无参构造，固定异常信息为“用户不存在”
    public UserNotFoundException() {
        super("用户不存在");
    }
}