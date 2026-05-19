package org.myfx.controls.aione.UserService.util;

import java.security.SecureRandom;
import java.util.Random;

public class CodeUtils {
    /**
     * 生成6位随机数字验证码
     */
    public static String generateCode() {
        Random random = new Random();
        // 生成100000-999999之间的随机数（保证6位）
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }


    /**
     * 生成高强度随机密钥/密码（支持JWT密钥、用户复杂密码等）
     * @param length 密钥长度（建议JWT密钥至少32位，密码至少12位）
     * @param includeSpecialChar 是否包含特殊字符（!@#$%^&*()_+-=[]{}|;:,.<>?）
     * @return 高强度随机字符串
     */
    public static String generateSecureKey(int length, boolean includeSpecialChar) {
        // 1. 定义字符池：大小写字母 + 数字 + （可选）特殊字符
        StringBuilder charPool = new StringBuilder();
        // 小写字母
        charPool.append("abcdefghijklmnopqrstuvwxyz");
        // 大写字母
        charPool.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        // 数字
        charPool.append("0123456789");
        // 特殊字符（可根据需求调整，避免易混淆字符如l、1、O、0）
        if (includeSpecialChar) {
            charPool.append("!@#$%^&*()_+-=[]{}|;:,.<>?");
        }

        // 2. 使用SecureRandom（比Random更安全，适合密码/密钥生成）
        SecureRandom random = new SecureRandom();
        StringBuilder key = new StringBuilder();

        // 3. 随机选取字符生成指定长度的密钥
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(charPool.length());
            key.append(charPool.charAt(randomIndex));
        }

        return key.toString();
    }

    /**
     * 简化方法：生成32位包含特殊字符的JWT密钥（常用默认配置）
     */
    public static String generateJwtSecret() {
        return generateSecureKey(32, true);
    }

    /**
     * 简化方法：生成16位用户高强度密码（包含特殊字符）
     */
    public static String generateStrongPassword() {
        return generateSecureKey(16, true);
    }

}