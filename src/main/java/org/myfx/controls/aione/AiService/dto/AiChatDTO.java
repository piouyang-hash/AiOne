package org.myfx.controls.aione.AiService.dto;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiChatDTO {

    /**
     * 用户ID（非必填）
     * 正常请求：从 UserContext 上下文获取
     * 测试/自主调用：可手动传入覆盖
     */
    @Schema(description = "用户ID（非必填，正常请求从上下文自动获取，仅测试/内部调用手动传入）",
            example = "10086",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer userId;

    /**
     * 会话UUID（标准UUID v4格式）
     */
    @Schema(description = "会话UUID（标准UUID v4格式）",
            example = "3b9e4f9a-8346-4b0f-9d1e-8f7c6a5b4d3e",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String sessionUuid;

    // 暂时不是必须的，因为我还有普通的接口需要使用，新写的web和redis组成的代码我正在写
    @Schema(description = "流式任务ID（每发送一条消息，自动生成新ID）",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String taskId;

    @NotBlank(message = "对话消息不能为空")
    @Parameter(description = "用户对话消息内容", required = true)
    private String message;

    // 🔥 新增：非必填 角色ID，无任何校验
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