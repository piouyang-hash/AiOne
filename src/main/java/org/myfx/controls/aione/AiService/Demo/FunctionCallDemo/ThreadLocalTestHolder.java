package org.myfx.controls.aione.AiService.Demo.FunctionCallDemo;

import org.springframework.stereotype.Component;

/**
 * ThreadLocal 工具类：存储 Integer 类型的测试值
 */
@Component
public class ThreadLocalTestHolder {
    // 定义 ThreadLocal 实例，存储 Integer 类型
    private static final ThreadLocal<Integer> THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 设置线程本地变量值
     * @param value 要存储的 Integer 值
     */
    public static void set(Integer value) {
        THREAD_LOCAL.set(value);
    }

    /**
     * 获取线程本地变量值
     * @return 线程内存储的 Integer 值（无值返回 null）
     */
    public static Integer get() {
        return THREAD_LOCAL.get();
    }

    /**
     * 删除（清空）线程本地变量
     * 【关键】使用完必须调用，避免内存泄漏
     */
    public static void delete() {
        THREAD_LOCAL.remove();
    }
}