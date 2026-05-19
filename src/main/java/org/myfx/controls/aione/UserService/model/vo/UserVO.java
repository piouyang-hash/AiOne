package org.myfx.controls.aione.UserService.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@Schema(description = "用户信息响应对象")
@AllArgsConstructor
public class UserVO {

    @Schema(description = "用户ID", example = "1001")
    private Integer id;

}