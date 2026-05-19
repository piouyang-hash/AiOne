package org.myfx.controls.aione.AiService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI聊天流式分片数据DTO，封装单条流式输出片段信息")
public class ChatChunkDTO {

    @Schema(
            description = "会话UUID（仅首帧传递，后续分片为null）",
            example = "123e4567-e89b-12d3-a456-426614174000",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String sessionUuid;

    @Schema(
            description = "AI流式输出的文本片段",
            example = "你好，有什么我可以帮助你的吗？",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Object content;

    @Schema(
            description = "是否为第一个分片（前端识别首帧的标识）",
            example = "true",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Boolean isFirst;

    @Schema(
            description = "是否为结束帧（标记流式输出完成）",
            example = "false",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Boolean isEnd;

    // 🔥 新增：错误标识（前端核心判断字段）
    @Schema(
            description = "是否为错误消息",
            example = "false",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private boolean isError = false; // 默认非错误

    // ===================== 适配你原有代码的构造函数（字段名严格对齐） =====================
    /**
     * 构造函数1：正常消息 / 结束包（兼容你原有调用）
     */
    public ChatChunkDTO(String sessionUuid, Object content, Boolean isFirst, Boolean isEnd) {
        this.sessionUuid = sessionUuid;
        this.content = content;
        this.isFirst = isFirst;
        this.isEnd = isEnd;
        this.isError = false;
    }

}