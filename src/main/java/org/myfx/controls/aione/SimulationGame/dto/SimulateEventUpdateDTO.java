package org.myfx.controls.aione.SimulationGame.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.myfx.controls.aione.SimulationGame.common.EventExecStatusEnum;

/**
 * 模拟事件记录 - 更新为完成状态 DTO
 * 前3个字段：定位唯一任务
 * 后2个字段：需要更新的字段
 */
@Data
@Schema(description = "模拟事件记录更新参数（更新为完成状态）")
public class SimulateEventUpdateDTO {

    @Schema(description = "地点编码【定位参数】", requiredMode = Schema.RequiredMode.REQUIRED)
    private String locationCode;

    @Schema(description = "事件编码【定位参数】", requiredMode = Schema.RequiredMode.REQUIRED)
    private String eventCode;

    @Schema(description = "实际开始时间【定位参数】", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer actualStart;

    @Schema(description = "实际结束时间【更新参数】", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer actualEnd;

    @Schema(description = "执行状态【更新参数】", requiredMode = Schema.RequiredMode.REQUIRED)
    private EventExecStatusEnum execStatus;
}