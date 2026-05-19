package org.myfx.controls.aione.ServiceCommon.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于控制器接口限流，防止恶意重复访问
 * 🔴 重要提醒：仅允许用于Controller类（ElementType.TYPE）或Controller中的接口方法（ElementType.METHOD）
 * 禁止用于Service、Dao、工具类等非控制器组件，否则会导致JWT校验逻辑异常
 */
// 注解可以用在方法上
@Target(ElementType.METHOD)
// 注解在运行时生效（拦截器需要在运行时判断）
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    // 时间窗口（秒），默认60秒
    int seconds() default 60;
    // 最大访问次数，默认20次
    int maxCount() default 20;
}