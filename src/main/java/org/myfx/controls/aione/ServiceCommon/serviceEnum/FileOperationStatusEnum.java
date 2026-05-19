package org.myfx.controls.aione.ServiceCommon.serviceEnum;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 文件操作状态枚举
 * code：1=成功 2=失败（存入数据库的TINYINT值）
 * desc：状态描述（用于前端展示/日志说明）
 */
@Getter
public enum FileOperationStatusEnum {

    /** 操作成功 */
    SUCCESS(1, "成功"),

    /** 操作失败 */
    FAIL(2, "失败");

    /** 操作状态编码（存入数据库的TINYINT值） */
    @EnumValue
    private final int code;

    /** 状态描述（用于前端展示/日志说明） */
    private final String desc;

    // 构造方法
    FileOperationStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据code获取枚举（数据库值转换枚举）
     */
    public static FileOperationStatusEnum getByCode(int code) {
        for (FileOperationStatusEnum status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的文件操作状态编码：" + code);
    }

    /**
     * 根据描述获取枚举
     */
    public static FileOperationStatusEnum getByDesc(String desc) {
        for (FileOperationStatusEnum status : values()) {
            if (status.getDesc().equals(desc)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的文件操作状态描述：" + desc);
    }
}