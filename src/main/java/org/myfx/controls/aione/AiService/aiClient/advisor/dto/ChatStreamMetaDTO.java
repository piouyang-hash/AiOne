package org.myfx.controls.aione.AiService.aiClient.advisor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 【前端专用】AI流式对话元数据DTO
 * 作用：流式传输前返回给前端，提前告知消息唯一标识，用于前端准备接收流式消息
 */
@Data
@NoArgsConstructor
@Schema(description = "AI流式对话-前端预接收元数据DTO")
public class ChatStreamMetaDTO {

    @Schema(description = "归属用户ID（0=匿名，>0=登录用户）", example = "1")
    private Integer userId;

    @Schema(description = "流式任务ID（传给前端，让前端可以正确的编排消息）")
    private String taskId;

    // ==================== 消息ID标识（全部String，前端兼容） ====================
    @Schema(description = "雪花ID-用户提问消息ID", example = "1789234567890123456")
    private String userMessageId;

    @Schema(description = "雪花ID-AI回复消息ID", example = "1789234567890123457")
    private String aiMessageId;

    // ==================== 消息内容 ====================
    @NotBlank(message = "用户消息内容不能为空")
    @Schema(description = "用户消息内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "你好，AI能做什么？")
    private String userMessage;

    @Schema(description = "AI完整原始输出文本（流式拼接）", example = "AI完整回复内容")
    private String aiReplyContent;

    // ==================== 【工具类专用】Long → String 赋值Setter ====================
    /**
     * 工具类专用：传入Long类型的用户消息ID，自动转为String赋值
     */
    public void setUserMessageId(Long userMessageId) {
        this.userMessageId = userMessageId != null ? String.valueOf(userMessageId) : null;
    }

    /**
     * 工具类专用：传入Long类型的AI消息ID，自动转为String赋值
     */
    public void setAiMessageId(Long aiMessageId) {
        this.aiMessageId = aiMessageId != null ? String.valueOf(aiMessageId) : null;
    }
}