package org.myfx.controls.aione.UserService.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.myfx.controls.aione.UserService.common.ServiceTypeEnum;
import org.myfx.controls.aione.UserService.common.SubscriptionStatusEnum;
import org.myfx.controls.aione.UserService.common.SubscriptionTypeEnum;

import java.time.LocalDateTime;

/**
 * 用户订阅实体类
 */
@Data
@Schema(description = "用户订阅实体")
public class UserSubscription {
    /**
     * 订阅记录ID
     */
    @Schema(description = "订阅记录ID", example = "1")
    private Long id;

    /**
     * 关联用户表的用户ID
     */
    @Schema(description = "用户ID", example = "10001")
    private Integer userId;

    /**
     * 服务类型（1-AI微服务，2-文档服务，3-存储服务）
     */
    @Schema(description = "服务类型", example = "1")
    private ServiceTypeEnum serviceType;

    /**
     * 订阅类型（1-月卡，2-年卡，3-终身卡，4-体验卡）
     */
    @Schema(description = "订阅类型", example = "2")
    private SubscriptionTypeEnum subscriptionType;

    /**
     * 订阅状态（1-生效中，2-已过期，3-暂停，4-未激活，5-已取消）
     */
    @Schema(description = "订阅状态", example = "1")
    private SubscriptionStatusEnum subscriptionStatus;

    /**
     * 支付订单号（关联订单微服务的订单ID）
     */
    @Schema(description = "支付订单号", example = "9876543210")
    private Long payOrderId;

    /**
     * 订阅生效时间
     */
    @Schema(description = "订阅生效时间", example = "2026-01-01 00:00:00")
    private LocalDateTime effectiveTime;

    /**
     * 订阅过期时间（终身卡填9999-12-31 23:59:59）
     */
    @Schema(description = "订阅过期时间", example = "2027-01-01 00:00:00")
    private LocalDateTime expireTime;

    /**
     * 记录创建时间
     */
    @Schema(description = "记录创建时间", example = "2026-01-01 10:00:00")
    private LocalDateTime createTime;

    /**
     * 记录更新时间
     */
    @Schema(description = "记录更新时间", example = "2026-01-01 10:00:00")
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "2026年1月续费AI年卡")
    private String remark;
}