package org.myfx.controls.aione.UserService.common.annotation;

import java.lang.annotation.*;

/**
 * 邮箱验证码校验注解（用于切面，标注在方法上）
 */
@Target({ElementType.METHOD}) // 改为仅适用于方法
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidEmailCode {

    /**
     * 是否开启检验（默认开启）
     */
    boolean validate() default true;
}