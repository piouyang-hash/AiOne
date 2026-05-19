package org.myfx.controls.aione.ServiceCommon.serviceEnum;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * TODO：数据库层面还没有修改，有时间去修改，需要修改成TINYINT，切面中的也要修改，兼容枚举类
 * 角色枚举
 * code：1=管理员，2=用户，3=游客
 * desc：角色描述（用于前端展示/日志说明）
 */
@Getter // 自动生成getter方法，方便获取code和desc
public enum RoleEnum {

    /** 管理员 */
    ADMIN(1, "管理员"),

    /** 用户 */
    USER(2, "用户"),

    /** 游客 */
    GUEST(3, "游客");

    /** 角色编码（存入数据库的TINYINT值） */
    @EnumValue // 标记该字段映射到数据库的role字段
    private final int code;

    /** 角色描述（用于前端展示/日志说明） */
    private final String desc;

    // 构造方法
    RoleEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // 扩展方法：根据code获取枚举（常用，比如从数据库查值后转换）
    public static RoleEnum getByCode(int code) {
        for (RoleEnum role : values()) {
            if (role.getCode() == code) {
                return role;
            }
        }
        throw new IllegalArgumentException("无效的角色编码：" + code);
    }

    // 扩展方法：根据desc模糊匹配（可选，按需使用）
    public static RoleEnum getByDesc(String desc) {
        for (RoleEnum role : values()) {
            if (role.getDesc().equals(desc)) {
                return role;
            }
        }
        throw new IllegalArgumentException("无效的角色描述：" + desc);
    }
}