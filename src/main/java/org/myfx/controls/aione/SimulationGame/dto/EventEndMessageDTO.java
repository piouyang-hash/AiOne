package org.myfx.controls.aione.SimulationGame.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 游戏事件生产消息 DTO
 * 用于传递：当前事件点位 + 下一个事件点位信息
 */
@Data
@Schema(name = "EventEndMessageDTO", description = "游戏事件结束消息传输实体")
public class EventEndMessageDTO {

    @NotBlank(message = "当前地点编码不能为空")
    @Schema(description = "当前地点编码", example = "SCENE_001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String locationCode;

    @NotBlank(message = "当前事件编码不能为空")
    @Schema(description = "当前事件编码", example = "EVENT_OPEN", requiredMode = Schema.RequiredMode.REQUIRED)
    private String eventCode;

    @Schema(description = "下一个地点编码（可为空，表示无后续地点）", example = "SCENE_002")
    private String nextLocationCode;

    @Schema(description = "下一个事件编码（可为空，表示无后续事件）", example = "EVENT_CLOSE")
    private String nextEventCode;

    // ===================== 新增字段1：事件持续时间 =====================
    @Schema(description = "事件持续时间（单位：秒）", example = "3600", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer eventDuration;

    // ===================== 新增字段2：下一个事件预计执行时间 =====================
    @Schema(description = "下一个事件预计执行时间（单位：毫秒时间戳）", example = "1742611200000")
    private Integer nextEventDuration;

}