package org.myfx.controls.aione.AiService.common.ai_chat_db;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 回收站状态枚举（适配用户侧删除场景 + MyBatis-Plus自动映射）
 * 注：@EnumValue 是 MyBatis-Plus 提供的注解，用于指定枚举与数据库字段的映射值
 */
@Getter // Lombok 简化getter方法编写，也可手动写getCode()
public enum RecycleStatusEnum {

    /**
     * 未放入回收站（默认值）
     */
    NOT_RECYCLE(0, "未放入回收站"),

    /**
     * 已放入回收站（用户侧删除时标记）
     */
    RECYCLED(1, "已放入回收站");

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
    RecycleStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // 【可选】根据code反向获取枚举（业务常用）
    public static RecycleStatusEnum getByCode(Integer code) {
        for (RecycleStatusEnum enums : values()) {
            if (enums.getCode().equals(code)) {
                return enums;
            }
        }
        // 兜底返回未放入回收站，避免空指针
        return NOT_RECYCLE;
    }
}