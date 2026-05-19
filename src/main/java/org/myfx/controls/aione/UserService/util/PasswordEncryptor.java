package org.myfx.controls.aione.UserService.util;

import java.util.Arrays;

import org.mindrot.jbcrypt.BCrypt;

/**
 * 密码加密工具类：基于BCrypt算法（带盐加密），支持明文密码"自毁"（使用后清除内存）
 */
public class PasswordEncryptor {

    // 工作因子（4-31之间，值越大加密越慢，安全性越高，推荐12）
    private static final int WORK_FACTOR = 12;

    // 私有构造：禁止实例化（工具类用静态方法）
    private PasswordEncryptor() {}

    /**
     * 加密密码（直接传String，内部自动处理转换和自毁）
     * @param rawPassword 明文密码（String类型，直接传）
     * @return 加密后的哈希字符串（可直接存数据库）
     */
    public static String encrypt(String rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("明文密码不能为null");
        }

        // 1. String转char[]（内部处理）
        char[] rawPwdChars = rawPassword.toCharArray();
        try {
            // 2. 生成盐值+加密
            String salt = BCrypt.gensalt(WORK_FACTOR);
            String passwordStr = new String(rawPwdChars);
            return BCrypt.hashpw(passwordStr, salt);
        } finally {
            // 3. 自毁：清除内存中的明文char[]
            Arrays.fill(rawPwdChars, (char) 0);
        }
    }

    /**
     * 验证密码（直接传String，内部自动处理转换和自毁，外部无额外步骤）
     * @param rawPassword 待验证的明文密码（String类型，直接传）
     * @param encryptedPassword 加密后的哈希字符串（String类型，从数据库取出）
     * @return true=匹配，false=不匹配
     */
    public static boolean verify(String rawPassword, String encryptedPassword) {
        // 1. 先判空（避免空指针）
        if (rawPassword == null || encryptedPassword == null) {
            return false;
        }

        // 2. String转char[]（内部处理，外部不用管）
        char[] rawPwdChars = rawPassword.toCharArray();
        try {
            // 3. 临时转String用于BCrypt验证（BCrypt.checkpw需要String参数）
            String passwordStr = new String(rawPwdChars);
            return BCrypt.checkpw(passwordStr, encryptedPassword);
        } finally {
            // 4. 自毁：清除内存中的明文密码char[]（核心安全步骤不丢）
            Arrays.fill(rawPwdChars, (char) 0);
        }
    }
}