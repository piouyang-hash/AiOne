package org.myfx.controls.aione.AiService.common.ai_chat_db;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 情绪类型枚举
 * code：ACTIVITY=活跃度，LIKE=喜爱值，FAMILIAR=熟悉度（存入数据库的字符串值）
 * desc：情绪类型描述（用于前端展示/日志说明）
 */
@Getter // 自动生成getter方法，方便获取code和desc
public enum EmotionTypeEnum {

    /** 活跃度（AI活跃度情绪值，对应互动积极程度） */
    ACTIVITY("ACTIVITY", "活跃度"),

    /** 喜爱值（AI喜爱度情绪值，对应对用户的偏好程度） */
    LIKE("LIKE", "喜爱值"),

    /** 熟悉度（AI熟悉度情绪值，对应对用户的了解程度） */
    FAMILIAR("FAMILIAR", "熟悉度");

    /** 情绪编码（存入数据库的字符串值，对应ai_emotion_log_detail表的emotion_type字段） */
    @EnumValue // MyBatis-Plus注解：指定存入数据库的字段值
    private final String code;

    /** 情绪描述（用于前端展示/日志说明） */
    private final String desc;

    // 构造方法（私有，枚举固定写法）
    EmotionTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc; // 统一命名为desc，和原枚举保持一致
    }

    /**
     * 核心扩展方法：根据数据库code值获取枚举（查询数据库后转换枚举的核心方法）
     * @param code 数据库存储的情绪编码（如ACTIVITY/LIKE）
     * @return 匹配的枚举值
     * @throws IllegalArgumentException 无匹配编码时抛出异常
     */
    public static EmotionTypeEnum getByCode(String code) {
        for (EmotionTypeEnum emotionType : values()) {
            if (emotionType.getCode().equals(code)) {
                return emotionType;
            }
        }
        throw new IllegalArgumentException("无效的情绪类型编码：" + code);
    }

    /**
     * 扩展方法：根据描述获取枚举（可选，用于前端/日志反向转换）
     * @param desc 情绪描述（如“活跃度”/“喜爱值”）
     * @return 匹配的枚举值
     * @throws IllegalArgumentException 无匹配描述时抛出异常
     */
    public static EmotionTypeEnum getByDesc(String desc) {
        for (EmotionTypeEnum emotionType : values()) {
            if (emotionType.getDesc().equals(desc)) {
                return emotionType;
            }
        }
        throw new IllegalArgumentException("无效的情绪类型描述：" + desc);
    }
}