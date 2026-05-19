package org.myfx.controls.aione.AiService.common.my_memory_db;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 爱好分类枚举（用于爱好字典表/关联表的分类标记，适配数据库存储）
 * code：存入数据库的分类编码（@EnumValue标注）
 * desc：分类中文描述（用于前端展示/AI提示词.txt）
 */
@Getter
public enum HobbyCategoryEnum {
    // ========== 核心大类（覆盖现有爱好） ==========
    SPORT("SPORT", "运动类"),          // 跑步、健身、瑜伽、球类等
    ART("ART", "文艺类"),              // 阅读、书法、绘画、摄影等
    LEISURE("LEISURE", "休闲类"),      // 喝咖啡、品茶、发呆、垂钓等
    DIGITAL("DIGITAL", "数码类"),      // 玩游戏、数码产品折腾、编程等
    HANDMADE("HANDMADE", "手工类"),    // DIY手工、手账、陶艺、编织等
    FOOD("FOOD", "美食类"),            // 品尝美食、烹饪、烘焙、探店等
    SOCIAL("SOCIAL", "社交类"),        // 参加俱乐部、聚会、桌游、露营等

    // ========== 补充常用大类（丰富分类维度） ==========
    STUDY("STUDY", "学习类"),          // 考证、看书学习、语言学习、技能提升等
    TRAVEL("TRAVEL", "旅行类"),        // 旅游、自驾游、徒步、打卡景点等
    MUSIC("MUSIC", "音乐类"),          // 听歌、乐器演奏、K歌、听音乐会等
    MOVIE("MOVIE", "影视类"),          // 看电影、追剧、看综艺、影评创作等
    FITNESS("FITNESS", "健身类"),      // 健身、瑜伽、普拉提、撸铁等（细分运动类）
    PET("PET", "宠物类"),              // 养宠物、遛宠物、宠物美容、宠物训练等
    CAREER("CAREER", "职场类"),        // 职场交流、商务谈判、行业分享、副业等
    GARDEN("GARDEN", "园艺类");        // 养花种草、园艺DIY、阳台种植、多肉养护等

    /**
     * 分类编码（存入数据库，@EnumValue标注）
     */
    @EnumValue
    private final String code;

    /**
     * 分类中文描述
     */
    private final String desc;

    // 构造方法
    HobbyCategoryEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}