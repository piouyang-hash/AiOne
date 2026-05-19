package org.myfx.controls.aione.AiService.common.ai_chat_db;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * Token变动方式枚举
 */
@Getter
public enum TokenChangeWayEnum {

    /**
     * 用户主动聊天消耗
     */
    USER_CHAT("USER_CHAT", "用户主动聊天消耗"),

    /**
     * AI主动聊天消耗
     */
    AI_CHAT("AI_CHAT", "AI主动聊天消耗"),

    /**
     * AI回复输出消耗
     */
    AI_REPLY("AI_REPLY", "AI回复输出消耗"),

    /**
     * 系统赠送
     */
    GIFT("GIFT", "系统赠送"),

    /**
     * 充值
     */
    RECHARGE("RECHARGE", "充值"),

    /**
     * 活动奖励
     */
    ACTIVITY("ACTIVITY", "活动奖励");

    /**
     * 编码：存入数据库的字段值
     */
    @EnumValue
    private final String code;

    /**
     * 描述：中文说明
     */
    private final String desc;

    TokenChangeWayEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据编码反查枚举
     */
    public static TokenChangeWayEnum getByCode(String code) {
        for (TokenChangeWayEnum way : values()) {
            if (way.getCode().equals(code)) {
                return way;
            }
        }
        return null;
    }

    /**
     * 根据描述反查枚举
     */
    public static TokenChangeWayEnum getByDesc(String desc) {
        for (TokenChangeWayEnum way : values()) {
            if (way.getDesc().equals(desc)) {
                return way;
            }
        }
        return null;
    }
}