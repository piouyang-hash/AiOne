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
@Order(30)
public class JwtCheckAspect {

    private final JwtTokenUtil jwtTokenUtil;
    private final StringRedisTemplate stringRedisTemplate;

    @Pointcut("@within(org.myfx.controls.aione.ServiceCommon.annotation.CheckJwt) || @annotation(org.myfx.controls.aione.ServiceCommon.annotation.CheckJwt)")
    public void jwtPointcut() {
    }

    @Around("jwtPointcut()")
    public Object doCheckJwt(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 获取请求头Token
        String token = RequestContext.getToken();
        if (token.isBlank()) {
            throw new AuthException(AuthException.AuthError.MISSING_AUTH_HEADER);
        }

        // 2. 获取接口注解
        CheckJwt checkJwt = getCheckJwtAnnotation(joinPoint);
        JwtTokenType requireTokenType = checkJwt.tokenType();

        // 3. 黑名单校验
        String blacklistKey = "jwt:blacklist:" + token;
        Boolean isBlacklisted = stringRedisTemplate.hasKey(blacklistKey);
        if (Boolean.TRUE.equals(isBlacklisted)) {
            throw new AuthException(AuthException.AuthError.TOKEN_REVOKED);
        }

        // 4. 解析Token
        Claims claims = jwtTokenUtil.extractClaimsByTokenType(token, requireTokenType);

        // 5. 校验token类型
        String actualTokenType = claims.get("token_type", String.class);
        String needTokenTypeVal = requireTokenType.getType();
        if (!needTokenTypeVal.equals(actualTokenType)) {
            throw new AuthException(AuthException.AuthError.REQUIRE_ACCESS_TOKEN);
        }

        // 解析数据
        Date expireDate = claims.getExpiration();
        Integer userId = claims.get("id", Integer.class);
        String roleStr = claims.get("role", String.class);
        String appTypeStr = claims.get("appType", String.class);

        UserContext.UserContextData contextData = new UserContext.UserContextData(
                userId,
                UserContext.validateRole(roleStr),
                UserContext.validateAppType(appTypeStr)
        );

        // ==================== 修复版：标准链式绑定（永远不报错） ====================
        return ScopedValue
                // 第一个绑定：用户上下文
                .where(UserContext.CONTEXT, contextData)
                // 第二个绑定：过期时间（直接传 键+值，类型完全匹配！）
                .where(JwtExpireTimeContext.CURRENT_EXPIRE_DATE, expireDate)
                // 执行目标方法
                .call(joinPoint::proceed);

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