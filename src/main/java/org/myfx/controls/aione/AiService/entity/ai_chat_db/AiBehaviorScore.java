package org.myfx.controls.aione.AiService.entity.ai_chat_db;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.myfx.controls.aione.AiService.common.ai_chat_db.AiBehaviorEnum;
import org.myfx.controls.aione.AiService.common.ai_chat_db.EmotionTypeEnum;

import java.time.LocalDateTime;

/**
 * AI行为分值配置实体类
 * 映射 ai_behavior_score 表（一个行为对应多维度分值）
 */
@Data
@Schema(description = "AI行为分值配置实体")
public class AiBehaviorScore {

    @Schema(description = "主键ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer id;

    @Schema(description = "关联行为编码", example = "CHAT", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(max = 50, message = "行为编码长度不能超过50")
    private AiBehaviorEnum behaviorCode;

    @Schema(description = "分值类型（枚举）", example = "ACTIVITY", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(max = 30, message = "分值类型长度不能超过30")
    private EmotionTypeEnum scoreType;

    @Schema(description = "变动分值（-100~100）", example = "3", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Min(value = -100, message = "分值不能小于-100")
    @Max(value = 100, message = "分值不能大于100")
    private Integer scoreVal;

    @Schema(description = "创建时间", example = "2026-04-04 10:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;
}