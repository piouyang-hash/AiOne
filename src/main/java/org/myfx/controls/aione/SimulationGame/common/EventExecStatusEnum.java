package org.myfx.controls.aione.SimulationGame.common;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 模拟游戏-事件执行状态枚举
 * 对应表中exec_status字段：EXECUTING-执行中/FINISHED-已完成/FAILED-失败
 */
@Getter
public enum EventExecStatusEnum {
    // ========== 事件执行状态 ==========
    EXECUTING("EXECUTING", "执行中"),
    FINISHED("FINISHED", "已完成"), // 数据库默认值
    FAILED("FAILED", "失败"),
    INTERRUPTED("INTERRUPTED", "中断"); // 新增中断状态

    /**
     * 状态编码（唯一标识，存入数据库的exec_status字段）
     */
    @EnumValue // MyBatis-Plus枚举注解，标识存入数据库的字段值
    private final String code;

    /**
     * 状态描述（中文名称，用于日志说明/业务提示）
     */
    private final String desc;

    // 构造方法
    EventExecStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @JsonValue  // 告诉 Jackson 序列化时只取这个值
    public String getCode() {
        return code;
    }

    /**
     * 根据描述反查枚举（方便业务层根据中文描述匹配状态）
     */
    public static EventExecStatusEnum getByDesc(String desc) {
        for (EventExecStatusEnum status : values()) {
            if (status.getDesc().equals(desc)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 核心：根据编码反查枚举（从数据库取出exec_status后，转换为对应的枚举对象）
     */
    public static EventExecStatusEnum getByCode(String code) {
        for (EventExecStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}