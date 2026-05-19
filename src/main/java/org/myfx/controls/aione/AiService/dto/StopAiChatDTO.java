package org.myfx.controls.aione.AiService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI流式对话 停止请求DTO
 */
@Data
@Schema(description = "停止AI流式对话请求参数")
public class StopAiChatDTO {

    @NotBlank(message = "会话UUID不能为空")
    @Schema(description = "会话唯一标识UUID", required = true, example = "a1b2c3d4-5678-90ef-ghij-klmnopqrstuv")
    private String sessionUuid;

    @NotBlank(message = "任务ID不能为空")
    @Schema(description = "流式任务ID", required = true, example = "task_123456")
    private String taskId;
}