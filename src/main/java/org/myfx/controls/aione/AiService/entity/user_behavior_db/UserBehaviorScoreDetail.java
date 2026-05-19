package org.myfx.controls.aione.AiService.entity.user_behavior_db;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import org.myfx.controls.aione.AiService.common.user_behavior_db.FeatureTypeEnum;

import java.time.LocalDateTime;

/**
 * 用户行为加分明细表实体
 */
@Data
@TableName("user_behavior_score_detail")
public class UserBehaviorScoreDetail {

    @Schema(description = "雪花ID（主键）", example = "1425879635487512577")
    @TableId(type = IdType.INPUT)
    private Long id;

    @Schema(description = "用户ID（冗余，优化查询）", example = "1001")
    @TableField("user_id")
    private Integer userId;

    @Schema(description = "关联base_user_behavior的行为ID", example = "1")
    @TableField("behavior_id")
    private Integer behaviorId;

    @Schema(description = "加分特征类型（ACTIVITY-活跃度/FAVOR-喜爱度/FAMILIAR-熟悉度）", example = "ACTIVITY")
    @TableField("feature_type")
    private FeatureTypeEnum featureType;

    @Schema(description = "本次加分值（可正可负，范围-100~100）", example = "5")
    @TableField("add_score")
    private Integer addScore;

    @Schema(description = "加分后特征分值（0-100）", example = "90")
    @TableField("score_after")
    private Integer scoreAfter;

    /**
     * 行为发生时间戳（毫秒）
     */
    @Schema(description = "行为发生时间戳（毫秒）", example = "1736596200000") // 示例值对应2026-01-11 10:30:00的毫秒时间戳
    @TableField("behavior_time")
    private Long behaviorTime; // 核心修改：LocalDateTime → Long（存储毫秒级时间戳）

    @Schema(description = "创建时间", example = "2026-01-11 10:31:00")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2026-01-11 10:31:00")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}