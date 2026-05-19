package org.myfx.controls.aione.AiService.entity.ai_chat_db;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.myfx.controls.aione.AiService.common.ai_chat_db.EmotionTypeEnum;

import java.time.LocalDateTime;

/**
 * AI情绪变动流水表实体类
 * 对应表：ai_emotion_log_detail
 */
@Data
@Schema(description = "AI情绪变动流水表（记录情绪值每一次波动明细）")
public class AiEmotionLogDetail {

    @Schema(description = "主键雪花ID", example = "1425678901234567890")
    private Long id;

    @Schema(description = "归属用户ID", example = "10001")
    private Integer userId;

    @Schema(description = "触发变动的行为ID", example = "5001")
    private Integer behaviorId;

    @Schema(description = "变动的情绪类型（LIKE-喜爱值/ACTIVITY-活跃度/FAMILIAR-熟悉度）", example = "ACTIVITY")
    private EmotionTypeEnum emotionType;

    @Schema(description = "变动分值（增量值，例如+5，-10）", example = "5")
    private Integer changeValue;

    @Schema(description = "变动后的实时分值（快照值）", example = "55")
    private Integer valueAfter;

    @Schema(description = "变动原因描述（可选）", example = "用户发送正向消息，活跃度+5")
    private String changeReason;

    @Schema(description = "行为发生时间戳（毫秒）", example = "1735689600000")
    private Long behaviorTime; // 行为发生时间戳（毫秒级，对应数据库behavior_time字段）

    @Schema(description = "记录创建时间", example = "2026-03-13 10:00:00")
    private LocalDateTime createTime;

    @Schema(description = "记录更新时间", example = "2026-03-13 10:00:00")
    private LocalDateTime updateTime;
}