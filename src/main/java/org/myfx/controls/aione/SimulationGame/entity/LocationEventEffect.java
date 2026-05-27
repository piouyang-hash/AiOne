package org.myfx.controls.aione.SimulationGame.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * 模拟游戏-地点事件属性影响表
 */
@Data
@TableName("simulate_location_event_effect")
@Schema(description = "模拟游戏-地点事件对人物三维属性的影响表")
public class LocationEventEffect {

    @TableId(type = IdType.AUTO)
    @Schema(description = "影响效果ID", example = "1")
    private Integer effectId;

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

    @Schema(description = "记录创建时间")
    private LocalDateTime createTime;

    @Schema(description = "记录更新时间")
    private LocalDateTime updateTime;
}