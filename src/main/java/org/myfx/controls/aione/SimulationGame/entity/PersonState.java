package org.myfx.controls.aione.SimulationGame.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * 模拟游戏-人物实时状态表
 */
@Data
@TableName("simulate_person_state")
@Schema(description = "模拟游戏-人物实时状态表（精简版）")
public class PersonState {

    @TableId(type = IdType.INPUT)
    @Schema(description = "主键ID，固定为1", example = "1")
    private Integer id;

    @Schema(description = "饥饿值 0-100（越高越饿，每分钟+1.5）", example = "0.0")
    private Double hunger;

    @Schema(description = "精力值 0-100（越低越累，每分钟-0.8）", example = "100.0")
    private Double energy;

    @Schema(description = "心情值 0-100（越高越开心，每分钟-0.3）", example = "50.0")
    private Double mood;

    @Schema(description = "当前地点编码（关联地点表）", example = "HOME")
    private String currentLocationCode;

    @Schema(description = "当前事件编码（关联事件表）", example = "SLEEP")
    private String currentEventCode;

    @Schema(description = "当前活动结束的全局秒数", example = "86400")
    private Long activityEndGlobalSec;

    @Schema(description = "状态更新时间（真实时间）")
    private LocalDateTime updateTime;
}