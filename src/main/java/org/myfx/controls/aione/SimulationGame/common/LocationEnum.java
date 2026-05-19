package org.myfx.controls.aione.SimulationGame.common;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * 模拟游戏-地点枚举（日常生活场景，地点编码存入数据库，desc为中文描述，用于前端展示/日志说明）
 * 对应表：t_simulate_location 的 location_code 字段
 */
@Getter
public enum LocationEnum {
    // ========== 日常生活核心地点 ==========
    HOME("HOME", "家"),
    SCHOOL("SCHOOL", "学校"),
    RESTAURANT("RESTAURANT", "餐厅"),
    HOSPITAL("HOSPITAL", "医院"),
    COMPANY("COMPANY", "公司");

    /**
     * 地点编码（唯一标识，存入数据库的location_code字段）
     */
    @EnumValue
    private final String code;

    /**
     * 地点描述（中文名称，用于前端展示/日志说明，无需存入数据库）
     */
    private final String desc;

    // 构造方法
    LocationEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // 可选：根据描述反查枚举
    public static LocationEnum getByDesc(String desc) {
        for (LocationEnum location : values()) {
            if (location.getDesc().equals(desc)) {
                return location;
            }
        }
        return null;
    }

    // 核心：根据编码反查枚举
    public static LocationEnum getByCode(String code) {
        for (LocationEnum location : values()) {
            if (location.getCode().equals(code)) {
                return location;
            }
        }
        return null;
    }

    // ============== 新增：获取所有枚举数据（供控制器调用） ==============

    /**
     * 内部VO类：封装枚举的code和desc，用于返回给前端（管理员查看）
     *
     * @param code 地点编码（存入数据库的参考值）
     * @param desc 地点中文描述（前端展示说明）
     */

        public record LocationEnumVO(String code, String desc) {
    }

    /**
     * 静态方法：获取所有地点枚举的结构化数据（供控制器调用）
     * @return 所有枚举的LocationEnumVO列表
     */
    public static List<LocationEnumVO> listAll() {
        List<LocationEnumVO> voList = new ArrayList<>();
        // 遍历所有枚举值，转换为VO对象存入列表
        for (LocationEnum location : values()) {
            voList.add(new LocationEnumVO(location.getCode(), location.getDesc()));
        }
        return voList;
    }
}