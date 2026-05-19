package org.myfx.controls.aione.UserService.util;

/**
 * 邮箱脱敏工具类
 * 统一管理邮箱脱敏规则，所有场景复用
 */
public class EmailDesensitizeUtil {

    /**
     * 邮箱脱敏核心方法
     * @param email 原始邮箱（可为null/空）
     * @return 脱敏后的邮箱字符串，非合法邮箱返回空字符串
     * 规则：123456@qq.com → 123****@qq.com；12@qq.com → 12****@qq.com
     */
    public static String desensitize(String email) {
        // 兜底处理：null/空/无@符号 → 返回空字符串
        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            return "";
        }

        String[] emailParts = email.split("@");
        String prefix = emailParts[0]; // 邮箱前缀
        String domain = emailParts[1]; // 邮箱域名

        // 前缀脱敏：≤3位保留全部，>3位保留前3位+****
        String desensitizedPrefix = prefix.length() <= 3
                ? prefix
                : prefix.substring(0, 3) + "****";

        return desensitizedPrefix + "@" + domain;
    }
}