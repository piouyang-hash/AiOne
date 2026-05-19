package org.myfx.controls.aione.AiService.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 状态信息VO
 * 包含AI情感状态 + 用户特征分数
 */
@Data
@Schema(description = "当前全局状态信息响应")
public class StatusVO {

    @Schema(description = "用户ID", example = "1001")
    private Integer userId;

    // ===================== AI 情感状态字段 =====================
    @Schema(description = "好感度值", example = "85")
    private Integer likeValue;

    @Schema(description = "亲密度", example = "92")
    private Integer familiarity;

    @Schema(description = "活跃度值", example = "78")
    private Integer activityValue;

    // ===================== 用户特征分数字段 =====================
    @Schema(description = "活跃度评分", example = "8.2")
    private Double activityScore;

    @Schema(description = "好感度评分", example = "9.5")
    private Double favorScore;

    @Schema(description = "亲密度评分", example = "8.8")
    private Double familiarScore;
}