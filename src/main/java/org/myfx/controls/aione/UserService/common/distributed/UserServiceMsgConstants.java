package org.myfx.controls.aione.UserService.common.distributed;

import lombok.Getter;

/**
 * 用户微服务-业务模块+类型常量枚举
 */
public class UserServiceMsgConstants {

    // ========== 第一步：定义业务模块常量 ==========
    public static final String BIZ_MODULE_ACCOUNT = "account";   // 账户模块
    public static final String BIZ_MODULE_SECURITY = "security"; // 安全模块
    public static final String BIZ_MODULE_PROFILE = "profile";   // 个人信息模块

    // ========== 第二步：定义各模块下的业务类型枚举 ==========
    // 账户模块-业务类型
    @Getter
    public enum AccountBizType {
        INIT(0, "账户初始化"),
        CANCEL(1, "用户注销");

        private final int code;
        private final String desc;

        AccountBizType(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

    }

    // 安全模块-业务类型（后续扩展用）
    @Getter
    public enum SecurityBizType {
        RESET_PASSWORD(0, "密码重置"),
        CHANGE_PHONE(1, "手机号换绑");

        private final int code;
        private final String desc;

        SecurityBizType(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

    }
}