package org.myfx.controls.aione.UserService.common;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 消息状态枚举（对应user_service_message表的message_status字段）
 * 通用状态：适用于所有业务模块的异步消息
 */
@Getter
public enum MessageStatusEnum {
    /**
     * 未执行（消息刚创建，未处理）
     */
    UNINIT(0, "未执行"),
    /**
     * 成功（业务处理成功）
     */
    SUCCESS(1, "成功"),
    /**
     * 失败（业务处理失败，可重试）
     */
    FAIL(2, "失败");

    /**
     * 数据库存储值（对应表的message_status字段）
     */
    @EnumValue
    private final Integer code;
    /**
     * 状态描述
     */
    private final String desc;

    MessageStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据code获取枚举（用于数据库值转枚举）
     */
    public static MessageStatusEnum getByCode(Integer code) {
        for (MessageStatusEnum enumItem : values()) {
            if (enumItem.getCode().equals(code)) {
                return enumItem;
            }
        }
        throw new IllegalArgumentException("无效的消息状态code：" + code);
    }
}