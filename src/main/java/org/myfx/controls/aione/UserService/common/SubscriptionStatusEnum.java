package org.myfx.controls.aione.UserService.common;

import com.baomidou.mybatisplus.annotation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * 订阅状态枚举
 */
@Getter
@Schema(description = "订阅状态枚举")
public enum SubscriptionStatusEnum {
    /**
     * 生效中
     */
    EFFECTIVE(1, "生效中"),
    /**
     * 已过期
     */
    EXPIRED(2, "已过期"),
    /**
     * 暂停
     */
    PAUSED(3, "暂停"),
    /**
     * 未激活
     */
    UNACTIVATED(4, "未激活"),
    /**
     * 已取消
     */
    CANCELED(5, "已取消");

    @EnumValue
    @Schema(description = "订阅状态编码")
    private final Integer code;

    @Schema(description = "订阅状态描述")
    private final String desc;

    SubscriptionStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static SubscriptionStatusEnum getByCode(Integer code) {
        for (SubscriptionStatusEnum enumObj : values()) {
            if (enumObj.getCode().equals(code)) {
                return enumObj;
            }
        }
        return null;
    }
}