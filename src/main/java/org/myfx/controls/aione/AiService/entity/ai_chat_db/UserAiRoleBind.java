package org.myfx.controls.aione.AiService.entity.ai_chat_db;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "用户与AI角色绑定实体（多对多）")
public class UserAiRoleBind {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "AI角色ID")
    private Integer roleId;

    @Schema(description = "绑定时间（自动填充）")
    private LocalDateTime createTime;
}