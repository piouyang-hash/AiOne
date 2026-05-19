package org.myfx.controls.aione.ServiceCommon.entity.event;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import java.time.LocalDateTime;

/**
 * 模拟游戏事件
 */
@Getter
public class SimGameEvent extends ApplicationEvent {

    @Schema(description = "地点描述（冗余存储，便于阅读）", example = "市中心")
    private final String locationDesc;

    @Schema(description = "事件描述（冗余存储，便于阅读）", example = "睡觉")
    private final String eventDesc;

    @Schema(description = "事件持续时间（单位：秒）", example = "3600")
    private final Integer eventDuration;

    @Schema(description = "下一个地点描述（可为空，表示无后续地点）", example = "学校")
    private final String nextLocationDesc;

    @Schema(description = "下一个事件描述（可为空，表示无后续事件）", example = "放松")
    private final String nextEventDesc;

    @Schema(description = "下一个事件预计执行时间（单位：毫秒时间戳）", example = "1742611200000")
    private final Integer nextEventDuration;

    @Schema(description = "消息发送时间（生产者本地时间）", example = "2026-03-02 15:30:00")
    private final LocalDateTime sendTime;

    // 标准构造方法
    public SimGameEvent(Object source, String locationDesc, String eventDesc, Integer eventDuration,
                        String nextLocationDesc, String nextEventDesc, Integer nextEventDuration) {
        super(source);
        this.locationDesc = locationDesc;
        this.eventDesc = eventDesc;
        this.eventDuration = eventDuration;
        this.nextLocationDesc = nextLocationDesc;
        this.nextEventDesc = nextEventDesc;
        this.nextEventDuration = nextEventDuration;
        this.sendTime = LocalDateTime.now();
    }
}