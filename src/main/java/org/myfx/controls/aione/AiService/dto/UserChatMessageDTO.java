package org.myfx.controls.aione.AiService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.myfx.controls.aione.AiService.common.ai_chat_db.ChatRoleEnum;

/**
 * 用户发送消息DTO
 * 用于接收前端传入的用户消息参数
 */
@Data
@Schema(name = "UserChatMessageDTO", description = "用户发送消息请求参数")
public class UserChatMessageDTO {

    /**
     * 会话ID（雪花ID）
     */
    @NotNull(message = "会话ID不能为空")
    @Schema(description = "会话ID（雪花ID）", example = "1789234567890123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long sessionId;

    /**
     * 用户消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    @Schema(description = "用户消息内容", example = "你好，AI能做什么？", requiredMode = Schema.RequiredMode.REQUIRED)
    private String message;

    /**
     * 消息角色（仅支持USER(1)=用户、SPRINGBOOT(3)=SpringBoot系统）
     */
    @NotNull(message = "消息角色不能为空")
    @Schema(description = "消息角色（USER=用户、SPRINGBOOT=SpringBoot系统）", example = "USER", requiredMode = Schema.RequiredMode.REQUIRED)
    private ChatRoleEnum role;
}