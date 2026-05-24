package org.myfx.controls.aione.ServiceCommon.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.myfx.controls.aione.ServiceCommon.annotation.ApiTimeRecord;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 接口响应时间统计切面
 * 最高优先级，统计全链路完整耗时
 */
@Slf4j
@Aspect
@Component
// 最高优先级：最先执行，最后退出，囊括所有其他切面的耗时
@Order(0)
public class ApiTimeRecordAspect {

    /**
     * 切入点：匹配所有加了 @ApiTimeRecord 注解的方法
     */
    @Pointcut("(@within(org.myfx.controls.aione.ServiceCommon.annotation.ApiTimeRecord) || @annotation(org.myfx.controls.aione.ServiceCommon.annotation.ApiTimeRecord))")
    public void timeRecordPointCut() {
    }

    /**
     * 环绕通知：统计方法执行耗时
     */
    @Around("timeRecordPointCut()")
    public Object recordApiTime(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Class<?> targetClass = joinPoint.getTarget().getClass();

        // 优先取方法上的注解，没有则取类上的注解
        ApiTimeRecord annotation = method.getAnnotation(ApiTimeRecord.class);
        if (annotation == null) {
            annotation = targetClass.getAnnotation(ApiTimeRecord.class);
        }

        long start = System.currentTimeMillis();
        String methodName = targetClass.getName() + "." + method.getName();
        String apiName = annotation == null ? "未命名" : annotation.value();

        try {
            return joinPoint.proceed();
        } finally {
            long cost = System.currentTimeMillis() - start;
            log.info("【接口耗时统计】接口名：{} | 方法：{} | 总耗时：{} ms",
                    apiName.isBlank() ? "未命名" : apiName,
                    methodName,
                    cost);
        }
    }

}