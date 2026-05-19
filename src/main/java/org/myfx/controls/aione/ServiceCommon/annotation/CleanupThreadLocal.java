package org.myfx.controls.aione.ServiceCommon.annotation;

import java.lang.annotation.*;

/**
 * 标记需要清理ThreadLocal的类（该类下所有方法执行后都会触发清理）
 */
@Target(ElementType.TYPE) // 改为支持类注解
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CleanupThreadLocal {
}