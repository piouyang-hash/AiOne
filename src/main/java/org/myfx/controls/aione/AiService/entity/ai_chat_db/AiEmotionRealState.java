package org.myfx.controls.aione.AiService.entity.ai_chat_db;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.LogicalDeleteEnum;

import java.time.LocalDateTime;

/**
 * AI情绪实时状态表实体类
 */
@Data
@Schema(description = "AI情绪实时状态实体")
public class AiEmotionRealState {

    @Schema(description = "主键ID（雪花ID，手动生成）", example = "1234567890123456789")
    private Long id;

    @Schema(description = "归属用户ID（关联base_user_info的user_id）", example = "10001")
    private Integer userId;

    @Schema(description = "喜爱值（-100=极度愤怒，0=冷静，100=极度喜爱）", example = "50")
    private Integer likeValue;

    @Schema(description = "活跃度（0=最低，50=中等，100=最高）", example = "50")
    private Integer activityValue;

    @Schema(description = "熟悉度（0=完全陌生，100=极度熟悉）", example = "0")
    private Integer familiarity;

    @Schema(description = "状态创建时间")
    private LocalDateTime createTime;

    @Schema(description = "状态更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "是否有效（1=有效，0=无效）", example = "1")
    private Integer isValid;

    /**
     * 删除状态（0=未删除，1=已删除）
     */
    @Schema(description = "删除状态（0=未删除，1=已删除）", example = "0", defaultValue = "0")
    private LogicalDeleteEnum isDeleted; // 对应数据库tinyint，用Integer适配
}