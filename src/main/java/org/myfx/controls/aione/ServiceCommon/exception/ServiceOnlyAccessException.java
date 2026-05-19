package org.myfx.controls.aione.ServiceCommon.exception;

/**
 * 仅限微服务访问异常（用于接口不允许前端直接调用，仅允许其他微服务通过请求头验证后调用的场景）
 */
public class ServiceOnlyAccessException extends RuntimeException {

    // 带错误消息的构造（核心，用于传递具体限制信息）
    public ServiceOnlyAccessException(String message) {
        super(message);
    }
}