package org.myfx.controls.aione.AiService.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.event.UserOfflineTooLongEvent;
import org.myfx.controls.aione.AiService.service.base.status.user_behavior_db.upper.UserBehaviorImpactScoreService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 用户离线过久事件监听器
 * 替代原 Kafka 消费者，处理用户离线超时业务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserOfflineTooLongEventListener {

    // 注入离线过久业务处理服务（和原消费者一致）
    private final UserBehaviorImpactScoreService userBehaviorImpactScoreService;

    /**
     * 监听 用户离线过久事件
     */
    @EventListener
    public void handleUserOfflineTooLongEvent(UserOfflineTooLongEvent event) {
        try {
            // 1. 从事件中获取用户ID
            Integer userId = event.getUserId();

            // 2. 基础参数校验（和原Kafka消费者一致）
            try {
                Assert.notNull(userId, "离线过久消息userId为空");
                Assert.isTrue(userId > 0, "离线过久消息userId不合法（需为正整数）");
            } catch (IllegalArgumentException e) {
                log.warn("【事件监听】离线过久消息参数不合法 → 原因：{}，userId：{}", e.getMessage(), userId);
                return;
            }

            // 3. 核心业务处理（完全复用原方法）
            boolean handleResult = userBehaviorImpactScoreService.handleUserOfflineTooLong(userId);

            // 4. 处理结果日志
            if (handleResult) {
                log.info("【事件监听】用户离线过久业务处理成功 → userId：{}", userId);
            } else {
                log.warn("【事件监听】用户离线过久业务处理失败 → userId：{}", userId);
            }

        } catch (Exception e) {
            log.error("【事件监听】用户离线过久事件处理失败", e);
        }
    }
}