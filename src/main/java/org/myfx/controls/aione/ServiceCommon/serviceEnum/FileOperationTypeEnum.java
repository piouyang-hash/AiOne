package org.myfx.controls.aione.ServiceCommon.serviceEnum;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 文件操作类型枚举
 * code：1=文件上传 2=文件删除（存入数据库的TINYINT值）
 * desc：操作描述（用于前端展示/日志说明）
 */
@Getter
public enum FileOperationTypeEnum {

    /** 文件上传 */
    UPLOAD(1, "文件上传"),

    /** 文件删除 */
    DELETE(2, "文件删除");

    /** 操作类型编码（存入数据库的TINYINT值） */
    @EnumValue
    private final int code;

    /** 操作描述（用于前端展示/日志说明） */
    private final String desc;

    // 构造方法
    FileOperationTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据code获取枚举（数据库值转换枚举）
     */
    public static FileOperationTypeEnum getByCode(int code) {
        for (FileOperationTypeEnum type : values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的文件操作类型编码：" + code);
    }

    /**
     * 根据描述获取枚举
     */
    public static FileOperationTypeEnum getByDesc(String desc) {
        for (FileOperationTypeEnum type : values()) {
            if (type.getDesc().equals(desc)) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的文件操作类型描述：" + desc);
    }
}