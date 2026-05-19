package org.myfx.controls.aione.UserService.common;
import com.baomidou.mybatisplus.annotation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * 服务类型枚举（用户订阅的服务类型）
 */
@Getter
@Schema(description = "服务类型枚举")
public enum ServiceTypeEnum {
    /**
     * AI微服务订阅
     */
    AI_SERVICE(1, "AI微服务"),
    /**
     * 文档服务订阅
     */
    DOC_SERVICE(2, "文档服务"),
    /**
     * 存储服务订阅
     */
    STORAGE_SERVICE(3, "存储服务"),
    /**
     * 预留拓展：其他服务
     */
    OTHER_SERVICE(99, "其他服务");

    /**
     * 数据库存储的code值（@EnumValue标注）
     */
    @EnumValue
    @Schema(description = "服务类型编码")
    private final Integer code;

    /**
     * 枚举描述
     */
    @Schema(description = "服务类型描述")
    private final String desc;

    ServiceTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据code获取枚举
     */
    public static ServiceTypeEnum getByCode(Integer code) {
        for (ServiceTypeEnum enumObj : values()) {
            if (enumObj.getCode().equals(code)) {
                return enumObj;
            }
        }
        return null;
    }
}
