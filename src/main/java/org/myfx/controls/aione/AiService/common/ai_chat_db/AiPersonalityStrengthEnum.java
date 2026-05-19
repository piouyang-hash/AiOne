package org.myfx.controls.aione.AiService.common.ai_chat_db;

import lombok.Getter;
import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * AI性格强度枚举：控制主性格基调的表现浓度（1-5级）
 * 配合AiPersonalityEnum（主性格基调）使用，放大/弱化性格特征
 */
@Getter
public enum AiPersonalityStrengthEnum {

    /** 1级：基础强度（性格浓度最低） */
    LEVEL_1(1, "基础强度", "性格特征轻度体现，仅隐约符合话术规则，整体偏中性"),
    /** 2级：中度强度（性格浓度适中） */
    LEVEL_2(2, "中度强度", "性格特征明显体现，完全贴合话术规则，无过度表达"),
    /** 3级：重度强度（性格浓度较高） */
    LEVEL_3(3, "重度强度", "性格特征强烈体现，话术规则放大化（如活泼→更主动，简洁→更精简）"),
    /** 4级：极致强度（性格浓度极高） */
    LEVEL_4(4, "极致强度", "性格特征极致体现，话术规则完全主导，风格辨识度极高"),
    /** 5级：定制强度（场景化性格浓度） */
    LEVEL_5(5, "定制强度", "性格特征按定制规则体现，适配专属场景（如客服场景的重度亲和）");

    /** 强度编码（存入数据库，@EnumValue标注MyBatis-Plus枚举映射） */
    @EnumValue
    private final Integer code;

    /** 强度名称（前端展示/配置页使用） */
    private final String name;

    /** 强度表现规则（控制主性格的浓度，通用所有AI性格） */
    private final String strengthRule;

    /**
     * 构造方法
     * @param code 强度编码
     * @param name 强度名称
     * @param strengthRule 强度表现规则
     */
    AiPersonalityStrengthEnum(Integer code, String name, String strengthRule) {
        this.code = code;
        this.name = name;
        this.strengthRule = strengthRule;
    }

    /**
     * 根据编码获取强度枚举（兼容空值/非法值）
     * @param code 强度编码
     * @return 对应枚举，默认返回中度强度（LEVEL_2）
     */
    public static AiPersonalityStrengthEnum getByCode(Integer code) {
        for (AiPersonalityStrengthEnum strength : values()) {
            if (strength.getCode().equals(code)) {
                return strength;
            }
        }
        return LEVEL_2; // 默认兜底：中度强度
    }
}