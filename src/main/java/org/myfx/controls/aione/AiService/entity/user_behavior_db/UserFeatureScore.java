package org.myfx.controls.aione.AiService.entity.user_behavior_db;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 用户特征分值表实体（活跃度/喜爱度/熟悉度）
 */
@Data
@TableName("user_feature_score")
public class UserFeatureScore {

    @Schema(description = "雪花ID（主键）", example = "1425879635487512576")
    @TableId(type = IdType.INPUT)
    private Long id;

    @Schema(description = "用户ID", example = "1001")
    @TableField("user_id")
    private Integer userId;

    @Schema(description = "用户活跃度分值（0-100，默认0）", example = "50")
    @TableField("activity_score")
    private Integer activityScore;

    @Schema(description = "用户对AI喜爱度分值（-100到100，默认0）", example = "-10")
    @TableField("favor_score")
    private Integer favorScore;

    @Schema(description = "AI对用户熟悉度分值（0-100，默认0）", example = "75")
    @TableField("familiar_score")
    private Integer familiarScore;

    @Schema(description = "创建时间", example = "2026-01-11 10:00:00")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2026-01-11 11:00:00")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    // 这样配置，存 Redis 是数字，存数据库还是日期，且彻底解决报错
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private LocalDateTime updateTime;

}