package org.myfx.controls.aione.SimulationGame.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.myfx.controls.aione.SimulationGame.common.EventExecStatusEnum;

import java.time.LocalDateTime;

/**
 * 模拟游戏-事件执行记录表实体类
 * 对应表：simulate_event_record
 */
@Data
@Schema(description = "模拟游戏-事件执行记录实体")
public class SimulateEventRecord {

    @Schema(description = "记录ID（雪花ID，主键）", example = "1425678901234567890")
    private Long recordId;

    @Schema(description = "关联序列规则ID（对应预设规则ID）", example = "1")
    private Long sequenceId;

    @Schema(description = "事件编码（冗余存储，方便查询）", example = "SLEEP")
    private String eventCode;

    @Schema(description = "地点编码（冗余存储，方便查询）", example = "CITY_CENTER")
    private String locationCode;

    @Schema(description = "实际开始（游戏服务器时间，int，线性递增）", example = "10000")
    private Integer actualStart;

    @Schema(description = "实际结束（游戏服务器时间，int，线性递增）", example = "10480")
    private Integer actualEnd;

    @Schema(description = "默认执行时间（秒，事件自身特性，冗余存储仅用于查阅，非必填）", example = "28800")
    private Integer defaultDuration;

    @Schema(description = "执行状态：EXECUTING-执行中/FINISHED-已完成/FAILED-失败/INTERRUPTED-中断", example = "FINISHED")
    private EventExecStatusEnum execStatus;

    @Schema(description = "每日安排版本号（如1=周一、2=周二）", example = "1")
    private Integer version;

    @Schema(description = "当日执行次序（全局唯一，1=第1件事、2=第2件事）", example = "1")
    private Integer seqNum;

    @Schema(description = "记录创建时间（真实时间）", example = "2026-01-30 10:00:00")
    private LocalDateTime createTime;

    @Schema(description = "记录更新时间（真实时间）", example = "2026-01-30 11:00:00")
    private LocalDateTime updateTime;
}