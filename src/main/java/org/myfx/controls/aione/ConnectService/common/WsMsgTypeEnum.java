package org.myfx.controls.aione.ConnectService.common;

import lombok.Getter;

/**
 * WebSocket 消息类型枚举
 * 仅包含：心跳检测、AI消息推送
 */
@Getter
public enum WsMsgTypeEnum {

    /**
     * 心跳检测
     */
    HEARTBEAT("HEARTBEAT", "心跳检测"),

    /**
     * AI消息推送
     */
    AI_PUSH("AI_PUSH", "AI消息推送"),

    /**
     * AI消息出错
     */
    AI_ERROR("AI_ERROR", "AI错误推送");

    /**
     * 消息编码（前端/后端传输使用）
     */
    private final String code;

    /**
     * 消息描述（注释/日志使用）
     */
    private final String desc;

    WsMsgTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据编码反查枚举（容错核心方法）
     */
    public static WsMsgTypeEnum getByCode(String code) {
        for (WsMsgTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
