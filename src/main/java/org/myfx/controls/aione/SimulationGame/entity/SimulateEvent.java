package org.myfx.controls.aione.SimulationGame.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 模拟游戏-事件表实体类
 * 对应表：simulate_event
 */
@Data
@Schema(description = "模拟游戏-事件实体")
public class SimulateEvent {

    @Schema(description = "事件ID（主键，自增）", example = "1")
    private Integer eventId;

    @Schema(description = "事件编码（如TRADING、SLEEP、FIGHT）", example = "SLEEP")
    private String eventCode;

    @Schema(description = "事件描述（详细事件说明）", example = "睡觉休息，恢复体力值，默认持续120分钟")
    private String eventDesc;

    @Schema(description = "创建时间", example = "2026-01-30 10:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2026-01-30 11:00:00")
    private LocalDateTime updateTime;
}