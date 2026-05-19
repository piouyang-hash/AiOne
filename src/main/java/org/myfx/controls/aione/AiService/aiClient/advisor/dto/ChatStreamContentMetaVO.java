package org.myfx.controls.aione.AiService.aiClient.advisor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * AI 聊天流式响应 元数据VO
 * （首条消息发送，用于前端消息编排、任务绑定）
 */
@Data
@Schema(description = "AI 聊天流式响应元数据")
public class ChatStreamContentMetaVO {

    @Schema(description = "流式任务ID（前端用于消息编排、绑定任务）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String taskId;

    @Schema(description = "AI 完整原始输出文本（流式最终拼接结果）", example = "你好！我是AI助手")
    private String aiReplyContent;
}