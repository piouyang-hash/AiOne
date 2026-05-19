package org.myfx.controls.aione.AiService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.myfx.controls.aione.AiService.common.ai_chat_db.AiBehaviorEnum;
import org.myfx.controls.aione.AiService.common.ai_chat_db.EmotionTypeEnum;

/**
 * AI行为分值配置 新增DTO
 * 用于：管理员新增【行为 + 分值类型 + 变动分值】配置
 * 对应表：ai_behavior_score
 */
@Data
@Schema(description = "AI行为分值配置-新增请求参数")
public class AiBehaviorScoreAddDTO {

    @Schema(
            description = "AI行为编码（枚举）",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "USER_SEND_MSG"
    )
    @NotNull(message = "AI行为编码不能为空")
    private AiBehaviorEnum aiBehaviorCode;

    @Schema(
            description = "情绪/分值类型（枚举）",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "ACTIVITY"
    )
    @NotNull(message = "分值类型不能为空")
    private EmotionTypeEnum scoreType;

    @Schema(
            description = "变动分值（-100~100）；传null=动态计算（如离线超时扣分）；传0=不变动；非null则必须在范围内",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "-20"
    )
    @Min(value = -100, message = "分值不能小于 -100")
    @Max(value = 100, message = "分值不能大于 100")
    private Integer scoreVal;
}