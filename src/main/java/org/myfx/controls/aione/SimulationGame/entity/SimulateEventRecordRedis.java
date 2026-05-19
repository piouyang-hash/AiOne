package org.myfx.controls.aione.SimulationGame.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.myfx.controls.aione.SimulationGame.common.EventExecStatusEnum;

/**
 * 模拟游戏-事件记录表Redis实体类
 * 用于Redis存储，适配缓存场景
 */
@Data
@Schema(description = "模拟游戏-事件记录Redis实体")
public class SimulateEventRecordRedis {

    @Schema(description = "记录ID（雪花ID，主键）", example = "1425678901234567890")
    private Long recordId;

    @Schema(description = "角色ID")
    private Integer roleId;

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

    @Schema(description = "执行状态：EXECUTING-执行中/FINISHED-已完成/FAILED-失败", example = "FINISHED")
    private EventExecStatusEnum execStatus;
}