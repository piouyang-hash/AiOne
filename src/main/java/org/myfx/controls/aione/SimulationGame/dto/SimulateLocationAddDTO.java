package org.myfx.controls.aione.SimulationGame.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 模拟游戏地点新增请求DTO
 * 用于接收前端新增地点的请求参数
 */
@Data // Lombok注解，自动生成getter/setter/toString等方法
@Schema(name = "SimulateLocationAddDTO", description = "新增游戏地点请求参数")
public class SimulateLocationAddDTO {

    /**
     * 地点编码（唯一标识）
     */
    @NotBlank(message = "地点编码不能为空") // 校验注解：非空（去除首尾空格后也不能为空）
    @Schema(
            description = "地点编码（唯一标识，不可重复，如CITY_CENTER、FOREST）",
            requiredMode = Schema.RequiredMode.REQUIRED, // 标记为必填项（Swagger文档中高亮）
            example = "CITY_CENTER" // 提供示例值，提升Swagger可读性
    )
    private String locationCode;

    /**
     * 地点描述（详细场景说明）
     */
    @Schema(
            description = "地点描述（详细场景说明，可为空，如「城市中心，繁华的商贸街区」）",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED, // 标记为非必填项
            example = "城市中心，繁华的商贸街区" // 提供示例值
    )
    private String locationDesc;
}