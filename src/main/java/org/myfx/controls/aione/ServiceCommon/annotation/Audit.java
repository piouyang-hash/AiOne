package org.myfx.controls.aione.ServiceCommon.annotation;

import java.lang.annotation.*;

/**
 * 审计注解
 * 只允许放在【管理员专用接口/类】上，目的是记录管理员执行了什么操作
 * 查询操作不需要此切面
 * 支持：类上（全部方法生效）、方法上（单个方法生效）
 */
@Target({ElementType.METHOD, ElementType.TYPE}) // 支持方法 + 类
@Retention(RetentionPolicy.RUNTIME)
public @interface Audit {
}