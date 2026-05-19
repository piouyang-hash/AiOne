package org.myfx.controls.aione.AiService.entity.ai_chat_db.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.myfx.controls.aione.AiService.common.ai_chat_db.order.OrderTypeEnum;
import org.myfx.controls.aione.AiService.common.ai_chat_db.order.PayStatusEnum;
import org.myfx.controls.aione.AiService.common.ai_chat_db.order.PayTypeEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "充值记录")
public class AiRechargeOrder {

    @Schema(description = "雪花ID（充值订单ID，填到account_flow的order_id）", example = "1997577082850246656")
    private Long id;

    @Schema(description = "充值用户ID", example = "1001")
    private Integer userId;

    @Schema(description = "充值金额", example = "100.00")
    private BigDecimal amount;

    @Schema(description = "充值到账的AI积分", example = "1000")
    private Long point;

    @Schema(description = "业务类型：1充值", example = "1")
    private OrderTypeEnum businessType;

    @Schema(description = "支付类型：1微信 2支付宝（禁止余额充值余额）", example = "1")
    private PayTypeEnum payType;

    @Schema(description = "状态：0待支付 1支付成功 2支付失败 3过期", example = "1")
    private PayStatusEnum status;

    @Schema(description = "创建时间（默认当前时间）", example = "2025-12-07 10:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间（自动刷新）", example = "2025-12-07 10:05:00")
    private LocalDateTime updateTime;

    @Schema(description = "支付完成时间", example = "2025-12-07 10:01:00")
    private LocalDateTime payTime;

    @Schema(description = "充值备注", example = "微信充值100元")
    private String remark;
}
