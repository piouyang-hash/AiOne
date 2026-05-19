package org.myfx.controls.aione.ServiceCommon.annotation;

import org.myfx.controls.aione.ServiceCommon.serviceEnum.RoleEnum;

import java.lang.annotation.*;

/**
 * 角色权限校验注解（改为枚举数组，类型安全）
 */
@Target({ElementType.METHOD, ElementType.TYPE}) // 可加在类/方法上
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {
    /** 允许访问的角色列表（直接填RoleEnum枚举，如{RoleEnum.ADMIN}） */
    RoleEnum[] allowedRoles() default {};
}