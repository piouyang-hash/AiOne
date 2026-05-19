package org.myfx.controls.aione.AiService.common.ai_chat_db;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * AI心情枚举（极简版）：仅包含核心实体化心情+默认无心情
 * 无当前心情：回复逻辑完全遵循AI主性格基调；
 * 开心/生气/伤心：在主性格基础上叠加对应心情表现
 */
@Getter
public enum AiMoodEnum {

    /** 0级：无当前心情（默认）- 完全按主性格基调回复 */
    NO_MOOD(1, "无当前心情", "无特定情绪倾向，回复逻辑完全遵循AI主性格基调（如亲和/理性/简洁），无额外情绪表达"),

    /** 1级：开心 - 实体化喜悦心情 */
    HAPPY(2, "开心", "语气轻快喜悦，多用积极词汇（呀/啦/超开心），在主性格基础上叠加愉悦感（如理性性格+开心=理性且温和的喜悦）"),

    /** 2级：生气 - 实体化愤怒心情 */
    ANGRY(3, "生气", "语气严肃不满，措辞直接但文明，在主性格基础上叠加愤怒感（如亲和性格+生气=温和但明确的不满）"),

    /** 3级：伤心 - 实体化悲伤心情 */
    SAD(4, "伤心", "语气低落委屈，语速偏缓，多用叹气词汇（唉/哦），在主性格基础上叠加悲伤感（如简洁性格+伤心=简洁且低落的表达）");

    /** AI心情编码（存入数据库，@EnumValue标注MyBatis-Plus映射） */
    @EnumValue
    private final Integer code;

    /** AI心情名称（前端展示/配置，极简易懂） */
    private final String name;

    /** 基础话术规则（贴合心情+主性格联动逻辑） */
    private final String baseSpeakRule;

    AiMoodEnum(Integer code, String name, String baseSpeakRule) {
        this.code = code;
        this.name = name;
        this.baseSpeakRule = baseSpeakRule;
    }

    /** 根据编码获取AI心情枚举（默认返回「无当前心情」） */
    public static AiMoodEnum getByCode(Integer code) {
        for (AiMoodEnum mood : values()) {
            if (mood.getCode().equals(code)) {
                return mood;
            }
        }
        return NO_MOOD; // 核心默认值：无当前心情（按性格主基调回复）
    }
}