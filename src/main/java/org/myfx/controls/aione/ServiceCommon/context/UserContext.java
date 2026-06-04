package org.myfx.controls.aione.ServiceCommon.context;

import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.AppTypeEnum;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.RoleEnum;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;

/**
 * 用户上下文工具类：Java21+ ScopedValue 实现
 * 替代 ThreadLocal，无内存泄漏、自动清理、线程安全（平台线程/虚拟线程通用）
 */
@Slf4j
public final class UserContext {

    // ==================== 上下文数据载体（不可变，推荐用于ScopedValue） ====================
    public record UserContextData(
            Integer userId,
            RoleEnum role,
            AppTypeEnum appType
    ) {}

    // ==================== ScopedValue 核心定义 ====================
    public static final ScopedValue<UserContextData> CONTEXT = ScopedValue.newInstance();

    // 私有构造，禁止实例化
    private UserContext() {}

    // ==================== 原有方法 100% 兼容（调用方无需改代码） ====================

    /**
     * 获取当前用户ID（兼容旧方法）
     */
    public static Integer getUserId() {
        return getValue(UserContextData::userId);
    }

    /**
     * 获取当前用户角色（兼容旧方法）
     */
    public static RoleEnum getRole() {
        return getValue(UserContextData::role);
    }

    /**
     * 获取当前应用类型（兼容旧方法）
     */
    public static AppTypeEnum getAppType() {
        return getValue(UserContextData::appType);
    }

    // 通用取值方法，简化代码
    private static <T> T getValue(java.util.function.Function<UserContextData, T> mapper) {
        if (!CONTEXT.isBound()) {
            return null;
        }
        try {
            return mapper.apply(CONTEXT.get());
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    // ==================== ScopedValue 专用：上下文绑定（核心入口，已修正！） ====================

    /**
     * 绑定上下文（带返回值，兼容Callable）
     */
    public static <T> T callWithContext(UserContextData data, Callable<T> action) throws Exception {
        // ✅ 正确API：where + call
        return ScopedValue.where(CONTEXT, data).call(action::call);
    }

    /**
     * 绑定上下文（无返回值，兼容Runnable）
     */
    public static void runWithContext(UserContextData data, Runnable action) {
        // ✅ 正确API：where + run
        ScopedValue.where(CONTEXT, data).run(action);
    }

    // ==================== 校验逻辑（完全保留你的原有逻辑） ====================

    public static RoleEnum validateRole(String roleName) {
        if (roleName == null || roleName.trim().isEmpty()) {
            String errorMsg = "用户角色枚举name不能为空或空字符串";
            log.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        String targetName = roleName.trim();
        for (RoleEnum role : RoleEnum.values()) {
            if (role.name().equals(targetName)) {
                return role;
            }
        }

        String errorMsg = String.format("无效的用户角色枚举name：%s，合法值为：%s",
                targetName, Arrays.toString(RoleEnum.values()));
        log.error(errorMsg);
        throw new IllegalArgumentException(errorMsg);
    }

    public static AppTypeEnum validateAppType(String appTypeName) {
        if (appTypeName == null || appTypeName.trim().isEmpty()) {
            String errorMsg = "应用类型枚举name不能为空或空字符串";
            log.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        String targetName = appTypeName.trim();
        for (AppTypeEnum appType : AppTypeEnum.values()) {
            if (appType.name().equals(targetName)) {
                return appType;
            }
        }

        String errorMsg = String.format("无效的应用类型枚举name：%s，合法值为：%s",
                targetName, Arrays.toString(AppTypeEnum.values()));
        log.error(errorMsg);
        throw new IllegalArgumentException(errorMsg);
    }

    // ==================== 兼容旧 clear 方法（无需手动清理） ====================
    public static void clear() {
        log.debug("ScopedValue 上下文作用域结束自动释放，无需手动清理");
    }

}