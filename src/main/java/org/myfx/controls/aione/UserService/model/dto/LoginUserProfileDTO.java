package org.myfx.controls.aione.UserService.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "登录返回的用户资料信息")
public class LoginUserProfileDTO {
    @Schema(description = "用户资料ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer id;

    @Schema(description = "用户ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer userId;

    @Schema(description = "用户昵称", example = "AbstractUserProfile")
    private String nickname;

    @Schema(description = "个人简介", example = "爱写代码的理工男～")
    private String bio;

    @Schema(description = "头像URL", accessMode = Schema.AccessMode.READ_ONLY)
    private String avatarUrl;

    @Schema(description = "创建时间", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createTime;

}