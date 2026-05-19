package org.myfx.controls.aione.ServiceCommon.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.myfx.controls.aione.ServiceCommon.annotation.RateLimit;
import org.myfx.controls.aione.ServiceCommon.context.RequestContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    ) throws Exception
    {
        // 1. 先判断：当前请求的是不是“方法接口”（排除静态资源等）
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true; // 不是方法，直接放行
        }

        // 2. 从RequestContext获取当前请求的方法名（已由RequestContextInterceptor初始化）
        String methodName = RequestContext.getMethodName();
        if (methodName == null) {
            return true; // 极端情况：方法名未初始化，直接放行（避免拦截正常请求）
        }

        // 3. 从HandlerMethod中获取@RateLimit注解（仍需判断是否有注解）
        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);
        if (rateLimit == null) {
            return true; // 没贴注解，放行
        }

        // 4. 从RequestContext获取IP（已兼容代理，无需再用request）
        String ip = RequestContext.getIp();

        // 5. 从注解拿限流参数
        int seconds = rateLimit.seconds();
        int maxCount = rateLimit.maxCount();

        // 6. 生成Redis的key（完全依赖RequestContext的信息）
        String redisKey = "rateLimit:" + ip + ":" + methodName;

        // 7. 计数+1，判断是否超过限制（逻辑不变）
        Long count = stringRedisTemplate.opsForValue().increment(redisKey);
        if (count != null && count == 1) {
            stringRedisTemplate.expire(redisKey, seconds, TimeUnit.SECONDS);
        }

        if (count != null && count > maxCount) {
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("访问太频繁！" + seconds + "秒内最多访问" + maxCount + "次～");
            return false;
        }

        // 8. 没超限制，放行
        System.out.println(ip + "访问" + methodName + "，" + seconds + "秒内第" + count + "次");
        return true;
    }
}