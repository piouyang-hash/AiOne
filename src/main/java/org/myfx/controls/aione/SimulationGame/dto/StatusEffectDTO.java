package org.myfx.controls.aione.SimulationGame.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 模拟游戏-地点事件属性影响表
 */
@Data
@Schema(description = "模拟游戏-地点事件对人物三维属性的影响表")
public class StatusEffectDTO {

    @Schema(description = "地点编码", example = "HOME")
    private String locationCode;

    @Schema(description = "事件编码", example = "SLEEP")
    private String eventCode;

    @Schema(description = "饥饿值变化量（正数增加，负数减少）", example = "1.0")
    private Double hungerEffect;

    @Schema(description = "精力值变化量（正数增加，负数减少）", example = "20.0")
    private Double energyEffect;

    @Schema(description = "心情值变化量（正数增加，负数减少）", example = "5.0")
    private Double moodEffect;
}

