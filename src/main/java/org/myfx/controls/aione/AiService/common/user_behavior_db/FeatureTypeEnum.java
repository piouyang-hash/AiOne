package org.myfx.controls.aione.AiService.common.user_behavior_db;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 用户行为特征类型枚举
 * code：ACTIVITY=活跃度，FAVOR=喜爱度，FAMILIAR=熟悉度（存入数据库的字符串值）
 * desc：特征类型描述（用于前端展示/日志说明）
 */
@Getter // 自动生成getter方法，方便获取code和desc
public enum FeatureTypeEnum {

    /** 活跃度（用户互动频率相关的行为特征） */
    ACTIVITY("ACTIVITY", "活跃度"),

    /** 喜爱度（用户对内容偏好相关的行为特征） */
    FAVOR("FAVOR", "喜爱度"),

    /** 熟悉度（用户对AI/内容熟悉程度相关的行为特征） */
    FAMILIAR("FAMILIAR", "熟悉度");

    /** 特征编码（存入数据库的字符串值，对应MySQL VARCHAR） */
    @EnumValue // MyBatis-Plus注解：指定存入数据库的字段值
    private final String code;

    /** 特征描述（用于前端展示/日志说明） */
    private final String desc;

    // 构造方法（私有，枚举固定写法）
    FeatureTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc; // 统一命名为desc，和示例枚举保持一致（原description）
    }

    /**
     * 核心扩展方法：根据数据库code值获取枚举（查询数据库后转换枚举的核心方法）
     * @param code 数据库存储的特征编码（如ACTIVITY/FAVOR）
     * @return 匹配的枚举值
     * @throws IllegalArgumentException 无匹配编码时抛出异常
     */
    public static FeatureTypeEnum getByCode(String code) {
        for (FeatureTypeEnum featureType : values()) {
            if (featureType.getCode().equals(code)) {
                return featureType;
            }
        }
        throw new IllegalArgumentException("无效的行为特征编码：" + code);
    }

    /**
     * 扩展方法：根据描述获取枚举（可选，用于前端/日志反向转换）
     * @param desc 特征描述（如“活跃度”/“喜爱度”）
     * @return 匹配的枚举值
     * @throws IllegalArgumentException 无匹配描述时抛出异常
     */
    public static FeatureTypeEnum getByDesc(String desc) {
        for (FeatureTypeEnum featureType : values()) {
            if (featureType.getDesc().equals(desc)) {
                return featureType;
            }
        }
        throw new IllegalArgumentException("无效的行为特征描述：" + desc);
    }
}