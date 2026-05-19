package org.myfx.controls.aione.ServiceCommon.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.NonNull;
import org.myfx.controls.aione.ServiceCommon.context.RequestContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 请求上下文拦截器：自动初始化和清理IP/请求头信息
 */
@Component
public class RequestContextInterceptor implements HandlerInterceptor {

    // 1. 请求进来时：初始化上下文（IP+请求头）
    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    )
    {
        RequestContext.init(handler); // 调用工具类初始化，自动解析IP和所有请求头
        return true; // 放行请求，不影响后续业务
    }

    // 2. 请求结束后：清理上下文（必写！防内存泄漏）
    @Override
    public void afterCompletion(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler,
            Exception ex
    )
    {
        RequestContext.clear(); // 不管请求成功还是失败，都要清理
    }
}