package org.myfx.controls.aione.AiService.vo.order;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.myfx.controls.aione.AiService.common.ai_chat_db.order.OrderTypeEnum;
import org.myfx.controls.aione.AiService.common.ai_chat_db.order.PayStatusEnum;
import org.myfx.controls.aione.AiService.common.ai_chat_db.order.PayTypeEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI充值订单创建成功 返回VO
 * 前端需要展示的核心字段
 */
@Data
@Schema(description = "AI充值订单创建响应VO")
public class AiRechargeOrderDetailVO {

    @Schema(description = "订单ID", example = "1234567890123456789")
    private String id;

    @Schema(description = "充值商品ID", example = "1000000000000000001")
    private String goodsId;

    @Schema(description = "充值金额(元)", example = "5.00")
    private BigDecimal amount;

    @Schema(description = "到账AI积分", example = "5")
    private String point;

    @Schema(description = "业务类型", example = "充值")
    private String businessType;

    @Schema(description = "支付方式", example = "微信/支付宝")
    private String payType;

    @Schema(description = "订单状态", example = "待支付/支付成功/支付失败/已过期")
    private String status;

    @Schema(description = "创建时间（默认当前时间）", example = "2025-12-07 10:00:00")
    private LocalDateTime createTime;

    // ===================== 核心：ID格式化 Set 方法 =====================
    // 专门处理 Long 转 String，解决前端精度丢失问题
    public void setId(Long id) {
        this.id = id == null ? null : id.toString();
    }

    public void setPoint(Long point) {
        if (point == null) {
            this.point = "0.000000";
            return;
        }
        // 直接除以100万，必定整除
        BigDecimal result = new BigDecimal(point).divide(new BigDecimal("1000000"));
        // 转普通字符串，无科学计数法
        this.point = result.toPlainString();
    }

    // ===================== 新增：商品ID格式化 =====================
    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId == null ? null : goodsId.toString();
    }

    // ===================== 枚举类型 Set 方法（自动转中文） =====================
    public void setBusinessType(OrderTypeEnum businessType) {
        this.businessType = businessType == null ? "" : businessType.getDesc();
    }

    public void setPayType(PayTypeEnum payType) {
        this.payType = payType == null ? "微信/支付宝" : payType.getDesc();
    }

    public void setStatus(PayStatusEnum status) {
        this.status = status == null ? "" : status.getDesc();
    }

}