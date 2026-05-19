package org.myfx.controls.aione.AiService.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 支付宝订单支付参数 DTO
 */
@Data
@Schema(description = "支付宝电脑网站支付请求参数")
public class AlipayPayDTO {

    @Schema(description = "商户唯一订单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "20240520123456789")
    private String outTradeNo;

    @Schema(description = "订单标题/商品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "Vue-APP支付测试")
    private String subject;

    @Schema(description = "订单总金额(元)", requiredMode = Schema.RequiredMode.REQUIRED, example = "0.01")
    private BigDecimal totalAmount;

    @Schema(description = "订单描述/商品详情", example = "这是商品的详细描述信息")
    private String body;

    @Schema(description = "订单超时时间", example = "15m", defaultValue = "15m")
    private String timeoutExpress = "15m";
}