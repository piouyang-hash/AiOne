package org.myfx.controls.aione.AiService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * AI 对话会话 - 未读消息数更新 请求DTO
 */
@Data
@Schema(description = "更新会话未读消息数请求参数")
public class AiChatSessionUnreadUpdateDTO {

    @Schema(description = "对话会话UUID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
    private String sessionUuid;

    @Schema(description = "非切分模式未读消息数（传null则不更新）", example = "5")
    private Integer normalUnreadCount;

    @Schema(description = "切分模式未读消息数（传null则不更新）", example = "3")
    private Integer splitUnreadCount;
}