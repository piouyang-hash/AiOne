package org.myfx.controls.aione.ServiceCommon.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.myfx.controls.aione.ServiceCommon.context.RequestContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.ServletRequestAttributes;

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
        ServletRequestAttributes attributes = (ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        // 2. 🔥 拿到你要的 methodName
        String methodName = null;
        if (joinPoint.getSignature() instanceof org.aspectj.lang.reflect.MethodSignature methodSignature) {
            methodName = methodSignature.getName();
        }

        // 3. 构建上下文
        String ip = RequestContext.getIpAddress(request);
        var headers = RequestContext.getHeaders(request);
        var contextData = new RequestContext.RequestContextData(ip, methodName, headers);

        // 4. ScopedValue绑定，执行目标方法
        return RequestContext.bind(contextData).call(() -> {
            try {
                // 执行Controller方法
                return joinPoint.proceed();
            } catch (Throwable e) {
                log.error("请求执行异常：", e);
                throw new RuntimeException(e);
            }
        });
    }
}