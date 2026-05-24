package org.myfx.controls.aione.AiService.entity.ai_chat_db.order;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.myfx.controls.aione.AiService.common.ai_chat_db.order.OrderTypeEnum;
import org.myfx.controls.aione.AiService.common.ai_chat_db.order.PayStatusEnum;
import org.myfx.controls.aione.AiService.common.ai_chat_db.order.PayTypeEnum;

import java.time.LocalDateTime;

/**
 * AI积分充值订单实体
 * 关联商品表：ai_recharge_goods
 * 移除冗余金额/积分字段，通过goodsId关联商品获取对应数据
 */
@Data
@TableName("ai_recharge_order")
@Schema(description = "AI积分充值订单")
public class AiRechargeOrder {

    @Schema(description = "雪花ID（充值订单ID，填到account_flow的order_id）", example = "1997577082850246656")
    private Long id;

    @Schema(description = "充值用户ID", example = "1001")
    private Integer userId;

    // ===================== 核心新增：关联充值商品ID =====================
    @Schema(description = "充值商品ID（关联ai_recharge_goods表雪花ID）", example = "1000000000000000001")
    private Long goodsId;

    @Schema(description = "业务类型：1-充值", example = "1")
    private OrderTypeEnum businessType;

    @Schema(description = "支付类型：1-微信 2-支付宝", example = "2")
    private PayTypeEnum payType;

    @Schema(description = "订单状态：1-待支付 3-支付成功 5-付款失败 4-已超时 5-已取消", example = "1")
    private PayStatusEnum status;

    @Schema(description = "创建时间", example = "2025-12-07 10:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-12-07 10:05:00")
    private LocalDateTime updateTime;

    @Schema(description = "支付完成时间", example = "2025-12-07 10:01:00")
    private LocalDateTime payTime;

    @Schema(description = "订单备注", example = "支付宝充值")
    private String remark;

    // ===================== 关键修复：非数据库字段，必须标注 =====================
    @TableField(exist = false)
    @Schema(description = "【非数据库字段】更新订单时的原始状态校验", hidden = true)
    private PayStatusEnum originalStatus;

}