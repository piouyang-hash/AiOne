package org.myfx.controls.aione.AiService.common.ai_chat_db;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * AI心情强度枚举（通用1-5级，控制AI说话风格的浓度）
 */
@Getter
public enum AiMoodStrengthEnum {

    /** 1级：轻微（风格浓度最低，接近中性） */
    LEVEL_1(1, "轻微", "风格浓度10%：仅隐约体现AI基础话术风格，整体偏中性"),
    /** 2级：较弱（风格浓度较低） */
    LEVEL_2(2, "较弱", "风格浓度30%：适度体现AI基础风格，不夸张、不突出"),
    /** 3级：中等（风格浓度适中，平衡自然） */
    LEVEL_3(3, "中等", "风格浓度50%：完全展现AI基础风格，自然不刻意"),
    /** 4级：较强（风格浓度较高，情感更明显） */
    LEVEL_4(4, "较强", "风格浓度70%：强化AI基础风格，情感表达更鲜明"),
    /** 5级：强烈（风格浓度最高，极致体现） */
    LEVEL_5(5, "强烈", "风格浓度90%：极致放大AI基础风格，特点突出、记忆点强");

    /** 强度编码（存入数据库，@EnumValue标注） */
    @EnumValue
    private final Integer code;

    /** 强度名称（前端展示/配置） */
    private final String name;

    /** 强度话术规则（控制基础风格的浓度，通用所有AI心情） */
    private final String strengthSpeakRule;

    AiMoodStrengthEnum(Integer code, String name, String strengthSpeakRule) {
        this.code = code;
        this.name = name;
        this.strengthSpeakRule = strengthSpeakRule;
    }

    /** 根据编码获取强度枚举 */
    public static AiMoodStrengthEnum getByCode(Integer code) {
        for (AiMoodStrengthEnum strength : values()) {
            if (strength.getCode().equals(code)) {
                return strength;
            }
        }
        return LEVEL_3; // 默认中等强度
    }
}