package org.myfx.controls.aione.UserService.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.LogicalDeleteEnum;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.RoleEnum;

import java.time.LocalDateTime;

@Data
@Schema(description = "用户实体")
public class User {

    @Schema(description = "用户ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer id;

    @Schema(description = "密码", example = "encrypted_password_string", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    private String password;

    @Schema(description = "邮箱", example = "2872145473@qq.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "邮箱不能为空")
    private String email;

    @Schema(description = "用户角色", accessMode = Schema.AccessMode.READ_ONLY, hidden = true)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY) // 前端无法传入，仅序列化返回时可见（可选）
    private RoleEnum role; // 数据库自动赋值，前端不可见、不可改

    @Schema(description = "创建时间", example = "2023-10-01T12:00:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记（适配SAGA场景）
     * 不设置默认值：依赖数据库is_deleted字段的DEFAULT 0
     */
    @Schema(description = "逻辑删除：0-未删除 1-已删除", hidden = true)
    private LogicalDeleteEnum isDeleted;
}