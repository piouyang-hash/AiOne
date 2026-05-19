package org.myfx.controls.aione.AiService.entity.ai_chat_db;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.myfx.controls.aione.AiService.common.ai_chat_db.AiPersonalityEnum;
import org.myfx.controls.aione.AiService.common.ai_chat_db.AiPersonalityStrengthEnum;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.LogicalDeleteEnum;

import java.time.LocalDateTime;

/**
 * AI基本性格配置表实体类
 * 对应表：ai_personality_config
 */
@Data
@Schema(description = "AI基本性格配置实体")
public class AiPersonalityConfig {

    @Schema(description = "主键ID（雪花ID，手动生成）", example = "1489567890123456789")
    private Long id;

    @Schema(description = "归属用户ID（null=所有用户通用，非null=该用户专属性格）", example = "10001")
    private Integer userId;

    @Schema(description = "AI基本性格编码（关联AiPersonalityEnum枚举code，默认中性（1））", example = "1")
    private AiPersonalityEnum aiPersonalityCode;

    @Schema(description = "性格强度编码（关联AiPersonalityStrengthEnum枚举code，默认1级：基础强度）", example = "1")
    private AiPersonalityStrengthEnum personalityStrengthCode;

    @Schema(description = "创建时间（数据库自动生成）", example = "2026-01-01 12:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间（数据库自动更新）", example = "2026-01-01 13:00:00")
    private LocalDateTime updateTime;

    @Schema(description = "是否有效（1=有效，0=无效）", example = "1")
    private Integer isValid;

    /**
     * 删除状态（0=未删除，1=已删除）
     */
    @Schema(description = "删除状态（0=未删除，1=已删除）", example = "0", defaultValue = "0")
    private LogicalDeleteEnum isDeleted; // 对应数据库tinyint，用Integer适配
}