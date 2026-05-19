package org.myfx.controls.aione.AiService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 用户信息+爱好列表DTO（接口返回用）
 */
@Data
@Schema(description = "用户信息及爱好列表")
public class UserWithHobbyDTO {
    @Schema(description = "用户ID", example = "101")
    private Integer userId;

    @Schema(description = "年龄", example = "25")
    private Byte age;

    @Schema(description = "性别名称", example = "男")
    private String genderName;

    @Schema(description = "身份", example = "上班族")
    private String identity;

    @Schema(description = "爱好列表", example = "[\"阅读\",\"编程\",\"运动\"]")
    private List<String> hobbyList;
}