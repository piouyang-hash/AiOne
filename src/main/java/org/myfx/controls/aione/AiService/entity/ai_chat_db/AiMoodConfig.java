package org.myfx.controls.aione.AiService.entity.ai_chat_db;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.myfx.controls.aione.AiService.common.ai_chat_db.AiMoodEnum;
import org.myfx.controls.aione.AiService.common.ai_chat_db.AiMoodStrengthEnum;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.LogicalDeleteEnum;

import java.time.LocalDateTime;

/**
 * AI心情配置实体类（对应ai_mood_config表）
 */
@Data
@Schema(name = "AiMoodConfig", description = "AI心情配置实体（控制AI说话风格和强度）")
public class AiMoodConfig {

    @Schema(description = "主键ID（雪花ID）", example = "1789234567890123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "归属用户ID（null=所有用户通用）", example = "1")
    private Integer userId;

    @Schema(description = "AI心情编码（关联AiMoodEnum枚举code）", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private AiMoodEnum aiMoodCode;

    @Schema(description = "AI强度编码（关联AiMoodStrengthEnum枚举code）", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    private AiMoodStrengthEnum aiStrengthCode;

    @Schema(description = "创建时间", example = "2025-12-31 10:00:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-12-31 10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updateTime;

    @Schema(description = "是否有效（1=有效，0=无效）", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer isValid;

    /**
     * 删除状态（0=未删除，1=已删除）
     */
    @Schema(description = "删除状态（0=未删除，1=已删除）", example = "0", defaultValue = "0")
    private LogicalDeleteEnum isDeleted; // 对应数据库tinyint，用Integer适配
}