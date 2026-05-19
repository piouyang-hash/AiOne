package org.myfx.controls.aione.UserService.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改密码请求DTO
 */
@Data
@Schema(description = "修改密码请求参数")
public class ChangePasswordDTO {
    @NotBlank(message = "原密码不能为空")
    @Size(min = 6, message = "原密码长度不能少于6位")
    @Schema(description = "原密码（登录态验证）", requiredMode = Schema.RequiredMode.REQUIRED, example = "Old123456")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, message = "新密码长度不能少于6位")
    @Schema(description = "新密码（需符合复杂度规则）", requiredMode = Schema.RequiredMode.REQUIRED, example = "New123456")
    private String newPassword;
}