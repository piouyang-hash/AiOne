package org.myfx.controls.aione.ServiceCommon.serviceEnum;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 逻辑删除枚举（适配SAGA场景 + MyBatis-Plus自动映射）
 * 注：@EnumValue 是 MyBatis-Plus 提供的注解，用于指定枚举与数据库字段的映射值
 */
@Getter // Lombok 简化getter方法编写，也可手动写getCode()
public enum LogicalDeleteEnum {

    /**
     * 未删除（默认值）
     */
    NOT_DELETED(0, "未删除"),

    /**
     * 已删除（SAGA补偿/业务删除时标记）
     */
    DELETED(1, "已删除");

    /**
     * 数据库存储的数值（Integer类型），@EnumValue 标记该字段为映射值
     */
    @EnumValue
    private final Integer code;

    /**
     * 枚举描述（便于业务理解）
     */
    private final String desc;

    // 构造方法
    LogicalDeleteEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // 【可选】根据code反向获取枚举（业务常用）
    public static LogicalDeleteEnum getByCode(Integer code) {
        for (LogicalDeleteEnum enums : values()) {
            if (enums.getCode().equals(code)) {
                return enums;
            }
        }
        // 兜底返回未删除，避免空指针
        return NOT_DELETED;
    }
}
