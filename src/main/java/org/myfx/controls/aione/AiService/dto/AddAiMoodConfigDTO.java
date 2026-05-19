package org.myfx.controls.aione.AiService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.myfx.controls.aione.AiService.common.ai_chat_db.AiMoodEnum;

/**
 * 新增AI心情配置DTO
 */
@Data
@Schema(name = "AddAiMoodConfigDTO", description = "新增AI心情配置请求参数")
public class AddAiMoodConfigDTO {

    @Schema(description = "用户ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用户ID不能为空")
    private Integer userId; // 已改为Integer类型

    @Schema(description = "AI心情编码（关联AiMoodEnum枚举code）", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "AI心情编码不能为空")
    private AiMoodEnum aiMoodCode;
}