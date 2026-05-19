package org.myfx.controls.aione.AiService.common.ai_chat_db;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * AI对话系统会话枚举
 * code：0=非系统会话，1=系统会话
 * desc：系统会话描述
 */
@Getter // 自动生成getter方法，方便获取code和desc
public enum AiChatSystemSessionEnum {

    /** 非系统会话（用户创建的普通会话） */
    NON_SYSTEM(0, "非系统会话"),

    /** 系统会话（平台预置的默认会话） */
    SYSTEM(1, "系统会话");

    /** 状态编码（存入数据库的int值，对应MySQL TINYINT） */
    @EnumValue // MyBatis-Plus注解：指定存入数据库的字段值
    private final int code;

    /** 状态描述（用于前端展示/日志说明） */
    private final String desc;

    // 构造方法（私有，枚举固定写法）
    AiChatSystemSessionEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // 核心扩展方法：根据数据库code值获取枚举（最常用，查询后转换）
    public static AiChatSystemSessionEnum getByCode(int code) {
        for (AiChatSystemSessionEnum sessionType : values()) {
            if (sessionType.getCode() == code) {
                return sessionType;
            }
        }
        throw new IllegalArgumentException("无效的系统会话编码：" + code);
    }

    // 扩展方法：根据描述获取枚举（可选，按需使用）
    public static AiChatSystemSessionEnum getByDesc(String desc) {
        for (AiChatSystemSessionEnum sessionType : values()) {
            if (sessionType.getDesc().equals(desc)) {
                return sessionType;
            }
        }
        throw new IllegalArgumentException("无效的系统会话描述：" + desc);
    }
}