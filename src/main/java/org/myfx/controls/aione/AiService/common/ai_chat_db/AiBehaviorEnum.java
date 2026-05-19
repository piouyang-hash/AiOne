package org.myfx.controls.aione.AiService.common.ai_chat_db;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * AI行为枚举
 */
@Getter
public enum AiBehaviorEnum {
    // ========== AI核心行为：等待 ==========
    WAIT("WAIT", "AI等待用户交互"),

    // ========== AI核心行为：接收用户消息 ==========
    USER_SEND_MSG("USER_SEND_MSG", "AI接收用户发送的聊天消息"),

    // ========== AI核心行为：主动发送消息 ==========
    AI_ACTIVE_SEND_MSG("AI_ACTIVE_SEND_MSG", "AI主动发送消息给用户");

    /**
     * 行为编码（唯一标识，存入数据库的字段）
     */
    @EnumValue
    private final String code;

    /**
     * 行为描述（中文名称，用于前端展示/日志说明）
     */
    private final String desc;

    // 构造方法（移除score参数）
    AiBehaviorEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据描述反查枚举
     */
    public static AiBehaviorEnum getByDesc(String desc) {
        for (AiBehaviorEnum behavior : values()) {
            if (behavior.getDesc().equals(desc)) {
                return behavior;
            }
        }
        return null;
    }

    /**
     * 根据编码反查枚举
     */
    public static AiBehaviorEnum getByCode(String code) {
        for (AiBehaviorEnum behavior : values()) {
            if (behavior.getCode().equals(code)) {
                return behavior;
            }
        }
        return null;
    }
}