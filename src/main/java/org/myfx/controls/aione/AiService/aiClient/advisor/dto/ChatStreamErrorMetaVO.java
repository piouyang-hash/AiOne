package org.myfx.controls.aione.AiService.aiClient.advisor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * AI 流式错误响应元数据（对标 ChatStreamContentMetaVO）
 */
@Data
@Schema(description = "AI 流式错误响应元数据")
public class ChatStreamErrorMetaVO {

    @Schema(description = "流式任务ID（前端用于绑定错误消息、终止流式编排）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String taskId;

    @Schema(description = "错误类型（异常类名）", example = "TokenInsufficientException")
    private String errorType;

    @Schema(description = "错误描述信息", example = "用户Token余额不足，无法发起AI对话")
    private String errorMsg;

}