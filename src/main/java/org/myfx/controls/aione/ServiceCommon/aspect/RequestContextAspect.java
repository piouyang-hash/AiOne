package org.myfx.controls.aione.ServiceCommon.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.myfx.controls.aione.ServiceCommon.context.RequestContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

@Slf4j
@Aspect
@Component
@Order(10) // 🔥 最高优先级！比你的JWT切面更早执行！
public class RequestContextAspect {

    // 匹配所有Controller接口
    // 精准匹配：4个核心控制器包 + 所有子包下的所有接口方法
    @Pointcut("execution(public * org.myfx.controls.aione.Demo.controller..*(..)) || "
            + "execution(public * org.myfx.controls.aione.AiService.controller..*(..)) || "
            + "execution(public * org.myfx.controls.aione.SimulationGame.controller..*(..)) || "
            + "execution(public * org.myfx.controls.aione.UserService.controller..*(..))")
    public void controllerPointcut() {}


    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) {
        // 1. 获取请求对象
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = null;

        if (attributes != null) {
            request = attributes.getRequest();
        } else {
            log.warn("[RequestContextAspect] ServletRequestAttributes is NULL - 无法获取HttpServletRequest，上下文初始化受限");
        }

        // 🔥 方案1：显式声明为final（核心修复）
        final String methodName;  // 改为final
        if (joinPoint.getSignature() instanceof MethodSignature methodSignature) {
            methodName = methodSignature.getName();
        } else {
            methodName = null;  // 必须初始化，final变量需明确赋值
            log.warn("[RequestContextAspect] JoinPoint signature is NOT MethodSignature (type: {}) - 无法获取目标方法名",
                    joinPoint.getSignature().getClass().getSimpleName());
        }

        // 3. 构建上下文数据
        String ip = null;
        Map<String, String> headers = null;

        if (request != null) {
            ip = RequestContext.getIpAddress(request);
            headers = RequestContext.getHeaders(request);
        } else {
            log.warn("[RequestContextAspect] HttpServletRequest is NULL - 无法获取IP地址和请求头，上下文数据将缺失");
        }

        var contextData = new RequestContext.RequestContextData(ip, methodName, headers);

        // 4. ScopedValue绑定并执行目标方法
        return RequestContext.bind(contextData).call(() -> {
            try {
                return joinPoint.proceed();
            } catch (Throwable e) {
                // 现在可以安全使用final变量methodName
                log.error("[RequestContextAspect] 请求执行异常（目标方法：{}）", methodName, e);
                throw new RuntimeException(e);
            }
        });
    }
}