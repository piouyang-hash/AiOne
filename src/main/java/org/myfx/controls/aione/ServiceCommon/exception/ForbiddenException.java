package org.myfx.controls.aione.ServiceCommon.exception;

/**
 * 权限禁止异常：用户角色不满足接口访问要求时抛出
 */
public class ForbiddenException extends RuntimeException {

    // 带消息的构造（常用）
    public ForbiddenException(String message) {
        super(message);
    }


}
