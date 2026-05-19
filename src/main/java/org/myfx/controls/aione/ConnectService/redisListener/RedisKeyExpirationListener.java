package org.myfx.controls.aione.ConnectService.redisListener;

import jakarta.annotation.Resource;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.ServiceCommon.event.publisher.UserStatusChangeEventPublisher;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Redis键过期事件监听器
 * 专门监听 connect:user:status:{userId}:{appType} 格式的键过期，并调用Kafka发送离线通知
 */
@Slf4j
@Component
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {

    // 注入Kafka生产者（替换为你实际的Kafka生产者类名）
    @Resource
    private UserStatusChangeEventPublisher userStatusChangeEventPublisher;;

    // 定义监听的键前缀（便于统一管理）
    private static final String CONNECT_USER_STATUS_PREFIX = "connect:user:status:";

    /**
     * 构造方法，注入监听容器
     */
    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    /**
     * 核心方法：监听并处理Redis键过期事件
     * 重点处理 connect:user:status:{userId}:{appType} 格式的过期键
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
            return;
        }

        // 2. 只处理以 connect:user:status: 开头的键
        if (expiredKey.startsWith(CONNECT_USER_STATUS_PREFIX)) {
            handleConnectUserStatusExpired(expiredKey);
        }
    }

    /**
     * 处理 connect:user:status:{userId}:{appType} 格式的键过期逻辑
     * @param expiredKey 过期的键名
     */
    private void handleConnectUserStatusExpired(String expiredKey) {
        try {
            // 3. 拆分键名，格式：connect:user:status:1:AI_CHAT → 拆分后数组长度应为5
            String[] keyParts = expiredKey.split(":");
            // 校验拆分后的长度，避免数组越界
            if (keyParts.length != 5) {
                log.error("过期键格式错误，不符合 connect:user:status:{userId}:{appType} 规范！键名：{}", expiredKey);
                return;
            }

            // 4. 提取userId和appType（字符串类型）
            String userIdStr = keyParts[3]; // 第4个元素（索引3）是userId，比如"1"
            String appTypeStr = keyParts[4]; // 第5个元素（索引4）是appType，比如"AI_CHAT"

            log.info("解析过期键成功：userId = {}, appType = {}", userIdStr, appTypeStr);

            // 5. 发布用户离线事件（替换原Kafka生产者调用）
            userStatusChangeEventPublisher.sendUserOfflineNotify(userIdStr, appTypeStr);

        } catch (Exception e) {
            // 捕获所有异常，避免影响其他监听逻辑
            log.error("处理 connect:user:status 过期键失败！键名：{}", expiredKey, e);
        }
    }
}