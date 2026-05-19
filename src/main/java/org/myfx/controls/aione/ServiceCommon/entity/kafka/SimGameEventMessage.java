package org.myfx.controls.aione.ServiceCommon.entity.kafka;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 模拟游戏事件Kafka消息实体
 * 用于封装发送到Kafka的游戏事件核心数据（存储描述而非编码）
 */
@Data
@Schema(description = "模拟游戏事件Kafka消息实体")
public class SimGameEventMessage {

    @Schema(description = "地点描述（冗余存储，便于阅读）", example = "市中心")
    private String locationDesc; // 替换原locationCode为地点描述

    @Schema(description = "事件描述（冗余存储，便于阅读）", example = "睡觉")
    private String eventDesc; // 替换原eventCode为事件描述

    // 新增：事件持续时间（单位：秒）
    @Schema(description = "事件持续时间（单位：秒）", example = "3600")
    private Integer eventDuration;

    @Schema(description = "下一个地点描述（可为空，表示无后续地点）", example = "学校")
    private String nextLocationDesc;

    @Schema(description = "下一个事件描述（可为空，表示无后续事件）", example = "放松")
    private String nextEventDesc;

    @Schema(description = "下一个事件预计执行时间（单位：毫秒时间戳）", example = "1742611200000")
    private Integer nextEventDuration;

    @Schema(description = "消息发送时间（生产者本地时间）", example = "2026-03-02 15:30:00")
    private LocalDateTime sendTime = LocalDateTime.now(); // 初始化自动填充当前时间
}