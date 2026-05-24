package org.myfx.controls.aione.ServiceCommon.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.myfx.controls.aione.ServiceCommon.annotation.RequireRole;
import org.myfx.controls.aione.ServiceCommon.context.UserContext;
import org.myfx.controls.aione.ServiceCommon.exception.ForbiddenException;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.RoleEnum;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
@Aspect
@Component
@Order(5)
public class RoleAuthAspect {

    // 切入点：拦截所有标记了@RequireRole的类或方法（逻辑不变）
    @Pointcut("@annotation(org.myfx.controls.aione.ServiceCommon.annotation.RequireRole) || @within(org.myfx.controls.aione.ServiceCommon.annotation.RequireRole)")
    public void roleAuthPointcut() {
    }

    // 前置通知：统一处理类/方法注解，优先方法注解
    @Before("roleAuthPointcut()")
    public void doBefore(JoinPoint joinPoint) {
        // 1. 获取方法上的注解（优先级最高）
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequireRole methodAnnotation = AnnotationUtils.findAnnotation(method, RequireRole.class);

        // 2. 获取类上的注解（方法无注解时用类注解）
        RequireRole classAnnotation = AnnotationUtils.findAnnotation(joinPoint.getTarget().getClass(), RequireRole.class);

        // 3. 确定最终生效的注解
        RequireRole requireRole = Objects.nonNull(methodAnnotation) ? methodAnnotation : classAnnotation;
        if (Objects.isNull(requireRole)) {
            return;
        }

        // 4. 纯枚举对比的角色校验
        checkRoleWithEnum(joinPoint, requireRole);
    }

    // 核心：纯枚举对比的校验逻辑（无任何字符串转换）
    private void checkRoleWithEnum(JoinPoint joinPoint, RequireRole requireRole) {
        // 1. 从上下文获取当前用户的RoleEnum枚举（原生对象，无字符串）
        RoleEnum currentRole = UserContext.getRole();
        String methodName = joinPoint.getSignature().toShortString();

        // 2. 获取注解中配置的允许角色枚举列表
        RoleEnum[] allowedRoles = requireRole.allowedRoles();
        // 空列表校验：注解未配置角色直接抛异常
        if (allowedRoles == null || allowedRoles.length == 0) {
            log.error("权限校验失败[{}]：@RequireRole注解的allowedRoles不能为空（需配置RoleEnum枚举）", methodName);
            throw new ForbiddenException("权限配置错误：接口未配置允许访问的角色");
        }
        List<RoleEnum> allowedRoleList = Arrays.asList(allowedRoles);

        // 3. 枚举原生对比（核心优化点）
        // 场景1：当前角色为空（未登录/未设置）
        if (Objects.isNull(currentRole)) {
            log.error("权限校验失败[{}]：当前用户未分配角色，允许的角色：{}", methodName, allowedRoleList);
            throw new ForbiddenException("权限不足：当前用户未分配角色，无法访问该接口");
        }

        // 场景2：当前角色不在允许列表中（直接用枚举contains对比）
        if (!allowedRoleList.contains(currentRole)) {
            log.error("权限校验失败[{}]：当前角色「{}({})」，允许的角色：{}",
                    methodName, currentRole.name(), currentRole.getDesc(), allowedRoleList);
            throw new ForbiddenException(
                    String.format("权限不足：该接口仅允许角色「%s」访问，当前角色：%s（%s）",
                            getRoleDescList(allowedRoleList), // 转成友好的描述（如"管理员、用户"）
                            currentRole.name(), currentRole.getDesc())
            );
        }

        // 场景3：校验通过
        log.debug("权限校验通过[{}]：当前角色「{}({})」，允许的角色：{}",
                methodName, currentRole.name(), currentRole.getDesc(), allowedRoleList);
    }

    // 辅助方法：把RoleEnum列表转成友好的描述字符串（如[ADMIN, USER] → "管理员、用户"）
    private String getRoleDescList(List<RoleEnum> roleList) {
        return roleList.stream()
                .map(RoleEnum::getDesc)
                .reduce((a, b) -> STR."\{a}、\{b}")
                .orElse("");
    }
}