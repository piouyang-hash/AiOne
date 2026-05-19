package org.myfx.controls.aione.AiService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 会话ID解析DTO
 * 用于存储从conversationId字符串中解析出的用户ID和会话ID
 */
@Data // Lombok注解，自动生成get/set/toString/equals/hashCode等方法
@Schema(description = "会话ID解析DTO：存储解析后的用户ID和会话ID")
public class ConversationIdDTO {

    @Schema(description = "用户ID", example = "1001")
    private Integer userId;

    @Schema(description = "会话ID", example = "123456789")
    private long sessionId;
}