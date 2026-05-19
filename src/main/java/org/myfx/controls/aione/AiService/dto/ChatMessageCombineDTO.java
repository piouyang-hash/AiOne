package org.myfx.controls.aione.AiService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.myfx.controls.aione.AiService.common.ai_chat_db.ChatRoleEnum;

/**
 * 合并版-用户消息+AI回复 DTO
 * 一次请求完成用户消息+AI回复的入库（事务管控）
 */
@Data
@Schema(name = "ChatMessageCombineDTO", description = "用户消息+AI回复合并请求参数")
public class ChatMessageCombineDTO {

    /**
     * 用户ID（登录用户）
     */
    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID（登录用户）", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer userId;

    /**
     * 会话ID（雪花ID）
     */
    @NotNull(message = "会话ID不能为空")
    @Schema(description = "会话ID（雪花ID）", example = "1789234567890123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long sessionId;

    /**
     * 用户消息内容
     */
    @NotBlank(message = "用户消息内容不能为空")
    @Schema(description = "用户消息内容", example = "你好，AI能做什么？", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userMessage;

    /**
     * AI回复内容
     */
    @NotBlank(message = "AI回复内容不能为空")
    @Schema(description = "AI回复内容", example = "我能解答问题、编写代码等～", requiredMode = Schema.RequiredMode.REQUIRED)
    private String aiReplyContent;

    /**
     * AI回复切分后的JSON内容（新增）
     */
    @Schema(description = "AI回复切分后的JSON内容", example = "{\"total\":2,\"segments\":{\"1\":\"Flux是Reactor核心\",\"2\":\"[DONE]\"}}")
    private String splitContentJson;

    /**
     * AI回复切分后的最后一段内容（新增）
     */
    @Schema(description = "AI回复切分后的最后一段内容", example = "[DONE]")
    private String lastSegment;

    /**
     * 消息角色
     */
    @NotNull(message = "消息角色不能为空")
    @Schema(description = "消息角色", example = "USER", requiredMode = Schema.RequiredMode.REQUIRED)
    private ChatRoleEnum role;
}