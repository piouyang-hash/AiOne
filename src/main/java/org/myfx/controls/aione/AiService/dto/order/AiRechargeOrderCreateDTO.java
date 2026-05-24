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
            description = "充值商品ID（雪花ID）",
            example = "1000000000000000001",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "充值商品ID不能为空")
    private Long goodsId;

}