package org.myfx.controls.aione.AiService.common.my_memory_db;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 通用爱好枚举（覆盖全品类常见爱好，包含code编码+desc描述）
 */
@Getter
public enum HobbyEnum {
    // ========== 运动类 ==========
    SPORT_RUNNING("SPORT_RUNNING", "跑步"),
    SPORT_SWIMMING("SPORT_SWIMMING", "游泳"),
    SPORT_BASKETBALL("SPORT_BASKETBALL", "篮球"),
    SPORT_FOOTBALL("SPORT_FOOTBALL", "足球"),
    SPORT_TENNIS("SPORT_TENNIS", "网球"),
    SPORT_BADMINTON("SPORT_BADMINTON", "羽毛球"),
    SPORT_TABLE_TENNIS("SPORT_TABLE_TENNIS", "乒乓球"),
    SPORT_YOGA("SPORT_YOGA", "瑜伽"),
    SPORT_PILATES("SPORT_PILATES", "普拉提"),
    SPORT_GYM("SPORT_GYM", "健身"),
    SPORT_RIDING("SPORT_RIDING", "骑行"),
    SPORT_HIKING("SPORT_HIKING", "徒步"),
    SPORT_CLIMBING("SPORT_CLIMBING", "攀岩"),
    SPORT_SKATING("SPORT_SKATING", "滑冰"),
    SPORT_SKIING("SPORT_SKIING", "滑雪"),
    SPORT_BOXING("SPORT_BOXING", "拳击"),
    SPORT_MARTIAL_ARTS("SPORT_MARTIAL_ARTS", "武术"),
    SPORT_GOLF("SPORT_GOLF", "高尔夫"),
    SPORT_SURFING("SPORT_SURFING", "冲浪"),
    SPORT_DIVING("SPORT_DIVING", "潜水"),

    // ========== 文艺类 ==========
    ART_READING("ART_READING", "阅读"),
    ART_WRITING("ART_WRITING", "写作"),
    ART_PAINTING("ART_PAINTING", "绘画"),
    ART_CALLIGRAPHY("ART_CALLIGRAPHY", "书法"),
    ART_SCULPTURE("ART_SCULPTURE", "雕塑"),
    ART_PHOTOGRAPHY("ART_PHOTOGRAPHY", "摄影"),
    ART_VIDEO_EDITING("ART_VIDEO_EDITING", "视频剪辑"),
    ART_MUSIC_LISTEN("ART_MUSIC_LISTEN", "听歌"),
    ART_MUSIC_PLAY_GUITAR("ART_MUSIC_PLAY_GUITAR", "弹吉他"),
    ART_MUSIC_PLAY_PIANO("ART_MUSIC_PLAY_PIANO", "弹钢琴"),
    ART_MUSIC_PLAY_VIOLIN("ART_MUSIC_PLAY_VIOLIN", "拉小提琴"),
    ART_DANCE("ART_DANCE", "跳舞"),
    ART_DRAMA("ART_DRAMA", "话剧"),
    ART_OPERA("ART_OPERA", "戏曲"),
    ART_CINEMA("ART_CINEMA", "看电影"),
    ART_TV("ART_TV", "追剧"),
    ART_POETRY("ART_POETRY", "写诗"),
    ART_FINE_ARTS("ART_FINE_ARTS", "赏析艺术品"),

    // ========== 休闲类 ==========
    LEISURE_COFFEE("LEISURE_COFFEE", "喝咖啡"),
    LEISURE_TEA("LEISURE_TEA", "品茶"),
    LEISURE_BOARD_GAME("LEISURE_BOARD_GAME", "桌游"),
    LEISURE_CARD_GAME("LEISURE_CARD_GAME", "打牌"),
    LEISURE_PUZZLE("LEISURE_PUZZLE", "拼拼图"),
    LEISURE_FISHING("LEISURE_FISHING", "钓鱼"),
    LEISURE_GARDENING("LEISURE_GARDENING", "园艺"),
    LEISURE_COOKING("LEISURE_COOKING", "烹饪"),
    LEISURE_BAKING("LEISURE_BAKING", "烘焙"),
    LEISURE_TRAVEL("LEISURE_TRAVEL", "旅行"),
    LEISURE_CAMPING("LEISURE_CAMPING", "露营"),
    LEISURE_STARGazing("LEISURE_STARGazing", "观星"),
    LEISURE_COLLECT("LEISURE_COLLECT", "收藏（邮票/手办/文物）"),
    LEISURE_PET_CARE("LEISURE_PET_CARE", "养宠物"),
    LEISURE_MASSAGE("LEISURE_MASSAGE", "按摩"),
    LEISURE_SPA("LEISURE_SPA", "做SPA"),

    // ========== 数码类 ==========
    DIGITAL_GAME("DIGITAL_GAME", "玩游戏"),
    DIGITAL_CODING("DIGITAL_CODING", "编程"),
    DIGITAL_ELECTRONICS("DIGITAL_ELECTRONICS", "研究电子产品"),
    DIGITAL_BLOG("DIGITAL_BLOG", "写博客"),
    DIGITAL_LIVE("DIGITAL_LIVE", "直播"),
    DIGITAL_PODCAST("DIGITAL_PODCAST", "听播客"),
    DIGITAL_VR("DIGITAL_VR", "玩VR"),
    DIGITAL_3D_PRINT("DIGITAL_3D_PRINT", "3D打印"),

    // ========== 手工类 ==========
    HANDMADE_DIY("HANDMADE_DIY", "DIY手工"),
    HANDMADE_KNITTING("HANDMADE_KNITTING", "编织"),
    HANDMADE_LEATHER("HANDMADE_LEATHER", "皮具制作"),
    HANDMADE_CANDLE("HANDMADE_CANDLE", "香薰蜡烛制作"),
    HANDMADE_SOAP("HANDMADE_SOAP", "手工皂制作"),
    HANDMADE_JEWELRY("HANDMADE_JEWELRY", "首饰制作"),
    HANDMADE_WOODWORK("HANDMADE_WOODWORK", "木工"),
    HANDMADE_PAPER_CRAFT("HANDMADE_PAPER_CRAFT", "纸艺"),

    // ========== 美食类 ==========
    FOOD_TASTE("FOOD_TASTE", "品尝美食"),
    FOOD_MAKING("FOOD_MAKING", "制作美食"),
    FOOD_BARBECUE("FOOD_BARBECUE", "烧烤"),
    FOOD_WINE_TASTING("FOOD_WINE_TASTING", "品酒"),
    FOOD_COFFEE_BREWING("FOOD_COFFEE_BREWING", "手冲咖啡"),
    FOOD_BAKERY("FOOD_BAKERY", "做面包"),
    FOOD_DESSERT("FOOD_DESSERT", "做甜品"),

    // ========== 社交类 ==========
    SOCIAL_PARTY("SOCIAL_PARTY", "参加派对"),
    SOCIAL_SPEECH("SOCIAL_SPEECH", "演讲"),
    SOCIAL_VOLUNTEER("SOCIAL_VOLUNTEER", "做志愿者"),
    SOCIAL_CLUB("SOCIAL_CLUB", "参加俱乐部活动"),

    // ========== 其他类 ==========
    OTHER_SLEEP("OTHER_SLEEP", "睡觉"),
    OTHER_SHOPPING("OTHER_SHOPPING", "购物"),
    OTHER_LEARNING("OTHER_LEARNING", "学习新技能"),
    OTHER_MEDITATION("OTHER_MEDITATION", "冥想"),
    OTHER_ASTRONOMY("OTHER_ASTRONOMY", "研究天文"),
    OTHER_GEOLOGY("OTHER_GEOLOGY", "研究地质");

    /**
     * 爱好编码（唯一标识）
     */
    @EnumValue
    private final String code;

    /**
     * 爱好描述（中文名称）
     */
    private final String desc;

    // 构造方法
    HobbyEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // 可选：根据描述反查枚举（方便业务使用）
    public static HobbyEnum getByDesc(String desc) {
        for (HobbyEnum hobby : values()) {
            if (hobby.getDesc().equals(desc)) {
                return hobby;
            }
        }
        return null;
    }

    // 可选：根据编码反查枚举
    public static HobbyEnum getByCode(String code) {
        for (HobbyEnum hobby : values()) {
            if (hobby.getCode().equals(code)) {
                return hobby;
            }
        }
        return null;
    }
}