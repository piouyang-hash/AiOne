package org.myfx.controls.aione.AiService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AiInternalChatDTO {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID（内部调用，直接传值）", example = "10086", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer userId;

    @NotNull(message = "会话ID不能为空")
    @Schema(description = "会话ID", example = "9527", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long sessionId;

    @NotBlank(message = "用户消息内容不能为空")
    @Schema(description = "用户对话消息", example = "帮我生成今日待办提醒", requiredMode = Schema.RequiredMode.REQUIRED)
    private String message;
}