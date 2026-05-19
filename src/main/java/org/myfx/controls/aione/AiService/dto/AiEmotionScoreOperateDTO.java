package org.myfx.controls.aione.AiService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.myfx.controls.aione.AiService.common.ai_chat_db.EmotionTypeEnum;

/**
 * AI心情分数操作DTO
 * 统一封装「AI心情分值更新」和「情绪变动明细新增」的核心参数
 */
@Data
@Schema(description = "AI心情分数操作DTO（分值更新/情绪变动明细新增共用）")
public class AiEmotionScoreOperateDTO {

    @Schema(description = "用户ID", example = "1001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用户ID不能为空")
    @Positive(message = "用户ID需为正整数")
    private Integer userId;

    @Schema(description = "AI行为ID（关联base_ai_behavior表主键）", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "AI行为ID不能为空")
    @Positive(message = "AI行为ID需为正整数")
    private Integer aiBehaviorId;

    @Schema(description = "情绪类型（如开心/难过/平静等）", example = "HAPPY", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "情绪类型不能为空")
    private EmotionTypeEnum emotionType;

    @Schema(description = "本次情绪变动分值（可正可负）", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "情绪变动分值不能为空")
    @Min(value = -100, message = "情绪变动分值不能小于-100")
    @Max(value = 100, message = "情绪变动分值不能大于100")
    private Integer addScore;

    @Schema(description = "变动前的心情分值（仅明细新增时必填）", example = "50", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Min(value = -100, message = "变动前分值不能小于-100")
    @Max(value = 100, message = "变动前分值不能大于100")
    private Integer scoreBefore;
}