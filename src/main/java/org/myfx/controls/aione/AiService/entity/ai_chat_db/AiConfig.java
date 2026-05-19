package org.myfx.controls.aione.AiService.entity.ai_chat_db;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "AI配置实体")
public class AiConfig {

    @Schema(description = "配置ID（主键）")
    private Integer configId;

    @Schema(description = "用户ID（唯一标识）")
    private Integer userId;

    @Schema(description = "主动聊天模式 0-关闭(默认) 1-开启")
    private Integer activeChatMode;

    @Schema(description = "AI消息切分 0-不切分(默认) 1-切分")
    private Integer splitAiMessage;

    @Schema(description = "记录创建时间")
    private LocalDateTime createTime;

    @Schema(description = "记录更新时间")
    private LocalDateTime updateTime;
}