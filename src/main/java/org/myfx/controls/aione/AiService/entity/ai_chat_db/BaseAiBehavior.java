package org.myfx.controls.aione.AiService.entity.ai_chat_db;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.myfx.controls.aione.AiService.common.ai_chat_db.AiBehaviorEnum;

import java.time.LocalDateTime;

/**
 * 基础AI行为字典表实体类
 * 映射 base_ai_behavior 表（仅存储行为定义，无分数）
 */
@Data
@Schema(description = "基础AI行为字典表实体")
public class BaseAiBehavior {

    @Schema(description = "行为ID（主键）", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer behaviorId;

    @Schema(description = "行为编码（枚举）", example = "CHAT", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(max = 50, message = "行为编码长度不能超过50")
    private AiBehaviorEnum behaviorCode;

    @Schema(description = "行为名称", example = "用户聊天互动", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(max = 30, message = "行为名称长度不能超过30")
    private String behaviorName;

    @Schema(description = "记录创建时间", example = "2026-04-04 10:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "记录更新时间", example = "2026-04-04 11:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;
}