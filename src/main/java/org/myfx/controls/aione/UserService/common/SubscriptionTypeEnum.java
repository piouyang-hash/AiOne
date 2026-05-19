package org.myfx.controls.aione.UserService.common;

import com.baomidou.mybatisplus.annotation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * 订阅类型枚举（套餐类型）
 */
@Getter
@Schema(description = "订阅类型枚举")
public enum SubscriptionTypeEnum {
    /**
     * 月卡
     */
    MONTHLY_CARD(1, "月卡"),
    /**
     * 年卡
     */
    YEARLY_CARD(2, "年卡"),
    /**
     * 终身卡
     */
    LIFETIME_CARD(3, "终身卡"),
    /**
     * 体验卡
     */
    TRIAL_CARD(4, "体验卡");

    @EnumValue
    @Schema(description = "订阅类型编码")
    private final Integer code;

    @Schema(description = "订阅类型描述")
    private final String desc;

    SubscriptionTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static SubscriptionTypeEnum getByCode(Integer code) {
        for (SubscriptionTypeEnum enumObj : values()) {
            if (enumObj.getCode().equals(code)) {
                return enumObj;
            }
        }
        return null;
    }
}