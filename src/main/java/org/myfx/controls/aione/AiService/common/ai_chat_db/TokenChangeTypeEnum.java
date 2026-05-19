package org.myfx.controls.aione.AiService.common.ai_chat_db;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * Token变动类型枚举
 */
@Getter
public enum TokenChangeTypeEnum {

    /**
     * 消耗(增加Token消耗)
     */
    CONSUME(1, "消耗（累加）"),

    /**
     * 计费(重置Token)
     */
    BILLING(2, "计费/重置（扣减）");

    /**
     * 编码：存入数据库的字段值
     */
    @EnumValue
    private final Integer code;

    /**
     * 描述：中文说明
     */
    private final String desc;

    TokenChangeTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据编码反查枚举
     */
    public static TokenChangeTypeEnum getByCode(Integer code) {
        for (TokenChangeTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据描述反查枚举
     */
    public static TokenChangeTypeEnum getByDesc(String desc) {
        for (TokenChangeTypeEnum type : values()) {
            if (type.getDesc().equals(desc)) {
                return type;
            }
        }
        return null;
    }
}