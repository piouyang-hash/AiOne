package org.myfx.controls.aione.ServiceCommon.context;

import java.util.Date;

/**
 * JWT上下文工具类：用ThreadLocal存储当前线程的JWT过期时间
 * ThreadLocal特点：线程私有，多线程间数据隔离，避免过期时间在方法间冗余传递
 */
public class JwtExpireTimeContext {

    // 定义ThreadLocal变量：仅存储当前JWT的过期时间（Date类型）
    private static final ThreadLocal<Date> CURRENT_EXPIRE_DATE = new ThreadLocal<>();

    /**
     * 设置当前线程的JWT过期时间
     * @param expireDate JWT的过期时间（如 new Date(1733251200000L) 表示2024-11-04 00:00:00）
     */
    public static void setExpireDate(Date expireDate) {
        CURRENT_EXPIRE_DATE.set(expireDate);
    }

    /**
     * 获取当前线程的JWT过期时间
     * @return JWT过期时间（未设置则返回null）
     */
    public static Date getExpireDate() {
        return CURRENT_EXPIRE_DATE.get();
    }

    /**
     * 清除当前线程的JWT过期时间（必须调用，避免内存泄漏）
     * 原因：ThreadLocal的value为强引用，线程池复用线程时会残留旧过期时间，且可能导致内存泄漏
     */
    public static void clear() {
//        // 清除前打印当前过期时间（方便调试）
//        Date beforeClearDate = CURRENT_EXPIRE_DATE.get();
//        System.out.println("===== JWT过期时间 ThreadLocal 清除前 =====");
//        System.out.println("当前存储的过期时间：" + (beforeClearDate != null ? beforeClearDate : "无"));
//        System.out.println("========================================");

        // 执行清除
        CURRENT_EXPIRE_DATE.remove();
    }
}
