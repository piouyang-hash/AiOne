package org.myfx.controls.aione.UserService.model.vo;

import lombok.Data; // 用Lombok的@Data省掉getter/setter，和你的实体类风格保持一致
import io.swagger.v3.oas.annotations.media.Schema;
import org.myfx.controls.aione.UserService.model.dto.LoginUserProfileDTO;

@Data
@Schema(description = "登录结果返回对象")
public class LoginResultVO {

    @Schema(description = "用户ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer userId;

    @Schema(description = "用户资料信息", requiredMode = Schema.RequiredMode.REQUIRED)
    private LoginUserProfileDTO profile; // 引用独立的资料DTO（拆出独立类）

    // ===================== 【新增】完整RefreshTokenVO对象 =====================
    @Schema(description = "刷新令牌完整信息对象（含过期时间/记住我）", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private RefreshTokenVO refreshTokenVO;
}