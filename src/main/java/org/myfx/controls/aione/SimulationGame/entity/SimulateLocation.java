package org.myfx.controls.aione.SimulationGame.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模拟游戏-地点表实体类
 * 对应表：t_simulate_location
 */
@Data
@Schema(description = "模拟游戏-地点实体")
public class SimulateLocation {

    @Schema(description = "地点ID（主键，自增）", example = "1")
    private Integer locationId;

    @Schema(description = "地点编码（如CITY_CENTER、FOREST）", example = "CITY_CENTER")
    private String locationCode;

    @Schema(description = "地点描述（详细场景说明）", example = "城市中心，繁华的商贸街区，遍布各类商铺")
    private String locationDesc;

    @Schema(description = "创建时间", example = "2026-01-30 10:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2026-01-30 11:00:00")
    private LocalDateTime updateTime;
}