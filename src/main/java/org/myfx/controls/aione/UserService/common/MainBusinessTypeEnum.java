package org.myfx.controls.aione.UserService.common;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 主事务类型枚举（对应user_service_message表的main_business_type字段）
 */
@Getter
public enum MainBusinessTypeEnum {

    USER_REGISTER(0, "用户注册"),

    USER_CANCEL(1, "用户注销"),

    USER_UPDATE_INFO(2, "用户修改信息");

    @EnumValue
    private final Integer code;

    private final String desc;

    MainBusinessTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据编码获取枚举
     */
    public static MainBusinessTypeEnum getByCode(Integer code) {
        for (MainBusinessTypeEnum enumItem : values()) {
            if (enumItem.getCode().equals(code)) {
                return enumItem;
            }
        }
        throw new IllegalArgumentException("无效的主事务类型编码：" + code);
    }
}