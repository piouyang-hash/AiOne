package org.myfx.controls.aione.ServiceCommon.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger 安全配置类（用于JWT令牌验证）
 * 配置后，Swagger UI 会显示 “Authorize” 按钮
 * 输入 token 后，自动在所有带 security 的接口请求头加上 Authorization: Bearer <token>
 */
@Configuration
@SecurityScheme(
        name = "bearerAuth",                // 名称（和接口里的 @SecurityRequirement(name="bearerAuth") 对应）
        type = SecuritySchemeType.HTTP,     // HTTP类型认证
        scheme = "bearer",                  // Bearer令牌（JWT常用）
        bearerFormat = "JWT",               // 说明是JWT
        in = SecuritySchemeIn.HEADER        // 放在Header里
)
public class OpenApiSecurityConfig {
    // 不需要写任何方法！
}