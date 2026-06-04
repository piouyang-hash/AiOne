package org.myfx.controls.aione.ServiceCommon.config;

import lombok.RequiredArgsConstructor;
import org.myfx.controls.aione.ServiceCommon.interceptor.RateLimitInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor; // 注入注入限流拦截器

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 2. 再注册RateLimitInterceptor（依赖上下文信息，后执行）
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/**") // 拦截所有接口（和上下文拦截器保持一致）
                .order(1); // 后于上下文拦截器执行，保证能拿到初始化的IP和方法名
    }
}