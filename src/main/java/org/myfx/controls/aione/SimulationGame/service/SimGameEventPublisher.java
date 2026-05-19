package org.myfx.controls.aione.SimulationGame.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.SimulationGame.event.SimGameEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 模拟游戏事件发布器
 * 替代原Kafka生产者，用于单体应用内发布模拟游戏事件
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SimGameEventPublisher {

    // 注入Spring事件发布器（核心替换KafkaTemplate）
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 发布模拟游戏事件（对外方法完全兼容原有调用，参数/名称不变）
     * @param locationDesc     地点描述（非空）
     * @param eventDesc        事件描述（非空）
     * @param eventDuration    事件持续时间（单位：秒，可为空，默认0）
     * @param nextLocationDesc 下一个地点描述（可为空，表示无后续地点）
     * @param nextEventDesc    下一个事件描述（可为空，表示无后续事件）
     */
    public void sendGameEventMessage(String locationDesc, String eventDesc, Integer eventDuration,
                                     String nextLocationDesc, String nextEventDesc) {
        // 1. 必传参数校验（原逻辑100%保留）
        if (locationDesc == null || locationDesc.trim().isEmpty()) {
            log.error("发布游戏事件失败：地点描述不能为空");
            return;
        }
        if (eventDesc == null || eventDesc.trim().isEmpty()) {
            log.error("发布游戏事件失败：事件描述不能为空");
            return;
        }
        // 持续时间兜底（原逻辑不变）
        eventDuration = (eventDuration == null) ? 0 : eventDuration;

        // 2. 封装事件对象（使用你之前改造的SimGameEvent）
        // nextEventDuration 无入参，默认赋值null
        SimGameEvent gameEvent = new SimGameEvent(
                this,
                locationDesc,
                eventDesc,
                eventDuration,
                nextLocationDesc,
                nextEventDesc,
                null
        );

        // 3. 发布Spring本地事件（核心替换Kafka发送）
        eventPublisher.publishEvent(gameEvent);
        log.info("模拟游戏事件发布成功，地点：{}，事件：{}", locationDesc, eventDesc);
    }
}