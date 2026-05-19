package org.myfx.controls.aione.AiService.listener;

import cn.hutool.core.lang.Assert;
import jakarta.annotation.Resource;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.event.AiWaitTooLongEvent;
import org.myfx.controls.aione.AiService.event.UserOfflineTooLongEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Redis用户活跃度分值键过期事件监听器
 * 专门监听 feature:score:user:activity:{userId}:{sessionId} 格式的键过期，处理活跃度分值相关离线逻辑
 */
@Slf4j
@Component
public class RedisActivityScoreKeyExpirationListener extends KeyExpirationEventMessageListener {

    // ========== 可注入业务服务（根据实际需求添加，比如用户行为分值处理服务） ==========
    // 1. 用户活跃度分值键前缀（原逻辑保留，新增User语义）
    private static final String USER_ACTIVITY_SCORE_KEY_PREFIX = "feature:score:user:activity:";
    // 2. 新增：AI活跃度分值键前缀（匹配AI的Redis Key构造规则）
    private static final String AI_ACTIVITY_SCORE_KEY_PREFIX = "emotion:score:ai:activity:";

    @Resource
    private ApplicationEventPublisher eventPublisher;

    /**
     * 构造方法，注入Redis消息监听容器（必须）
     */
    public RedisActivityScoreKeyExpirationListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    /**
     * 核心方法：监听并处理Redis键过期事件
     * 同时处理：
     * - 用户：feature:score:user:activity:{userId} / feature:score:user:activity:{userId}:{sessionId}
     * - AI：emotion:score:ai:activity:{userId}
     * @param message 过期的键名（字节数组形式）
     * @param pattern 监听的频道模式
     */
    @Override
    public void onMessage(@NonNull Message message, byte[] pattern) {
        // 1. 将字节数组转换为字符串（过期的键名），指定UTF-8编码避免乱码
        String expiredKey;
        try {
            expiredKey = new String(message.getBody(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("【Redis过期监听】键名转字符串失败", e);
            return;
        }

        // 2. 分维度处理过期键
        if (expiredKey.startsWith(USER_ACTIVITY_SCORE_KEY_PREFIX)) {
            // 处理用户活跃度键过期
            handleUserActivityScoreKeyExpired(expiredKey);
        } else if (expiredKey.startsWith(AI_ACTIVITY_SCORE_KEY_PREFIX)) {
            // 新增：处理AI活跃度键过期
            handleAiActivityScoreKeyExpired(expiredKey);
        }
    }

    /**
     * 处理【用户】活跃度分值键过期逻辑（移除sessionId相关，仅处理用户维度）
     * 固定键格式：feature:score:user:activity:{userId}
     * @param expiredKey 过期的用户活跃度分值键名
     */
    private void handleUserActivityScoreKeyExpired(String expiredKey) {
        Integer userId = null;
        String[] keyParts = expiredKey.split(":");

        try {
            // 1. 校验键格式（仅支持固定格式：feature:score:user:activity:{userId}）
            if (keyParts.length != 5) {
                log.error("【Redis过期监听-用户】活跃度键格式错误（仅支持feature:score:user:activity:{userId}），键名：{}", expiredKey);
                return;
            }

            // 2. 解析并校验userId（Assert简化合法性校验）
            userId = Integer.parseInt(keyParts[4]);
            Assert.isTrue(userId > 0, "userId需为正整数，当前值：{}", userId);

            // ===================== 核心改造：发布本地事件（替代Kafka） =====================
            eventPublisher.publishEvent(new UserOfflineTooLongEvent(this, userId));

            log.info("【Redis过期监听-用户】用户活跃度键过期，已发布离线超时事件，userId={}，键名：{}",
                    userId, expiredKey);

        } catch (NumberFormatException e) {
            log.error("【Redis过期监听-用户】活跃度键中userId转换失败（非数字），键名：{}", expiredKey, e);
        } catch (IllegalArgumentException e) {
            // 捕获Assert的参数不合法异常
            log.error("【Redis过期监听-用户】活跃度键中userId不合法，{}，键名：{}", e.getMessage(), expiredKey);
        } catch (Exception e) {
            log.error("【Redis过期监听-用户】处理活跃度键过期失败，键名：{}", expiredKey, e);
        }
    }

    /**
     * 新增：处理【AI】活跃度分值键过期逻辑（匹配AI的Redis Key构造规则）
     * 格式：emotion:score:ai:activity:{userId}（仅userId，无sessionId）
     * @param expiredKey 过期的AI活跃度分值键名
     */
    private void handleAiActivityScoreKeyExpired(String expiredKey) {
        Integer userId = null;
        String[] keyParts = expiredKey.split(":");

        try {
            // 1. 校验AI键格式（仅支持：emotion:score:ai:activity:{userId} → 长度=5）
            if (keyParts.length != 5) {
                log.error("【Redis过期监听-AI】活跃度键格式错误，键名：{}", expiredKey);
                return;
            }

            // 2. 解析userId并校验合法性
            userId = Integer.parseInt(keyParts[4]);
            if (userId <= 0) {
                log.error("【Redis过期监听-AI】活跃度键中userId不合法，userId={}，键名：{}", userId, expiredKey);
                return;
            }

            // ===================== 核心改造：发布本地事件（替代Kafka） =====================
            eventPublisher.publishEvent(new AiWaitTooLongEvent(this, userId));

            log.info("【Redis过期监听-AI】AI活跃度键过期，已发布等待超时事件，userId={}，键名：{}",
                    userId, expiredKey);

        } catch (NumberFormatException e) {
            log.error("【Redis过期监听-AI】活跃度键参数转换失败，键名：{}", expiredKey, e);
        } catch (Exception e) {
            log.error("【Redis过期监听-AI】处理活跃度键过期失败，键名：{}", expiredKey, e);
        }
    }
}