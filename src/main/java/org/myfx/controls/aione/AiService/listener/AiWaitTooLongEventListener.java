package org.myfx.controls.aione.AiService.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.event.AiWaitTooLongEvent;
import org.myfx.controls.aione.AiService.service.base.status.ai_behavior_db.upper.AiBehaviorImpactScoreService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * AI等待过久事件监听器
 * 替代原 Kafka 消费者，处理AI等待超时业务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiWaitTooLongEventListener {

    // 注入AI等待过久业务处理服务（和原消费者一致）
    private final AiBehaviorImpactScoreService aiBehaviorImpactScoreService;

    /**
     * 监听 AI 等待过久事件
     */
    @EventListener
    public void handleAiWaitTooLongEvent(AiWaitTooLongEvent event) {
        try {
            // 1. 从事件中获取用户ID
            Integer userId = event.getUserId();

            // 2. 基础校验（和原Kafka消费者一致）
            if (userId == null || userId <= 0) {
                log.warn("【事件监听】AI等待过久消息userId不合法，userId：{}", userId);
                return;
            }

            // 3. 核心业务处理（完全复用原方法）
            boolean handleResult = aiBehaviorImpactScoreService.handleAiWaitTooLong(userId);

            // 4. 处理结果日志
            if (handleResult) {
                log.info("【事件监听】AI等待过久业务处理成功 → userId：{}", userId);
            } else {
                log.warn("【事件监听】AI等待过久业务处理失败 → userId：{}", userId);
            }

        } catch (Exception e) {
            log.error("【事件监听】AI等待过久事件处理失败", e);
        }
    }
}