package org.myfx.controls.aione.ServiceCommon.annotation;

import org.myfx.controls.aione.ServiceCommon.serviceEnum.JwtTokenType;
import java.lang.annotation.*;

/**
 * 用于标记需要检验JWT令牌的方法
 * 🔴 重要提醒：仅允许用于Controller类（ElementType.TYPE）或Controller中的接口方法（ElementType.METHOD）
 * 禁止用于Service、Dao、工具类等非控制器组件，否则会导致JWT校验逻辑异常
 */
// 注解在运行时生效（AOP需要在运行时扫描并拦截）
@Retention(RetentionPolicy.RUNTIME)
// 注解可用在Controller类或Controller的方法上（仅控制器相关，非控制器组件禁止使用）
@Target({ElementType.METHOD, ElementType.TYPE})
// 允许在生成Javadoc时显示该注解（包含重要提醒，便于团队查看）
@Documented
public @interface CheckJwt {

    /**
     * 指定需要校验的Token类型 → 默认校验 ACCESS 令牌
     */
    JwtTokenType tokenType() default JwtTokenType.ACCESS;

}