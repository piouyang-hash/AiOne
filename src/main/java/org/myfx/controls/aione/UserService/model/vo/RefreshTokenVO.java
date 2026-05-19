package org.myfx.controls.aione.UserService.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.myfx.controls.aione.ServiceCommon.config.JwtConfig;

@Data
@Schema(description = "刷新Token返回结果")
public class RefreshTokenVO {


    @Schema(description = "新短期访问令牌")
    private String accessToken;

    @Schema(description = "新长期刷新令牌")
    private String refreshToken;

    @Schema(description = "是否记住我")
    private Boolean rememberMe;

    @Schema(description = "AccessToken过期时间戳（毫秒）")
    private Long accessTokenExpirationTime;

    @Schema(description = "RefreshToken过期时间戳（毫秒）")
    private Long refreshTokenExpirationTime;

    /**
     * 自定义setRememberMe方法
     * 调用时：自动填充 AccessToken 过期时间 + 根据 rememberMe 填充 RefreshToken 过期时间
     * @param rememberMe 是否记住我
     */
    public void setRememberMe(Boolean rememberMe) {
        this.rememberMe = rememberMe;

        // 1. 固定设置 AccessToken 过期时间
        this.accessTokenExpirationTime = JwtConfig.getAccessTokenExpirationTime();

        // 2. 根据 rememberMe 动态设置 RefreshToken 过期时间
        if (Boolean.TRUE.equals(rememberMe)) {
            // true → 长期有效期
            this.refreshTokenExpirationTime = JwtConfig.getRefreshTokenExpirationTime();
        } else {
            // false → 会话有效期
            this.refreshTokenExpirationTime = JwtConfig.getRefreshTokenSessionExpirationTime();
        }
    }
}