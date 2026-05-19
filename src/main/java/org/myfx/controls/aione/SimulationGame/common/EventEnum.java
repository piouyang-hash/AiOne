package org.myfx.controls.aione.SimulationGame.common;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * 模拟游戏-事件枚举（日常生活场景，事件编码存入数据库，desc为中文描述，用于前端展示/日志说明）
 * 对应表：t_simulate_event 的 event_code 字段
 */
@Getter
public enum EventEnum {
    // ========== 日常生活核心事件 ==========
    SLEEP("SLEEP", "睡觉"),
    ATTEND_CLASS("ATTEND_CLASS", "上课"),
    WAITING("WAITING", "等待中"),
    HAVE_MEAL("HAVE_MEAL", "吃饭"),
    EARN_MONEY("EARN_MONEY", "赚钱"),
    RECEIVE_TREATMENT("RECEIVE_TREATMENT", "治疗");

    /**
     * 事件编码（唯一标识，存入数据库的event_code字段）
     */
    @EnumValue
    private final String code;

    /**
     * 事件描述（中文名称，用于前端展示/日志说明，无需存入数据库）
     */
    private final String desc;

    // 构造方法
    EventEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // 可选：根据描述反查枚举（方便业务层根据中文描述匹配事件）
    public static EventEnum getByDesc(String desc) {
        for (EventEnum event : values()) {
            if (event.getDesc().equals(desc)) {
                return event;
            }
        }
        return null;
    }

    // 核心：根据编码反查枚举（从数据库取出event_code后，转换为对应的枚举对象）
    public static EventEnum getByCode(String code) {
        for (EventEnum event : values()) {
            if (event.getCode().equals(code)) {
                return event;
            }
        }
        return null;
    }

    // ============== 新增：获取所有枚举数据（供控制器调用） ==============

    /**
     * 内部VO类：封装枚举的code和desc，用于返回给前端（管理员查看）
     * 静态内部类，无需额外创建外部类，简洁高效
     *
     * @param code 事件编码（存入数据库的参考值）
     * @param desc 事件中文描述（前端展示说明）
     */
    public record EventEnumVO(String code, String desc) {
    }

    /**
     * 静态方法：获取所有事件枚举的结构化数据（供控制器调用）
     * @return 所有枚举的EventEnumVO列表
     */
    public static List<EventEnumVO> listAll() {
        List<EventEnumVO> voList = new ArrayList<>();
        // 遍历所有枚举值，转换为VO对象存入列表
        for (EventEnum event : values()) {
            voList.add(new EventEnumVO(event.getCode(), event.getDesc()));
        }
        return voList;
    }
}