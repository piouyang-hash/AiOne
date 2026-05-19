package org.myfx.controls.aione.AiService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * AI角色 - 新增DTO
 * 仅包含新增角色所需的核心字段
 */
@Data
@Schema(description = "AI角色新增请求参数")
public class AiRoleAddDTO {

    @Schema(description = "角色基础描述（如：售后客服AI、生日祝福AI）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String roleDesc;

    @Schema(description = "人设核心定义（AI性格/身份描述）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String personaCore;

    @Schema(description = "人设语气风格（normal-正常/gentle-温柔/active-活泼/serious-严谨）")
    private String personaTone;

    @Schema(description = "角色状态 0-草稿 1-已发布", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer roleStatus;

    @Schema(description = "可见范围 0-私有(仅自己) 1-公开(所有人)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer visibleScope;
}