package org.myfx.controls.aione.ServiceCommon.aspect;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import org.aspectj.lang.reflect.MethodSignature;
import org.myfx.controls.aione.ServiceCommon.annotation.CheckJwt;
import org.myfx.controls.aione.ServiceCommon.context.JwtExpireTimeContext;
import org.myfx.controls.aione.ServiceCommon.context.RequestContext;
import org.myfx.controls.aione.ServiceCommon.context.UserContext;
import org.myfx.controls.aione.ServiceCommon.exception.AuthException;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.JwtTokenType;
import org.myfx.controls.aione.ServiceCommon.utils.JwtTokenUtil;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Date;

/**
 * 简化版JWT检验切面：仅获取请求头中的令牌，解析用户ID并打印
 */
// 改造之前的JwtAspect，添加ThreadLocal的设置和清理
@Aspect
@Component
@RequiredArgsConstructor
@Order(1)
public class JwtCheckAspect {

    private final JwtTokenUtil jwtTokenUtil;
    private final StringRedisTemplate stringRedisTemplate;

    @Pointcut("@within(org.myfx.controls.aione.ServiceCommon.annotation.CheckJwt) || @annotation(org.myfx.controls.aione.ServiceCommon.annotation.CheckJwt)")
    public void jwtPointcut() {
    }

    @Around("jwtPointcut()")
    public Object doCheckJwt(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            // 1. 获取请求头Token
            String token = RequestContext.getToken();
            if (token.isBlank()) {
                throw new AuthException(AuthException.AuthError.MISSING_AUTH_HEADER);
            }

            // 2. 获取当前接口注解要求的令牌类型
            CheckJwt checkJwt = getCheckJwtAnnotation(joinPoint);
            JwtTokenType requireTokenType = checkJwt.tokenType();

            // 3. 黑名单通用校验
            String blacklistKey = "jwt:blacklist:" + token;
            Boolean isBlacklisted = stringRedisTemplate.hasKey(blacklistKey);
            if (Boolean.TRUE.equals(isBlacklisted)) {
                throw new AuthException(AuthException.AuthError.TOKEN_REVOKED);
            }

            // 4. 【核心】调用工具类：自动根据类型选密钥+解析+抛异常
            Claims claims = jwtTokenUtil.extractClaimsByTokenType(token, requireTokenType);

            // 5. 强校验token_type 文本一致
            String actualTokenType = claims.get("token_type", String.class);
            String needTokenTypeVal = requireTokenType.getType();
            if (!needTokenTypeVal.equals(actualTokenType)) {
                throw new AuthException(AuthException.AuthError.REQUIRE_ACCESS_TOKEN);
            }

            // 6. 解析信息塞入上下文
            Date expireDate = claims.getExpiration();
            JwtExpireTimeContext.setExpireDate(expireDate);

            Integer userId = claims.get("id", Integer.class);
            String role = claims.get("role", String.class);
            String appType = claims.get("appType", String.class);

            UserContext.setUserId(userId);
            UserContext.setRole(role);
            UserContext.setAppType(appType);

            // 放行
            return joinPoint.proceed();
        } finally {
            // 强制清理ThreadLocal
            UserContext.clear();
            JwtExpireTimeContext.clear();
            RequestContext.clear();
        }
    }

    /**
     * 工具方法：获取 方法/类 上的 @CheckJwt 注解
     */
    private CheckJwt getCheckJwtAnnotation(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        CheckJwt checkJwt = method.getAnnotation(CheckJwt.class);
        if (checkJwt == null) {
            checkJwt = joinPoint.getTarget().getClass().getAnnotation(CheckJwt.class);
        }
        return checkJwt;
    }
}