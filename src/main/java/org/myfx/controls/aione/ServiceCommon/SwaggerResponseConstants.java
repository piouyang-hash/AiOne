package org.myfx.controls.aione.ServiceCommon;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.MediaType;

import java.lang.annotation.*;

/**
 * ✅ Swagger 统一响应注解集合
 * 用法：
 *  - 单个：@Api200
 *  - 多个：@Api200 @Api400 @Api500
 *  - 或在类上自定义组合注解（如 @ApiStandardResponses）
 */
public class SwaggerResponseConstants {

    // ==============================
    // 200 - 成功响应
    // ==============================
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD,ElementType.ANNOTATION_TYPE})
    @ApiResponse(
            responseCode = "200",
            description = "操作成功",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AppResponse.class)
            )
    )
    public @interface Api200 {}

    // ==============================
    // 400 - 参数错误 / 业务异常
    // ==============================
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD,ElementType.ANNOTATION_TYPE})
    @ApiResponse(
            responseCode = "400",
            description = "参数错误 / 业务逻辑异常",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AppResponse.class)
            )
    )
    public @interface Api400 {}

    // ==============================
    // 401 - 未授权
    // ==============================
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD,ElementType.ANNOTATION_TYPE})
    @ApiResponse(
            responseCode = "401",
            description = "未授权（令牌无效或过期）",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AppResponse.class)
            )
    )
    public @interface Api401 {}

    // ==============================
    // 403 - 禁止访问
    // ==============================
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD,ElementType.ANNOTATION_TYPE})
    @ApiResponse(
            responseCode = "403",
            description = "禁止访问（无权限操作）",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AppResponse.class)
            )
    )
    public @interface Api403 {}

    // ==============================
    // 404 - 资源不存在
    // ==============================
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
    @ApiResponse(
            responseCode = "404",
            description = "资源不存在（如用户ID不存在、资料ID不存在等）",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AppResponse.class)
            )
    )
    public @interface Api404 {}

    // ==============================
    // 500 - 服务器内部错误
    // ==============================
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD,ElementType.ANNOTATION_TYPE})
    @ApiResponse(
            responseCode = "500",
            description = "服务器内部错误",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AppResponse.class)
            )
    )
    public @interface Api500 {}

    // ==============================
    // 可选：组合注解，包含常见的几种响应
    // ==============================
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    @Api200
    @Api400
    @Api500
    public @interface ApiStandardResponses {}

    private SwaggerResponseConstants() {}
}
