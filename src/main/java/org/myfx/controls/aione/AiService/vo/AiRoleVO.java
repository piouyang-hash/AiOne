package org.myfx.controls.aione.AiService.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI角色 展示VO
 * 用于接口返回给前端的角色数据
 */
@Data
@Schema(description = "AI角色展示VO")
public class AiRoleVO {

    @Schema(description = "角色ID（主键，自增）")
    private Integer roleId;

    @Schema(description = "角色基础描述（如：售后客服AI、生日祝福AI）")
    private String roleDesc;

    @Schema(description = "人设核心定义（AI性格/身份描述）")
    private String personaCore;

    @Schema(description = "人设语气风格（normal-正常/gentle-温柔/active-活泼/serious-严谨）")
    private String personaTone;

    @Schema(description = "角色头像网络路径")
    private String avatarPath;

    @Schema(description = "角色创建人ID（归属用户）")
    private Integer createUserId;

    @Schema(description = "角色状态 0-草稿 1-已发布")
    private Integer roleStatus;

    @Schema(description = "可见范围 0-私有(仅自己) 1-公开(所有人)")
    private Integer visibleScope;

    @Schema(description = "记录创建时间（自动填充）")
    private LocalDateTime createTime;

    @Schema(description = "记录更新时间（自动填充）")
    private LocalDateTime updateTime;

}