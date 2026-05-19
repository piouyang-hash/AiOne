package org.myfx.controls.aione.AiService.dto.redis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * AI对话队列任务实体
 * （仅存储队列中需要的核心数据，userId/sessionUuid 存在Redis键中）
 */
@Data
@Schema(description = "AI对话队列任务实体")
public class AiChatQueueTask {

    @Schema(description = "流式任务ID（每发送一条消息，自动生成新ID）",
            example = "3b9e4f9a-8346-4b0f-9d1e-8f7c6a5b4d3e",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String taskId;

    @Schema(description = "用户对话消息内容",
            example = "你好，请帮我写一个方案",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String message;

    @Schema(description = "角色ID（非必填，为空默认使用1）",
            example = "1",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer roleId;

    // 🔥 新增：用户发送消息的时间戳（Long类型，毫秒级时间戳）
    @Schema(description = "用户发送消息的时间戳（毫秒级）",
            example = "1775566794837",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long userSendTimestamp;

    @Schema(description = "雪花ID-用户提问消息ID，作为后端数据流通参数，前端无需传入",
            example = "1789234567890123456")
    private Long userMessageId;

    @Schema(description = "是否为AI主动消息", example = "true")
    private Boolean isActiveMessage;

}