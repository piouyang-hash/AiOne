package org.myfx.controls.aione.SimulationGame.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 模拟游戏事件新增请求DTO
 * 用于接收前端新增事件的请求参数
 */
@Data // Lombok注解，自动生成getter/setter/toString等方法
@Schema(name = "SimulateEventAddDTO", description = "新增游戏事件请求参数")
public class SimulateEventAddDTO {

    /**
     * 事件编码（唯一标识）
     */
    @NotBlank(message = "事件编码不能为空") // 校验注解：非空（去除首尾空格后也不能为空）
    @Schema(
            description = "事件编码（唯一标识，不可重复，如TRADING、FIGHT、QUEST_ACCEPT）",
            requiredMode = Schema.RequiredMode.REQUIRED, // 标记为必填项（Swagger文档中高亮）
            example = "TRADING" // 提供示例值，提升Swagger可读性
    )
    private String eventCode;

    /**
     * 事件描述（详细事件说明）
     */
    @Schema(
            description = "事件描述（详细事件说明，可为空，如「与野外怪物发生战斗，获得经验值」）",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED, // 标记为非必填项
            example = "与野外怪物发生战斗，获得经验值" // 提供示例值
    )
    private String eventDesc;
}