package org.myfx.controls.aione.AiService.common.ai_chat_db.order;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 订单类型枚举（独立版）
 * 核心：只映射订单编码，适配数据库订单类型字段
 */
@Getter
public enum OrderTypeEnum implements Serializable {

    // 订单类型枚举项（只保留订单编码+订单描述）
    RECHARGE(1, "充值订单"),
    CONSUME(2, "消费订单"),
    REFUND(3, "退款订单"),
    FREEZE(4, "冻结订单"),
    UNFREEZE(5, "解冻订单");

    // ========== 核心字段 ==========
    /** 订单编码（对应数据库订单类型字段，加@EnumValue适配MP） */
    @EnumValue // MP核心注解：标记对应数据库TINYINT的数值
    private final Integer code;
    /** 订单描述 */
    private final String desc;

    // ========== 缓存（仅订单编码） ==========
    private static final Map<Integer, OrderTypeEnum> ORDER_CODE_CACHE = new HashMap<>();
    static {
        for (OrderTypeEnum e : values()) {
            ORDER_CODE_CACHE.put(e.getCode(), e);
        }
    }

    // ========== 构造器 ==========
    OrderTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // ========== 核心方法 ==========
    /**
     * 根据订单编码获取枚举（唯一核心方法）
     */
    public static OrderTypeEnum getByCode(Integer orderCode) {
        OrderTypeEnum enumObj = ORDER_CODE_CACHE.get(orderCode);
        if (enumObj == null) {
            throw new IllegalArgumentException("无效订单编码：" + orderCode);
        }
        return enumObj;
    }
}
