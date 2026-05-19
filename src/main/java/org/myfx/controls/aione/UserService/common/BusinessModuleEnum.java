package org.myfx.controls.aione.UserService.common;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 业务模块枚举（对应user_service_message表的business_module字段）
 * 适配多微服务：账户/用户/订单/优惠券等模块
 * code改为数字类型，便于数据库存储和排序
 */
@Getter
public enum BusinessModuleEnum {
    /**
     * 账户模块（账户微服务）
     */
    ACCOUNT(1, "账户模块"),
    /**
     * 用户模块（用户微服务）
     */
    USER(2, "用户模块"),
    /**
     * 订单模块（订单微服务）
     */
    ORDER(3, "订单模块"),
    /**
     * 优惠券模块（营销微服务）
     */
    COUPON(4, "优惠券模块"),
    /**
     * 支付模块（支付微服务）
     */
    PAY(5, "支付模块");

    /**
     * 数据库存储值（对应表的business_module字段，改为数字类型）
     */
    @EnumValue
    private final Integer code;
    /**
     * 模块描述
     */
    private final String desc;

    BusinessModuleEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据数字code获取枚举（用于数据库值转枚举）
     */
    public static BusinessModuleEnum getByCode(Integer code) {
        for (BusinessModuleEnum enumItem : values()) {
            if (enumItem.getCode().equals(code)) {
                return enumItem;
            }
        }
        throw new IllegalArgumentException("无效的业务模块code：" + code);
    }
}