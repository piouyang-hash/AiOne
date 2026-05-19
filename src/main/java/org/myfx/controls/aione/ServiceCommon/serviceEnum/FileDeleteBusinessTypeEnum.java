package org.myfx.controls.aione.ServiceCommon.serviceEnum;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 文件删除业务类型枚举（映射数据库business_type的数字编码）
 */
@Getter // 生成getter方法，便于获取编码和描述
public enum FileDeleteBusinessTypeEnum {
    // 枚举项：编码 + 描述
    PRIVATE_BOOK(1, "私有书籍"),
    PUBLIC_BOOK(2, "共有书籍"),
    USER_AVATAR(3, "用户头像"),
    PUBLIC_BOOK_COVER(4, "共有书籍封面"),
    TEST(5, "测试数据库");

    // 数据库中存储的数字编码（@EnumValue标记映射字段）
    @EnumValue
    private final int code;
    // 业务类型描述（前端/日志展示用）
    private final String desc;

    // 构造方法
    FileDeleteBusinessTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // 可选：根据编码获取枚举（便于代码中转换）
    public static FileDeleteBusinessTypeEnum getByCode(int code) {
        for (FileDeleteBusinessTypeEnum type : values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的业务类型编码：" + code);
    }
}
