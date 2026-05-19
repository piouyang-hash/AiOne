package org.myfx.controls.aione.UserService.common.exception;

/**
 * 用户无头像异常：当用户没有头像文件时抛出
 */
public class UserHasNoAvatarException extends RuntimeException {

    // 构造方法：接收用户ID，生成具体错误信息
    public UserHasNoAvatarException(Integer userId) {
        super("用户ID[" + userId + "]暂无头像文件，无法执行相关操作～");
    }
}
