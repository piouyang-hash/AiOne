package org.myfx.controls.aione.AiService.common.ai_chat_db;

import lombok.Getter;

/**
 * 积分充值档位枚举
 * 定义系统支持的固定充值金额档位
 */
@Getter
public enum PointRechargeTierEnum {
    // ========== 积分充值档位 ==========
    TIER_5_YUAN(5, "5元充值档位"),
    TIER_10_YUAN(10, "10元充值档位"),
    TIER_20_YUAN(20, "20元充值档位"),
    TIER_50_YUAN(50, "50元充值档位"),
    TIER_100_YUAN(100, "100元充值档位"),
    TIER_200_YUAN(200, "200元充值档位");

    /**
     * 充值金额（单位：元，固定档位）
     */
    private final Integer amount;

    /**
     * 档位描述（中文名称，用于前端展示/订单说明）
     */
    private final String desc;

    // 构造方法（金额+描述）
    PointRechargeTierEnum(Integer amount, String desc) {
        this.amount = amount;
        this.desc = desc;
    }

    /**
     * 根据描述反查枚举
     */
    public static PointRechargeTierEnum getByDesc(String desc) {
        for (PointRechargeTierEnum tier : values()) {
            if (tier.getDesc().equals(desc)) {
                return tier;
            }
        }
        return null;
    }

    /**
     * 根据金额反查枚举（核心匹配方法）
     */
    public static PointRechargeTierEnum getByAmount(Integer amount) {
        for (PointRechargeTierEnum tier : values()) {
            if (tier.getAmount().equals(amount)) {
                return tier;
            }
        }
        return null;
    }

    /**
     * 校验金额是否为合法充值档位
     */
    public static boolean isValidAmount(Integer amount) {
        return getByAmount(amount) != null;
    }
}