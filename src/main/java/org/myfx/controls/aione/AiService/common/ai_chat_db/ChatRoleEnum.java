package org.myfx.controls.aione.AiService.common.ai_chat_db;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * AI对话角色枚举（映射数据库tinyint字段）
 */
@Getter
public enum ChatRoleEnum {
    /** 用户角色 */
    USER(1, "user"),
    /** AI助手角色 */
    ASSISTANT(2, "assistant"),
    /** SpringBoot系统角色（替代原system，避免AI混淆） */
    SPRINGBOOT(3, "springboot");

    /** 数据库存储的数值（tinyint），@EnumValue标注映射字段 */
    @EnumValue
    private final Integer code;
    /** 角色描述（便于代码阅读） */
    private final String desc;

    ChatRoleEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // 可选：根据code反查枚举（方便查询结果转换）
    public static ChatRoleEnum getByCode(Integer code) {
        for (ChatRoleEnum role : values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("无效的对话角色code：" + code);
    }
}