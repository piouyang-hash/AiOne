package org.myfx.controls.aione.ServiceCommon.context;

import java.util.Date;

/**
 * JWT过期时间上下文 - ScopedValue 版
 */
public class JwtExpireTimeContext {

    // 公开ScopedValue，用于AOP链式绑定（最简方案，不报错）
    public static final ScopedValue<Date> CURRENT_EXPIRE_DATE = ScopedValue.newInstance();

    // 兼容旧代码：业务代码不用改
    public static Date getExpireDate() {
        return CURRENT_EXPIRE_DATE.isBound() ? CURRENT_EXPIRE_DATE.get() : null;
    }
}