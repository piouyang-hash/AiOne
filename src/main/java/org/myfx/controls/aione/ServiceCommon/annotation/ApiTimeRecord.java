package org.myfx.controls.aione.ServiceCommon.annotation;

import java.lang.annotation.*;

/**
 * 接口响应时间统计注解
 * 加在 方法/接口 上，自动记录执行耗时
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiTimeRecord {

    /**
     * 接口名称（备注，方便日志区分）
     */
    String value() default "";
}