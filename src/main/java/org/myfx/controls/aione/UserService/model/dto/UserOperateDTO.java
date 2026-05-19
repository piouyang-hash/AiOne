package org.myfx.controls.aione.UserService.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.AppTypeEnum;

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

    @Schema(description = "应用类型枚举（注册时必填，用于指定注册的应用；" +
            "注销时会删除用户所有应用下的信息，不区分应用，不用填写）")
    private AppTypeEnum appType;

}