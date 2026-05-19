package org.myfx.controls.aione.ServiceCommon.serviceEnum;

import com.baomidou.mybatisplus.annotation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * 应用类型枚举类（对应user_profile表的app_id字段）
 * 1=阅读器，2=AI聊天，3=拓展位（预留）
 */
@Getter
@Schema(description = "应用类型枚举")
public enum AppTypeEnum {
    /** 阅读器应用 */
    READER(1, "阅读器"),
    /** AI聊天应用 */
    AI_CHAT(2, "AI聊天"),
    /** 管理员系统应用 */
    ADMIN_SYSTEM(3, "管理员系统"),
    /** 拓展位（预留） */
    EXTENSION(4, "拓展位");

    /** 数据库存储的枚举值（对应app_id的TINYINT类型） */
    @EnumValue // 标记该字段映射到数据库的app_id
    @Schema(description = "枚举值（数据库存储）")
    private final Integer code;

    /** 枚举描述（前端展示/业务说明） */
    @Schema(description = "应用名称")
    private final String desc;

    // 构造方法
    AppTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据code获取枚举（方便业务层转换）
     */
    public static AppTypeEnum getByCode(Integer code) {
        for (AppTypeEnum enumItem : AppTypeEnum.values()) {
            if (enumItem.getCode().equals(code)) {
                return enumItem;
            }
        }
        // 默认返回阅读器（兼容历史数据）
        return READER;
    }
}