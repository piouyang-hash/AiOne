package org.myfx.controls.aione.UserService.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.AppTypeEnum;

@Data
@Schema(description = "登录请求参数")
public class LoginDTO {

    @Schema(description = "用户邮箱", requiredMode = Schema.RequiredMode.REQUIRED, example = "2872145474@qq.com")
    @NotBlank(message = "邮箱不能为空")
    private String email;

    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "ah1149727586")
    private String password;

    @Schema(description = "应用类型（READER=读者端，AI_CHAT=AI端）", requiredMode = Schema.RequiredMode.REQUIRED, example = "READER")
    @NotNull(message = "应用类型不能为空")
    private AppTypeEnum appType;

    @Schema(description = "记住我（true=勾选，false=不勾选）", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    //@NotNull(message = "记住我选项不能为空")
    private Boolean rememberMe;
}