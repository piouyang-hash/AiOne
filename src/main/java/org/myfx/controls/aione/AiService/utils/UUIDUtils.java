package org.myfx.controls.aione.AiService.utils;

import java.util.UUID;

/**
 * UUID 工具类（生成标准 UUIDv4）
 * 对应前端 generateUUID 逻辑，基于 JDK 原生实现，安全且高效
 */
public final class UUIDUtils {

    // 私有构造器：禁止实例化工具类
    private UUIDUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 生成标准 UUIDv4 字符串（无连字符版本可选）
     * @return 标准 UUIDv4 字符串，格式如：550e8400-e29b-41d4-a716-446655440000
     */
    public static String generateUUID() {
        // JDK 原生方法，生成符合 RFC 4122 标准的 UUIDv4（随机 UUID）
        // 底层使用 SecureRandom（加密安全的随机数生成器），比普通 Random 更安全
        return UUID.randomUUID().toString();
    }

}