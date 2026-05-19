package org.myfx.controls.aione.ServiceCommon.aspect;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.myfx.controls.aione.ServiceCommon.context.JwtExpireTimeContext;
import org.myfx.controls.aione.ServiceCommon.context.RequestContext;
import org.myfx.controls.aione.ServiceCommon.context.UserContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Order(0) // 最先执行的切面
public class ThreadLocalCleanupAspect {

    /**
     * 切入点：拦截所有被@CleanupThreadLocal注解标记的类中的所有方法
     */
    @Pointcut("@within(org.myfx.controls.aione.ServiceCommon.annotation.CleanupThreadLocal)")
    public void cleanupThreadLocalAnnotatedClasses() {
    }

    /**
     * 在Controller方法执行后清理ThreadLocal
     */
    @After("cleanupThreadLocalAnnotatedClasses()")
    public void cleanupThreadLocal() {

        // 清理所有ThreadLocal上下文
        UserContext.clear();
        JwtExpireTimeContext.clear();
        RequestContext.clear();
    }
}