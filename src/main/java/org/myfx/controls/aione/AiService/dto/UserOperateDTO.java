package org.myfx.controls.aione.AiService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户注册/注销请求DTO（内部接口专用）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户注册/注销请求参数（仅含用户ID）")
public class UserOperateDTO {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID（Integer类型，唯一标识）", requiredMode = Schema.RequiredMode.REQUIRED, example = "101")
    private Integer userId;
}