package org.myfx.controls.aione.ServiceCommon.context;

import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.AppTypeEnum;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.RoleEnum;

import java.util.Arrays;

/**
 * 用户上下文工具类：用ThreadLocal存储当前线程的用户ID、用户名、角色
 * ThreadLocal特点：线程私有，多线程间数据隔离，避免参数传递冗余
 */
@Slf4j
public class UserContext {

    // 定义 ThreadLocal 变量：存储用户 ID
    private static final ThreadLocal<Integer> CURRENT_USER_ID = new ThreadLocal<>();
    // 新增：存储用户角色
    private static final ThreadLocal<RoleEnum> CURRENT_ROLE = new ThreadLocal<>();
    // 新增：存储应用类型（枚举类型）
    private static final ThreadLocal<AppTypeEnum> CURRENT_APP_TYPE = new ThreadLocal<>();

    // ==================== 用户 ID 相关操作 ====================
    /**
     * 设置当前线程的用户 ID
     */
    public static void setUserId(Integer userId) {
        CURRENT_USER_ID.set(userId);
    }

    /**
     * 获取当前线程的用户 ID
     * @return 用户 ID（未设置则返回 null）
     */
    public static Integer getUserId() {
        return CURRENT_USER_ID.get();
    }

    // ==================== 用户角色相关操作 ====================
    /**
     * 设置当前线程的用户角色（参数为枚举name字符串，显式对比枚举值）
     * @param roleName 枚举name（如"ADMIN"），空值/无效值会抛异常
     * @throws IllegalArgumentException 传入的角色name无效/为空时抛出
     */
    public static void setRole(String roleName) {
        // 1. 空值处理：清除ThreadLocal并抛出异常（和setAppType逻辑对齐）
        if (roleName == null || roleName.trim().isEmpty()) {
            CURRENT_ROLE.remove();
            String errorMsg = "用户角色枚举name不能为空或空字符串";
            log.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        // 2. 显式遍历枚举值，对比name（移除try-catch，主动校验）
        String targetName = roleName.trim();
        RoleEnum matchRole = null;
        for (RoleEnum role : RoleEnum.values()) {
            if (role.name().equals(targetName)) {
                matchRole = role;
                break; // 找到匹配项，终止遍历
            }
        }

        // 3. 未找到匹配项：打error日志并抛异常（提示合法值）
        if (matchRole == null) {
            String errorMsg = String.format("无效的用户角色枚举name：%s，合法值为：%s",
                    targetName, Arrays.toString(RoleEnum.values()));
            log.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        // 4. 找到匹配项：设置到ThreadLocal中
        CURRENT_ROLE.set(matchRole);
    }

    /**
     * 获取当前线程的用户角色
     * @return 用户角色（未设置则返回 null）
     */
    public static RoleEnum getRole() {
        return CURRENT_ROLE.get();
    }

    // ==================== 应用类型 相关操作 ====================
    /**
     * 设置当前线程的应用类型（参数为枚举name字符串，显式对比枚举值）
     * @param appTypeName 枚举name（如"AI_CHAT"），空值/无效值会抛异常
     * @throws IllegalArgumentException 传入的应用类型name无效时抛出
     */
    public static void setAppType(String appTypeName) {
        // 空值处理：清除ThreadLocal并抛出异常（比设置null更严格，符合显式校验逻辑）
        if (appTypeName == null || appTypeName.trim().isEmpty()) {
            CURRENT_APP_TYPE.remove();
            String errorMsg = "应用类型枚举name不能为空或空字符串";
            log.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        // 显式遍历枚举值，对比name（移除try-catch，改为主动校验）
        String targetName = appTypeName.trim();
        AppTypeEnum matchAppType = null;
        // 遍历所有枚举值，逐个对比name
        for (AppTypeEnum appType : AppTypeEnum.values()) {
            if (appType.name().equals(targetName)) {
                matchAppType = appType;
                break; // 找到匹配项，终止遍历
            }
        }

        // 未找到匹配项：打error日志并抛异常
        if (matchAppType == null) {
            String errorMsg = String.format("无效的应用类型枚举name：%s，合法值为：%s",
                    targetName, Arrays.toString(AppTypeEnum.values()));
            log.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        // 找到匹配项：设置到ThreadLocal中
        CURRENT_APP_TYPE.set(matchAppType);
    }

    /**
     * 获取当前线程的应用类型（直接返回AppTypeEnum枚举）
     * @return 应用类型枚举（未设置则返回 null）
     */
    public static AppTypeEnum getAppType() {
        return CURRENT_APP_TYPE.get();
    }

    /**
     * 清除当前线程存储的所有用户上下文数据（ID、用户名、角色、应用类型）
     */
    public static void clear() {
        CURRENT_USER_ID.remove();
        CURRENT_ROLE.remove();     // 清除角色
        CURRENT_APP_TYPE.remove(); // 新增：清除应用类型
    }
}