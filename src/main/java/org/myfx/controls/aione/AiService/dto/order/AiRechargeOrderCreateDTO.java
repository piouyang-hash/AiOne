package org.myfx.controls.aione.AiService.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

/**
 * AI充值订单创建DTO（前端传参专用）
 * 仅包含前端可填写/选择的字段，后端做参数校验+补全其他字段
 */
@Data
@Schema(description = "AI充值订单创建请求参数")
public class AiRechargeOrderCreateDTO {  // 已改名：更符合业务命名规范

    @Schema(
            description = "充值金额（元），必须大于0，保留两位小数",
            example = "100.00",
            requiredMode = Schema.RequiredMode.REQUIRED // 标记为必填
    )
    @NotNull(message = "充值金额不能为空") // 非空校验
    @DecimalMin(value = "0.01", message = "充值金额必须大于0") // 金额最小0.01元
    private BigDecimal amount;

    @Schema(
            description = "充值备注（可选），比如“充值买书”",
            example = "充值购买电子书",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED // 非必填
    )
    private String remark; // 前端可选填，后端自动过滤敏感字符后入库
}