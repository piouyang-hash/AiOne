package org.myfx.controls.aione.AiService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 清空会话未读消息数 请求DTO
 */
@Data
@Schema(description = "清空AI对话会话未读消息数 请求参数")
public class ChatSessionUnreadClearDTO {

    @NotBlank(message = "会话UUID不能为空")
    @Schema(description = "对话会话UUID（标准UUID v4格式）", requiredMode = Schema.RequiredMode.REQUIRED, example = "123e4567-e89b-12d3-a456-426614174000")
    private String sessionUuid;

    @Schema(description = "是否在会话中（布尔标识，暂不使用）", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "true")
    private Boolean inSession;
}