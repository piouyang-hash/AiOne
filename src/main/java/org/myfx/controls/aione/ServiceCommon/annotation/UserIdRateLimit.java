package org.myfx.controls.aione.ServiceCommon.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 【用户ID专属限流注解】
 * 作用：对登录用户使用 userId 精准限流，未登录用户自动降级为 IP 限流
 * 🔴 仅允许用于 Controller 接口方法，禁止在 Service/Dao 中使用
 * 执行顺序：在 JWT 校验切面之后执行，确保能获取到用户ID
 */
@Target(ElementType.METHOD)       // 仅作用于接口方法
@Retention(RetentionPolicy.RUNTIME) // 运行时生效
public @interface UserIdRateLimit {

    // 限流时间窗口（秒），默认60秒
    int seconds() default 60;

    // 时间窗口内最大允许访问次数，默认20次
    int maxCount() default 20;
}