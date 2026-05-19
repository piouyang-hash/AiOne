package org.myfx.controls.aione.UserService.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 微服务间调用打赏接口的请求体DTO
 */
@Data
@Schema(description = "打赏接口请求参数（微服务内部调用）")
public class RewardRequestDTO {

    @Schema(description = "打赏金额（仅支持5、10、15）", example = "10", requiredMode =  Schema.RequiredMode.REQUIRED)
    private Integer amount;

    @Schema(description = "打赏留言（可选）", example = "支持作者！")
    private String message;

    @Schema(description = "调用方微服务名称（用于日志追溯）", example = "order-service", requiredMode =  Schema.RequiredMode.REQUIRED)
    private String callerServiceName;
}