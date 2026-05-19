package org.myfx.controls.aione.AiService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.myfx.controls.aione.AiService.common.ai_chat_db.AiPersonalityEnum;

/**
 * 新增AI性格配置入参DTO
 */
@Data
@Schema(description = "新增AI性格配置入参")
public class AddAiPersonalityConfigDTO {

    @Schema(description = "归属用户ID（null=所有用户通用）", example = "10001")
    private Integer userId;

    @Schema(description = "AI基本性格编码", example = "1")
    private AiPersonalityEnum aiPersonalityCode;
}