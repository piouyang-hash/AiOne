package org.myfx.controls.aione.AiService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.myfx.controls.aione.AiService.common.user_behavior_db.FeatureTypeEnum;

/**
 * 用户特征分值操作DTO
 * 统一封装「特征分值更新」和「行为加分明细新增」的核心参数
 */
@Data
@Schema(description = "用户特征分值操作DTO（分值更新/明细新增共用）")
public class UserFeatureScoreOperateDTO {

    @Schema(description = "用户ID", example = "1001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用户ID不能为空")
    @Positive(message = "用户ID需为正整数")
    private Integer userId;

    @Schema(description = "行为ID（关联base_user_behavior表，仅明细新增时必填）", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Positive(message = "行为ID需为正整数")
    private Integer behaviorId;

    @Schema(description = "特征类型（活跃度/喜爱度/熟悉度）", example = "ACTIVITY", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "特征类型不能为空")
    private FeatureTypeEnum featureType;

    @Schema(description = "本次加分值（可正可负）", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "加分值不能为空")
    @Min(value = -100, message = "加分值不能小于-100")
    @Max(value = 100, message = "加分值不能大于100")
    private Integer addScore;

    @Schema(description = "加分前的特征分值（仅明细新增时必填）", example = "50", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Min(value = -100, message = "加分前分值不能小于-100")
    @Max(value = 100, message = "加分前分值不能大于100")
    private Integer scoreBefore;
}