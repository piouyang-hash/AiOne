package org.myfx.controls.aione.SimulationGame.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 模拟游戏-事件执行序列规则表实体类
 * 对应表：simulate_event_sequence
 */
@Data
@Schema(description = "模拟游戏-事件执行序列规则实体")
public class SimulateEventSequence {

    @Schema(description = "序列规则ID（主键，自增）", example = "1")
    private Long sequenceId;

    @Schema(description = "角色ID")
    private Integer roleId;

    @Schema(description = "规则版本号（区分不同每日事件规则）", example = "1")
    private Integer version;

    @Schema(description = "地点编码（该事件序列所属地点）", example = "FOREST")
    private String locationCode;

    @Schema(description = "事件编码（该步骤执行的事件）", example = "FIGHT")
    private String eventCode;

    @Schema(description = "执行次序（1=第一步、2=第二步...）", example = "2")
    private Integer seqNum;

    @Schema(description = "创建时间", example = "2026-01-30 10:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2026-01-30 11:00:00")
    private LocalDateTime updateTime;
}