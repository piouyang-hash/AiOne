package org.myfx.controls.aione.AiService.utils;

import org.myfx.controls.aione.AiService.dto.redis.AiChatQueueTask;
import org.springframework.data.redis.core.ReactiveListOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * AI 聊天任务队列 Redis 工具类（响应式版，适配Project Reactor）
 * 基于 Redis List 实现任务排队 FIFO 先进先出
 * Key 规则：ai:chat:queue:{userId}:{sessionUuid}
 */
@Component
public class AiChatQueueRedisUtil {

    private static final String AI_CHAT_QUEUE_PREFIX = "ai:chat:queue:";
    private static final long DEFAULT_EXPIRE_SECONDS = 2 * 60 * 60;

    // 🔥 核心替换：同步 RedisTemplate → 响应式 ReactiveRedisTemplate
    private final ReactiveRedisTemplate<String, AiChatQueueTask> reactiveRedisTemplate;
    private final ReactiveListOperations<String, AiChatQueueTask> listOps;

    // 构造器注入响应式Redis模板
    public AiChatQueueRedisUtil(ReactiveRedisTemplate<String, AiChatQueueTask> reactiveRedisTemplate) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
        this.listOps = reactiveRedisTemplate.opsForList();
    }

    private String buildKey(String userId, String sessionUuid) {
        return AI_CHAT_QUEUE_PREFIX + userId + ":" + sessionUuid;
    }

    // ====================== 🚀 响应式核心方法 ======================

    /**
     * 1. 【入队】添加任务到队列尾部
     */
    public Mono<Void> enqueueTask(String userId, String sessionUuid, AiChatQueueTask task) {
        String key = buildKey(userId, sessionUuid);
        return listOps.rightPush(key, task)
                // 使用 Duration.ofSeconds 代替 long + TimeUnit
                .then(reactiveRedisTemplate.expire(key, Duration.ofSeconds(DEFAULT_EXPIRE_SECONDS)))
                .then();
    }

    /**
     * 2.1 读取队首任务（只看，不删除！）
     */
    public Mono<AiChatQueueTask> peekFirstTask(String userId, String sessionUuid) {
        String key = buildKey(userId, sessionUuid);
        // index(key, 0) = 读取列表第0个元素（队首），不删除
        return listOps.index(key, 0)
                // 刷新过期时间
                .flatMap(task -> reactiveRedisTemplate.expire(key, Duration.ofSeconds(DEFAULT_EXPIRE_SECONDS))
                        .thenReturn(task)
                );
    }

    /**
     * 2.2 【出队-删除】删除队首任务（执行成功后调用！）
     */
    public Mono<Void> removeFirstTask(String userId, String sessionUuid) {
        String key = buildKey(userId, sessionUuid);
        // leftPop = 删除队首元素
        return listOps.leftPop(key)
                .then(reactiveRedisTemplate.expire(key, Duration.ofSeconds(DEFAULT_EXPIRE_SECONDS)))
                .then();
    }

    /**
     * 3. 【查看】获取当前队列所有任务
     */
    public Flux<AiChatQueueTask> getQueueAllTasks(String userId, String sessionUuid) {
        String key = buildKey(userId, sessionUuid);
        return listOps.range(key, 0, -1);
    }

    /**
     * 4. 【查看】获取队列长度
     */
    public Mono<Long> getQueueSize(String userId, String sessionUuid) {
        String key = buildKey(userId, sessionUuid);
        return listOps.size(key);
    }

    /**
     * 5. 【清空】删除整个队列
     */
    public Mono<Boolean> clearQueue(String userId, String sessionUuid) {
        String key = buildKey(userId, sessionUuid);
        return reactiveRedisTemplate.delete(key)
                .map(count -> count > 0); // 将 Long 转换为 Boolean
    }
}