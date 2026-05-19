package org.myfx.controls.aione.AiService.common.ai_chat_db.order;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

import java.io.Serializable;

/**
 * 付款状态枚举类
 * 通用付款状态：等待付款、付款成功、付款失败
 */
@Getter
public enum PayStatusEnum implements Serializable {
    /**
     * 等待选择支付方式（订单创建后默认状态）
     */
    WAIT_SELECT_PAYMENT(0, "等待选择支付方式"),

    /**
     * 等待付款（待支付）（恢复为原code=1）
     */
    WAIT_PAY(1, "等待付款"),

    /**
     * Try阶段已冻结（新增：TCC Try成功，金额已冻结，待系统确认进入付款环节）（顺延为2）
     */
    TRY_FROZEN(2, "Try阶段已冻结"),

    /**
     * 付款成功（原code=2 → 顺延为3）
     */
    PAY_SUCCESS(3, "付款成功"),

    /**
     * 订单超时（待支付状态下超时未付款）（原code=3 → 顺延为4）
     */
    ORDER_TIMEOUT(4, "订单超时"),

    /**
     * 付款失败（支付渠道返回失败）（原code=4 → 顺延为5）
     */
    PAY_FAIL(5, "付款失败"),

    /**
     * 付款错误（服务器异常/事务回滚导致的失败）（原code=5 → 顺延为6）
     */
    PAY_ERROR(6, "付款错误");

    /**
     * 序列化版本号（避免序列化异常）
     */
    private static final long serialVersionUID = 1L;

    /**
     * 状态编码（数据库存储值）
     */
    @EnumValue
    private final Integer code;

    /**
     * 状态描述（前端展示/日志说明）
     */
    private final String desc;

    /**
     * 构造方法
     */
    PayStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据编码获取枚举实例（通用工具方法）
     */
    public static PayStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PayStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    // 新增：按name转换（解决"TRY_FROZEN"的转换问题）
    public static PayStatusEnum getByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("PayStatusEnum name不能为空");
        }
        for (PayStatusEnum status : values()) {
            if (status.name().equals(name.trim())) { // 匹配枚举的name()值
                return status;
            }
        }
        throw new IllegalArgumentException("无效的PayStatusEnum name：" + name);
    }

}