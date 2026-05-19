package org.myfx.controls.aione.ServiceCommon.serviceEnum;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * JWT令牌类型枚举
 * <p>区分访问令牌(短时效)和刷新令牌(长时效)，用于Token生成、校验、过期策略区分</p>
 */
@Getter
@Schema(description = "JWT令牌类型枚举")
public enum JwtTokenType {

    /**
     * 访问令牌
     * 用途：接口鉴权，短时效（建议15-30分钟）
     */
    @Schema(description = "访问令牌(短时效)")
    ACCESS("access_token", "访问令牌"),

    /**
     * 刷新令牌
     * 用途：刷新访问令牌，长时效（建议7-30天）
     */
    @Schema(description = "刷新令牌(长时效)")
    REFRESH("refresh_token", "刷新令牌");

    /**
     * 令牌类型标识（用于Token载荷/Redis键区分）
     */
    private final String type;

    /**
     * 描述
     */
    private final String description;

    /**
     * 构造方法
     *
     * @param type        类型标识
     * @param description 描述
     */
    JwtTokenType(String type, String description) {
        this.type = type;
        this.description = description;
    }

    /**
     * 根据类型标识获取枚举
     *
     * @param type 类型标识
     * @return 对应的枚举，不存在则抛出异常
     */
    public static JwtTokenType getByType(String type) {
        for (JwtTokenType tokenType : JwtTokenType.values()) {
            if (tokenType.getType().equals(type)) {
                return tokenType;
            }
        }
        throw new IllegalArgumentException("非法的JWT令牌类型：" + type);
    }

    /**
     * 校验类型标识是否合法
     *
     * @param type 类型标识
     * @return true-合法 false-非法
     */
    public static boolean isValidType(String type) {
        for (JwtTokenType tokenType : JwtTokenType.values()) {
            if (tokenType.getType().equals(type)) {
                return true;
            }
        }
        return false;
    }
}