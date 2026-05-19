package org.myfx.controls.aione.ServiceCommon.exception;

/**
 * IP限流异常：此IP被限流，禁止访问接口
 */
public class IpRateLimitException extends RuntimeException {

    public IpRateLimitException(String message) {
        super(message);
    }
}