package org.myfx.controls.aione.AiService.common.user_behavior_db;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 用户行为枚举（AI交互核心行为，code存入数据库，desc为中文描述）
 */
@Getter
public enum BehaviorEnum {
    // ========== AI核心交互行为 ==========
    CHAT_SEND_MSG("CHAT_SEND_MSG", "发送AI聊天消息"),
    CHAT_LIKE("CHAT_LIKE", "点赞AI回复"),
    CHAT_COLLECT("CHAT_COLLECT", "收藏AI回复"),
    CHAT_SHARE("CHAT_SHARE", "分享AI回复"),
    CHAT_DISLIKE("CHAT_DISLIKE", "点踩AI回复"),
    CHAT_VIEW_HISTORY("CHAT_VIEW_HISTORY", "查看聊天历史"),
    CHAT_FEEDBACK("CHAT_FEEDBACK", "反馈AI回复问题"),
    USER_OFFLINE("USER_OFFLINE", "用户离线");

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
    BehaviorEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // 根据描述反查枚举
    public static BehaviorEnum getByDesc(String desc) {
        for (BehaviorEnum behavior : values()) {
            if (behavior.getDesc().equals(desc)) {
                return null;
            }
        }
        return null;
    }

    // 根据编码反查枚举
    public static BehaviorEnum getByCode(String code) {
        for (BehaviorEnum behavior : values()) {
            if (behavior.getCode().equals(code)) {
                return behavior;
            }
        }
        return null;
    }
}