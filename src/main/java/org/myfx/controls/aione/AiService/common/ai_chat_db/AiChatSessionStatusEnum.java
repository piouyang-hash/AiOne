package org.myfx.controls.aione.AiService.common.ai_chat_db;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * AI对话会话状态枚举
 * code：0=关闭，1=活跃
 * desc：状态描述
 */
@Getter // 自动生成getter方法，方便获取code和desc
public enum AiChatSessionStatusEnum {

    /** 会话关闭 */
    CLOSED(0, "关闭"),

    /** 会话活跃 */
    ACTIVE(1, "活跃");

    /** 状态编码（存入数据库的int值） */
    @EnumValue
    private final int code;

    /** 状态描述（用于前端展示/日志说明） */
    private final String desc;

    // 构造方法
    AiChatSessionStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // 扩展方法：根据code获取枚举（常用，比如从数据库查值后转换）
    public static AiChatSessionStatusEnum getByCode(int code) {
        for (AiChatSessionStatusEnum status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的会话状态编码：" + code);
    }

    // 扩展方法：根据desc模糊匹配（可选，按需使用）
    public static AiChatSessionStatusEnum getByDesc(String desc) {
        for (AiChatSessionStatusEnum status : values()) {
            if (status.getDesc().equals(desc)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的会话状态描述：" + desc);
    }
}