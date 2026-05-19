package org.myfx.controls.aione.SimulationGame.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 模拟游戏-地点与事件关联表实体类
 * 对应表：simulate_location_event_relation
 */
@Data
@Schema(description = "模拟游戏-地点与事件关联实体")
public class SimulateLocationEventRelation {

    @Schema(description = "关联ID（主键，自增）", example = "1")
    private Integer relationId;

    @Schema(description = "地点编码（如CITY_CENTER、FOREST）", example = "CITY_CENTER")
    private String locationCode;

    @Schema(description = "事件编码（如TRADING、SLEEP）", example = "TRADING")
    private String eventCode;

    @Schema(description = "事件在该地点的默认持续秒数（游戏服务器时间跨度）", example = "120")
    private Integer eventDuration;

    @Schema(description = "创建时间", example = "2026-01-30 10:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2026-01-30 11:00:00")
    private LocalDateTime updateTime;
}