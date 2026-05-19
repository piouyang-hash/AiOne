package org.myfx.controls.aione.UserService.common;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 业务类型枚举（对应user_service_message表的business_type字段）
 * 按业务模块分类，适配多微服务的不同业务类型
 */
@Getter
public enum BusinessTypeEnum {

    // ====================== 用户模块（USER）======================
    /**
     * 用户资料创建（用户注册后创建基本资料）→ 归属：用户注册主事务
     */
    USER_PROFILE_CREATE(1, BusinessModuleEnum.USER, MainBusinessTypeEnum.USER_REGISTER, "用户资料创建"),
    /**
     * 用户手机号绑定（用户绑定手机号）→ 暂未绑定主事务（可根据需求补充）
     */
    USER_PHONE_BIND(2, BusinessModuleEnum.USER, null, "用户手机号绑定"),

    // ====================== 账户模块（ACCOUNT）======================
    /**
     * 账户初始化（用户注册后初始化账户）→ 归属：用户注册主事务
     */
    ACCOUNT_INIT(0, BusinessModuleEnum.ACCOUNT, MainBusinessTypeEnum.USER_REGISTER, "账户初始化"),
    /**
     * 账户充值（用户充值操作）→ 暂未绑定主事务（可根据需求补充）
     */
    ACCOUNT_RECHARGE(1, BusinessModuleEnum.ACCOUNT, null, "账户充值"),
    /**
     * 账户提现（用户提现操作）→ 暂未绑定主事务（可根据需求补充）
     */
    ACCOUNT_WITHDRAW(2, BusinessModuleEnum.ACCOUNT, null, "账户提现"),

    // ====================== 订单模块（ORDER）======================
    /**
     * 订单创建（用户创建订单）→ 暂未绑定主事务（可根据需求补充）
     */
    ORDER_CREATE(0, BusinessModuleEnum.ORDER, null, "订单创建"),
    /**
     * 订单支付（用户支付订单）→ 暂未绑定主事务（可根据需求补充）
     */
    ORDER_PAY(1, BusinessModuleEnum.ORDER, null, "订单支付"),
    /**
     * 订单取消（用户取消订单）→ 暂未绑定主事务（可根据需求补充）
     */
    ORDER_CANCEL(2, BusinessModuleEnum.ORDER, null, "订单取消");

    /**
     * 数据库存储值（对应表的business_type字段）
     */
    @EnumValue
    private final Integer code;
    /**
     * 所属业务模块（关联BusinessModuleEnum，避免跨模块混用type）
     */
    private final BusinessModuleEnum module;
    /**
     * 归属主事务类型（关联MainBusinessTypeEnum，标记子事务所属的核心主事务）
     */
    private final MainBusinessTypeEnum mainBusinessType;
    /**
     * 业务类型描述
     */
    private final String desc;

    // 重载构造方法：适配“有主事务”和“无主事务”的场景
    BusinessTypeEnum(Integer code, BusinessModuleEnum module, MainBusinessTypeEnum mainBusinessType, String desc) {
        this.code = code;
        this.module = module;
        this.mainBusinessType = mainBusinessType;
        this.desc = desc;
    }

    /**
     * 根据「模块+code」获取枚举（核心：解决不同模块type值重复的问题）
     */
    public static BusinessTypeEnum getByModuleAndCode(BusinessModuleEnum module, Integer code) {
        for (BusinessTypeEnum enumItem : values()) {
            if (enumItem.getModule() == module && enumItem.getCode().equals(code)) {
                return enumItem;
            }
        }
        throw new IllegalArgumentException("模块[" + module.getDesc() + "]下无效的业务类型code：" + code);
    }

    /**
     * 新增：根据「主事务+模块+code」获取枚举（精准匹配主-子事务关系）
     */
    public static BusinessTypeEnum getByMainAndModuleAndCode(MainBusinessTypeEnum mainBusinessType, BusinessModuleEnum module, Integer code) {
        for (BusinessTypeEnum enumItem : values()) {
            if (enumItem.getMainBusinessType() == mainBusinessType
                    && enumItem.getModule() == module
                    && enumItem.getCode().equals(code)) {
                return enumItem;
            }
        }
        throw new IllegalArgumentException("主事务[" + mainBusinessType.getDesc() + "]-模块[" + module.getDesc() + "]下无效的业务类型code：" + code);
    }

    /**
     * 简化版：仅根据code获取（慎用，仅适用于单模块场景）
     */
    public static BusinessTypeEnum getByCode(Integer code) {
        for (BusinessTypeEnum enumItem : values()) {
            if (enumItem.getCode().equals(code)) {
                return enumItem;
            }
        }
        throw new IllegalArgumentException("无效的业务类型code：" + code);
    }
}