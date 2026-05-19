package org.myfx.controls.aione.AiService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * AI回复消息DTO
 * 用于接收前端/内部传入的AI回复消息参数
 */
@Data
@Schema(name = "AiReplyMessageDTO", description = "AI回复消息请求参数")
public class AiReplyMessageDTO {

    /**
     * 会话ID（雪花ID）
     */
    @NotNull(message = "会话ID不能为空")
    @Schema(description = "会话ID（雪花ID）", example = "1789234567890123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long sessionId;

    /**
     * AI回复内容
     */
    @NotBlank(message = "AI回复内容不能为空")
    @Schema(description = "AI回复内容", example = "我能解答问题、编写代码等～", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    /**
     * 父消息ID（关联对应的用户消息ID）
     */
    @NotNull(message = "父消息ID不能为空")
    @Schema(description = "父消息ID（关联对应的用户消息ID）", example = "1789234567890123457", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long parentMsgId;
}
