package org.myfx.controls.aione.ServiceCommon.annotation;

import java.lang.annotation.*;

// 注解可以用在类和方法上
@Target({ElementType.TYPE, ElementType.METHOD})
// 注解保留到运行时（AOP需要在运行时解析）
@Retention(RetentionPolicy.RUNTIME)
// 生成javadoc时包含该注解
@Documented
public @interface ServiceAuth {

    // 移除默认值，必须在使用时显式指定允许的服务名
    String[] allowedServices();
}