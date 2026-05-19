package org.myfx.controls.aione.ServiceCommon.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.myfx.controls.aione.ServiceCommon.annotation.ServiceAuth;
import org.myfx.controls.aione.ServiceCommon.context.RequestContext;
import org.myfx.controls.aione.ServiceCommon.exception.ServiceOnlyAccessException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Aspect  // 标记为切面
@Component  // 交给Spring管理
@Order(3)  // 切面执行顺序（值越小越先执行，这里按你的要求设为3）
public class ServiceAuthAspect {

    // 切入点：拦截所有标记了@ServiceAuth的类或方法
    @Pointcut("@annotation(org.myfx.controls.aione.ServiceCommon.annotation.ServiceAuth) || @within(org.myfx.controls.aione.ServiceCommon.annotation.ServiceAuth)")
    public void serviceAuthPointcut() {
    }

    // 前置通知：在目标方法执行前进行校验
    @Before("serviceAuthPointcut() && @annotation(serviceAuth)")
    public void doBeforeMethod(ServiceAuth serviceAuth) {
        checkAuth(serviceAuth); // 共用校验逻辑
    }

    @Before("serviceAuthPointcut() && @within(serviceAuth)")
    public void doBeforeClass(ServiceAuth serviceAuth) {
        checkAuth(serviceAuth); // 共用校验逻辑
    }

    // 抽取共用的校验逻辑
    private void checkAuth(ServiceAuth serviceAuth) {
        String callerService = RequestContext.getHeader("x-service-name");
        List<String> allowedList = Arrays.asList(serviceAuth.allowedServices());

        if (callerService == null || !allowedList.contains(callerService)) {
            // 抛出自定义异常，明确提示“仅限微服务调用”及允许的服务列表
            throw new ServiceOnlyAccessException(
                    "权限不足：该接口仅允许指定微服务调用（允许的服务：" + allowedList + "），请通过微服务网关调用"
            );
        }
    }
}