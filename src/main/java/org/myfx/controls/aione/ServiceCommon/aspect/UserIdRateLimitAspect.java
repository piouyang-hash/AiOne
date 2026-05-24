package org.myfx.controls.aione.ServiceCommon.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.myfx.controls.aione.ServiceCommon.annotation.UserIdRateLimit;
import org.myfx.controls.aione.ServiceCommon.context.RequestContext;
import org.myfx.controls.aione.ServiceCommon.context.UserContext;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 用户ID限流切面
 * 核心：登录用户用 userId 精准限流 | 未登录用户用 IP 兜底
 * 执行顺序：在 JwtCheckAspect(Order=1) 之后执行
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@Order(3)
public class UserIdRateLimitAspect {

    private final StringRedisTemplate stringRedisTemplate;

    // ==================== 🔥 最终修改版：切点匹配【专属用户ID限流注解】 ====================
    @Pointcut("@annotation(org.myfx.controls.aione.ServiceCommon.annotation.UserIdRateLimit)")
    public void userIdRateLimitPointcut() {
    }

    // ==================== 环绕通知：核心限流逻辑（完全不变） ====================
    @Around("userIdRateLimitPointcut()")
    public Object rateLimitAround(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 获取目标方法 + 限流注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        // 替换为新注解
        UserIdRateLimit rateLimit = method.getAnnotation(UserIdRateLimit.class);

        // 2. 获取注解参数
        int seconds = rateLimit.seconds();
        int maxCount = rateLimit.maxCount();

        // 3. 核心：获取限流标识（userId/IP）
        String limitKey = getRateLimitKey();

        // 4. 接口方法名
        String methodName = RequestContext.getMethodName();

        // 5. Redis Key
        String redisKey = STR."rateLimit:user:\{limitKey}:\{methodName}";

        // 6. Redis计数+限流判断
        try {
            Long count = stringRedisTemplate.opsForValue().increment(redisKey);
            if (count != null && count == 1) {
                stringRedisTemplate.expire(redisKey, seconds, TimeUnit.SECONDS);
            }
            if (count != null && count > maxCount) {
                log.warn("用户ID限流触发！标识：{}，接口：{}，限制：{}秒内最多{}次",
                        limitKey, methodName, seconds, maxCount);
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                        STR."访问频繁！\{seconds}秒内最多访问\{maxCount}次");
            }
        } catch (Exception e) {
            log.error("用户ID限流Redis异常，自动放行", e);
        }

        return joinPoint.proceed();
    }

    // ==================== 工具方法：获取限流标识 ====================
    private String getRateLimitKey() {
        Integer userId = UserContext.getUserId();
        return Optional.ofNullable(userId)
                .map(String::valueOf)
                .orElseGet(RequestContext::getIp);
    }
}