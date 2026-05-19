package org.myfx.controls.aione.AiService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.myfx.controls.aione.AiService.common.user_behavior_db.BehaviorEnum;
import org.myfx.controls.aione.AiService.common.user_behavior_db.FeatureTypeEnum;

/**
 * 用户行为分值配置 新增DTO
 * 用于：管理员新增【行为 + 分值类型 + 变动分值】配置
 * 对应表：user_behavior_score
 */
@Data
@Schema(description = "用户行为分值配置-新增请求参数")
public class UserBehaviorScoreAddDTO {

    @Schema(
            description = "用户行为编码（枚举）",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "CHAT_SEND_MSG"
    )
    @NotNull(message = "用户行为编码不能为空")
    private BehaviorEnum behaviorCode;

    @Schema(
            description = "分值类型（枚举）",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "ACTIVITY"
    )
    @NotNull(message = "分值类型不能为空")
    private FeatureTypeEnum scoreType;

    @Schema(
            description = "变动分值（-100~100）；传null=动态计算；传0=不变动；非null则必须在范围内",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "10"
    )
    @Min(value = -100, message = "分值不能小于 -100")
    @Max(value = 100, message = "分值不能大于 100")
    private Integer scoreVal;
}