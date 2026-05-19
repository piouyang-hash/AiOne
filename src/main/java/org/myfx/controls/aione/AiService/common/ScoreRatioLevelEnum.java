package org.myfx.controls.aione.AiService.common;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 分数比例等级枚举（适配ScoreRatioCalculationUtil工具类返回值）
 * code：LOW=低，MEDIUM=中等，HIGH=高（存入数据库的字符串值，对应工具类返回的low/medium/high）
 * desc：等级描述（用于前端展示/日志说明）
 */
@Getter // 自动生成getter方法，方便获取code和desc
public enum ScoreRatioLevelEnum {

    /** 低等级（工具类返回low，对应分数区间0~30） */
    LOW("LOW", "低"),

    /** 中等等级（工具类返回medium，对应分数区间31~70） */
    MEDIUM("MEDIUM", "中等"),

    /** 高等级（工具类返回high，对应分数区间71~100） */
    HIGH("HIGH", "高");

    /** 等级编码（存入数据库的字符串值，对应MySQL VARCHAR，和工具类返回值语义对齐） */
    @EnumValue // MyBatis-Plus注解：指定存入数据库的字段值
    private final String code;

    /** 等级描述（用于前端展示/日志说明，中文语义化） */
    private final String desc;

    // 构造方法（私有，枚举固定写法）
    ScoreRatioLevelEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 核心扩展方法：根据数据库code值获取枚举（查询数据库后转换枚举的核心方法）
     * @param code 数据库存储的等级编码（如LOW/MEDIUM/HIGH）
     * @return 匹配的枚举值
     * @throws IllegalArgumentException 无匹配编码时抛出异常
     */
    public static ScoreRatioLevelEnum getByCode(String code) {
        for (ScoreRatioLevelEnum level : values()) {
            if (level.getCode().equals(code)) {
                return level;
            }
        }
        throw new IllegalArgumentException("无效的分数比例等级编码：" + code);
    }

    /**
     * 扩展方法：根据工具类返回的小写字符串获取枚举（适配工具类返回值的核心方法）
     * @param levelStr 工具类返回的等级字符串（如low/medium/high）
     * @return 匹配的枚举值
     * @throws IllegalArgumentException 无匹配字符串时抛出异常
     */
    public static ScoreRatioLevelEnum getByToolReturnStr(String levelStr) {
        // 先转大写，统一匹配枚举code（兼容工具类返回的小写）
        String upperCode = levelStr == null ? null : levelStr.toUpperCase();
        return getByCode(upperCode);
    }

    /**
     * 扩展方法：根据描述获取枚举（用于前端/日志反向转换）
     * @param desc 等级描述（如“低”/“中等”/“高”）
     * @return 匹配的枚举值
     * @throws IllegalArgumentException 无匹配描述时抛出异常
     */
    public static ScoreRatioLevelEnum getByDesc(String desc) {
        for (ScoreRatioLevelEnum level : values()) {
            if (level.getDesc().equals(desc)) {
                return level;
            }
        }
        throw new IllegalArgumentException("无效的分数比例等级描述：" + desc);
    }
}