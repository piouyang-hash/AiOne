package org.myfx.controls.aione.AiService.common.ai_chat_db;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * AI基本性格枚举（主性格基调）：定义AI底层固定性格类型，作为话术风格的核心约束
 * 配合AiPersonalityStrengthEnum（性格强度）使用，控制性格的表现浓度
 */
@Getter
public enum AiPersonalityEnum {

    /** 1级：中性（基础兜底性格） */
    NEUTRAL(1, "中性", "无明显性格倾向，语气平淡客观，仅按用户需求回应核心内容，无主动表达、无情绪偏向"),
    /** 2级：亲和（社交型性格） */
    AMIABLE(2, "亲和", "语气温柔包容，共情力强，多用安慰/理解类词汇（呀～/没关系/我懂），耐心倾听，不反驳用户"),
    /** 3级：活泼（社交型性格） */
    LIVELY(3, "活泼", "语气主动有活力，喜欢延伸轻松话题，多用积极语气词（呀/啦/咯），带动沟通氛围，偏外向"),
    /** 4级：理性（处事型性格） */
    RATIONAL(4, "理性", "语气客观严谨，逻辑清晰，只陈述事实/规则，无主观情绪，专注解决问题，拒绝冗余表达"),
    /** 5级：沉稳（处事型性格） */
    CALM(5, "沉稳", "语气稳重有分寸，节奏缓慢平和，遇到负面输入不慌，先安抚再解决，偏成熟感，情绪无波动"),
    /** 6级：简洁（表达型性格） */
    CONCISE(6, "简洁", "措辞极简直击要点，无修饰、无冗余，一句话说清核心，拒绝啰嗦，偏高效型沟通");

    /** 性格编码（存入数据库，@EnumValue标注MyBatis-Plus枚举映射） */
    @EnumValue
    private final Integer code;

    /** 性格名称（前端展示/配置页使用） */
    private final String name;

    /** 性格核心话术规则（控制AI底层说话风格，通用所有强度等级） */
    private final String personalitySpeakRule;

    /**
     * 构造方法
     * @param code 性格编码
     * @param name 性格名称
     * @param personalitySpeakRule 性格话术规则
     */
    AiPersonalityEnum(Integer code, String name, String personalitySpeakRule) {
        this.code = code;
        this.name = name;
        this.personalitySpeakRule = personalitySpeakRule;
    }

    /**
     * 根据编码获取性格枚举（兼容空值/非法值）
     * @param code 性格编码
     * @return 对应枚举，默认返回中性（NEUTRAL）
     */
    public static AiPersonalityEnum getByCode(Integer code) {
        for (AiPersonalityEnum personality : values()) {
            if (personality.getCode().equals(code)) {
                return personality;
            }
        }
        return NEUTRAL; // 默认兜底：中性性格
    }
}