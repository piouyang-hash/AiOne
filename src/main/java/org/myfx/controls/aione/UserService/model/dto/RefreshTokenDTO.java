package org.myfx.controls.aione.UserService.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Token刷新请求参数DTO
 *
 * @author 系统自动生成
 */
@Data
@Schema(title = "Token刷新请求DTO", description = "用户无感刷新Token的入参封装")
public class RefreshTokenDTO {

    @Schema(description = "记住我状态：true-长期有效/false-会话有效", example = "false", defaultValue = "false")
    @NotNull(message = "记住我状态不能为空")
    private Boolean rememberMe;

    @Schema(description = "旧的AccessToken（用于后端校验合法性）", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    @NotNull(message = "旧AccessToken不能为空")
    private String oldAccessToken;

    @Schema(description = "Token提前过期时间（单位：毫秒）", example = "60000")
    @NotNull(message = "过期时间不能为空")
    private Long expireTime;
}