package org.myfx.controls.aione.AiService.entity.user_behavior_db;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.myfx.controls.aione.AiService.common.user_behavior_db.BehaviorEnum;
import org.myfx.controls.aione.AiService.common.user_behavior_db.FeatureTypeEnum;

import java.time.LocalDateTime;

/**
 * 用户行为分值配置表实体（多维度分值独立配置）
 */
@Data
@TableName("user_behavior_score")
public class UserBehaviorScore {

    @Schema(description = "主键ID", example = "1")
    private Integer id;

    @Schema(description = "关联用户行为编码", example = "CHAT_SEND_MSG")
    @TableField("behavior_code")
    private BehaviorEnum behaviorCode;

    @Schema(description = "分值类型：LIKE-喜爱值,ACTIVITY-活跃度,FAMILIARITY-熟悉度", example = "ACTIVITY")
    @TableField("score_type")
    private FeatureTypeEnum scoreType;

    @Schema(description = "变动分值（-100~100，NULL表示不配置）", example = "10")
    @TableField("score_val")
    private Integer scoreVal;

    @Schema(description = "创建时间", example = "2026-01-11 10:00:00")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2026-01-11 11:00:00")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}