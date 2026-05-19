package org.myfx.controls.aione.AiService.common.ai_chat_db.order;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;

@Getter
public enum PayTypeEnum implements Serializable {

    /**
     * 微信支付
     */
    WECHAT(1, "微信"),

    /**
     * 支付宝支付
     */
    ALIPAY(2, "支付宝"),

    /**
     * 支付宝支付
     */
    WECHAT_OR_ALIPAY(3, "微信/支付宝"),

    /**
     * 余额支付
     */
    BALANCE(4, "余额");

    private static final long serialVersionUID = 1L;

    /**
     * 支付类型编码
     */
    @EnumValue
    private final Integer code;

    /**
     * 支付类型描述
     */
    private final String desc;

    // 构造方法
    PayTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据支付类型编码匹配枚举
     * @param code 支付类型编码（1=微信，2=支付宝，3=余额）
     * @return 匹配的枚举值 / null（code为null或无匹配值时）
     */
    public static PayTypeEnum getByCode(Integer code) {
        // 1. 非空校验：避免空指针
        if (code == null) {
            return null;
        }
        // 2. 遍历枚举值，匹配code
        for (PayTypeEnum payType : PayTypeEnum.values()) {
            // 使用Objects.equals避免code为null时的空指针（此处code非null，但养成习惯）
            if (Objects.equals(payType.getCode(), code)) {
                return payType;
            }
        }
        // 3. 无匹配值返回null
        return null;
    }
}