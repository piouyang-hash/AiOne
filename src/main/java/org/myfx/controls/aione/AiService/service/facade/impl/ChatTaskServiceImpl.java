package org.myfx.controls.aione.AiService.service.facade.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.dto.AiChatDTO;
import org.myfx.controls.aione.AiService.dto.redis.AiChatQueueTask;
import org.myfx.controls.aione.AiService.service.facade.ChatTaskService;
import org.myfx.controls.aione.AiService.service.facade.FluxChatService;
import org.myfx.controls.aione.AiService.utils.AiChatQueueRedisUtil;
import org.myfx.controls.aione.ServiceCommon.context.UserContext;
import org.redisson.api.RLockReactive;
import org.redisson.api.RedissonReactiveClient;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AI聊天任务服务实现类
 */
@Service
@Slf4j
public class ChatTaskServiceImpl implements ChatTaskService {

    /**
     * 流式对话任务容器：key=sessionUuid:taskId，value=可取消的订阅对象
     * ConcurrentHashMap 保证线程安全
     */
    private static final ConcurrentHashMap<String, Disposable> CHAT_STREAM_TASK_MAP = new ConcurrentHashMap<>();

    // Key: uniqueKey, Value: 信号触发器
    private final Map<String, Sinks.Empty<Void>> CHAT_CANCELLER_MAP = new ConcurrentHashMap<>();

    // ==================== 【新增】打断/取消核心状态 ====================
    // TODO ====================== 【重大集群隐患】======================
    // 以下三个 Map 均为 【本地JVM内存状态】，集群多实例部署时完全失效！！！
    // 问题：分布式锁是Redis集群级，但任务状态是本地内存，实例间不互通
    // 后果：集群下会出现「假锁、取消失败、重复执行、任务错乱」，分布式锁形同虚设
    // 优化方案：将所有状态迁移到 Redis 集中式存储 (Hash/Set)，实现集群状态同步
    // =================================================================

    /**
     * 1. 记录【每个会话当前正在执行的任务唯一标识】(key: userId_sessionUuid, value: uniqueKey)
     * TODO 集群致命问题：本地内存，多实例之间状态不共享，无法跨实例取消任务
     */
    private static final ConcurrentHashMap<String, String> CURRENT_EXECUTING_TASK = new ConcurrentHashMap<>();

    /**
     * 2. 取消锁(防止重复取消，保证cancel操作原子性、不被再次打断) (key: userId_sessionUuid, value: 锁标记)
     * TODO 集群致命问题：本地内存锁，仅单实例有效，集群下无法防止跨实例重复取消
     */
    private static final ConcurrentHashMap<String, Boolean> CANCEL_LOCK_MAP = new ConcurrentHashMap<>();

    /**
     * 存储每个任务的完成 Mono
     * TODO 集群问题：本地内存，跨实例无法感知任务完成状态
     */
    private final Map<String, Sinks.One<Void>> TASK_COMPLETION_MAP = new ConcurrentHashMap<>();

    // ====================== 核心配置：Redis键前缀设计 ======================
    /**
     * Redis键前缀：规范命名，区分业务模块
     * 最终key格式：ai:chat:stream:{sessionUuid}
     */
    private static final String AI_CHAT_STREAM_KEY_PREFIX = "ai:chat:stream:";

    /**
     * Redis缓存过期时间：10分钟（防止无用数据堆积）
     */
    private static final Duration CACHE_EXPIRE_TIME = Duration.ofMinutes(10);

    // 注入 Redisson 客户端（用于分布式锁）
    @Resource
    private RedissonReactiveClient redissonReactiveClient;

    // ====================== 依赖注入 ======================
    /**
     * 响应式AI聊天服务
     */
    @Resource
    private FluxChatService fluxChatService;

    @Resource
    private AiChatQueueRedisUtil aiChatQueueRedisUtil;

    @Override
    public void addAiChatTaskToQueue(AiChatDTO aiChatDTO) {
        String sessionUuid = aiChatDTO.getSessionUuid();
        String taskId = aiChatDTO.getTaskId();
        String message = aiChatDTO.getMessage();
        Integer roleId = aiChatDTO.getRoleId();
        Integer userId = UserContext.getUserId();
        Long userSendTimestamp = aiChatDTO.getUserSendTimestamp();
        String userSessionKey = userId + ":" + sessionUuid; // 会话唯一标识
        Long userMessageId = aiChatDTO.getUserMessageId();
        boolean isActiveMessage = aiChatDTO.getIsActiveMessage();

        log.info("========================================");
        log.info("【外部入口】收到新消息 → userId:{} session:{} taskId:{}", userId, sessionUuid, taskId);
        log.info("【外部入口】消息内容:{}", message);

        if (roleId == null) roleId = 1;

        AiChatQueueTask task = new AiChatQueueTask();
        task.setTaskId(taskId);
        task.setMessage(message);
        task.setRoleId(roleId);
        task.setUserSendTimestamp(userSendTimestamp);
        task.setUserMessageId(userMessageId);
        task.setIsActiveMessage(isActiveMessage);

        cancelCurrentTaskIfRunning(userSessionKey)   // 只取消当前执行的任务
                // 不再调用 clearQueue，直接入队新任务
                .then(aiChatQueueRedisUtil.enqueueTask(userId.toString(), sessionUuid, task))
                .doOnSuccess(v -> log.info("【入队成功】最新任务已存入Redis taskId:{}", taskId))
                // 如果当前没有正在执行的任务，则立即启动队列执行；否则新任务会排队等待
                .then(Mono.defer(() -> {
                    // 检查是否有任务正在执行（通过 CURRENT_EXECUTING_TASK 或 Redis 锁状态）
                    if (CURRENT_EXECUTING_TASK.containsKey(userSessionKey)) {
                        log.info("【队列调度】当前已有任务执行中，新任务进入排队");
                        return Mono.empty();
                    }
                    return executeChatQueueReactive(userId, sessionUuid);
                }))
                .subscribe();
    }

    /**
     * 【核心】原子取消当前执行任务（加锁防重入，确保cancel不会被重复触发）
     */
    private Mono<Void> cancelCurrentTaskIfRunning(String userSessionKey) {
        // 防止重复取消（同一个 userSessionKey 在同一时间只允许一个取消流程）
        if (CANCEL_LOCK_MAP.putIfAbsent(userSessionKey, true) != null) {
            log.warn("【取消任务】⚠️ 取消操作执行中，不可重入 → {}", userSessionKey);
            return Mono.empty();
        }

        String currentExecutingTask = CURRENT_EXECUTING_TASK.get(userSessionKey);
        if (currentExecutingTask == null) {
            log.info("【取消任务】ℹ️ 无正在执行的任务，无需取消");
            CANCEL_LOCK_MAP.remove(userSessionKey);
            return Mono.empty();
        }

        log.info("【取消任务】🚨 检测到新任务，立即发送取消信号 → {}", currentExecutingTask);
        Sinks.Empty<Void> cancelSink = CHAT_CANCELLER_MAP.get(currentExecutingTask);
        if (cancelSink == null) {
            log.warn("【取消任务】⚠️ 取消开关不存在，直接清理状态");
            CANCEL_LOCK_MAP.remove(userSessionKey);
            return Mono.empty();
        }

        // 立即发送取消信号（不等待）
        return Mono.fromRunnable(() -> {
                    Sinks.EmitResult result = cancelSink.tryEmitEmpty();
                    if (result.isSuccess()) {
                        log.info("【取消任务】✅ 已成功发送取消信号（不等待结束） → {}", currentExecutingTask);
                    } else {
                        log.warn("【取消任务】⚠️ 发送取消信号失败: {}", result);
                    }
                })
                // 关键改动：不再等待 completionSink，直接完成
                .doFinally(signal -> {
                    // 释放取消锁，允许后续请求再次尝试取消
                    CANCEL_LOCK_MAP.remove(userSessionKey);
                    log.info("【取消任务】ℹ️ 取消锁已释放");
                })
                .then(); // 返回 Mono<Void> 且立即完成
    }

    /**
     * 响应式队列执行（锁持有到整个队列执行完毕才释放）
     */
    private Mono<Void> executeChatQueueReactive(Integer userId, String sessionUuid) {
        String lockKey = "lock:ai:chat:queue:" + userId + ":" + sessionUuid;
        String userSessionKey = userId + ":" + sessionUuid;
        RLockReactive lock = redissonReactiveClient.getLock(lockKey);

        // 基础初始化日志
        log.info("【队列调度】初始化 → userId:{} lockKey:{}", userId, lockKey);

        return Mono.usingWhen(
                        // ========== 1. 资源创建：抢锁（超详细日志） ==========
                        Mono.defer(() -> {
                            long currentThreadId = Thread.currentThread().getId();
                            // 打印：当前线程 + 抢锁参数（核心）
                            log.info("【队列调度·抢锁】线程ID:{} | 开始尝试抢锁", currentThreadId);
                            log.info("【队列调度·抢锁】参数 → waitTime=0秒(不等待) leaseTime=-1(看门狗续期)");

                            // 执行抢锁，并打印原始结果
                            return lock.tryLock(0, -1, TimeUnit.SECONDS)
                                    .doOnSuccess(lockSuccess -> {
                                        log.info("【队列调度·抢锁】Redisson原始返回结果: {}", lockSuccess);
                                    })
                                    .map(lockSuccess -> new LockHolder(lock, currentThreadId, lockSuccess));
                        }),

                        // ========== 2. 资源使用：执行业务 ==========
                        holder -> {
                            if (!holder.lockSuccess) {
                                // 抢锁失败，额外打印：锁当前是否被持有（排查关键！）
                                return holder.lock.isLocked()
                                        .doOnNext(locked -> log.warn("【队列调度】❌ 抢锁失败 | 锁当前状态: 已被持有={}", locked))
                                        .then(Mono.empty());
                            }
                            log.info("【队列调度】✅ 抢锁成功 | 加锁线程ID:{} | 开始执行队列", holder.threadId);
                            return processQueue(userId, sessionUuid);
                        },

                        // ========== 3. 资源释放：正常完成解锁 ==========
                        holder -> {
                            log.info("【队列调度·解锁】触发：正常执行完成，准备解锁");
                            return holder.lockSuccess ? safeUnlock(holder) : Mono.empty();
                        },

                        // ========== 4. 资源释放：异常解锁 ==========
                        (holder, err) -> {
                            log.error("【队列调度·解锁】触发：执行异常，准备解锁", err);
                            return holder.lockSuccess ? safeUnlock(holder) : Mono.empty();
                        },

                        // ========== 5. 资源释放：取消解锁 ==========
                        holder -> {
                            log.info("【队列调度·解锁】触发：流程被取消，准备解锁");
                            return holder.lockSuccess ? safeUnlock(holder) : Mono.empty();
                        }
                )
                .onErrorResume(e -> {
                    log.error("【队列调度】全局异常兜底", e);
                    CURRENT_EXECUTING_TASK.remove(userSessionKey);
                    return Mono.empty();
                });
    }

    private record LockHolder(RLockReactive lock, long threadId, boolean lockSuccess) { }

    /**
     * 【终极安全解锁】严格使用你提供的API，无任何无效方法
     * 1. 校验线程所有权
     * 2. 正常解锁
     * 3. 兜底强制解锁（彻底删除Redis锁，永不残留）
     */
    private Mono<Void> safeUnlock(LockHolder holder) {
        RLockReactive lock = holder.lock;
        long lockThreadId = holder.threadId;
        String lockKey = lock.getName();
        long currentThreadId = Thread.currentThread().getId();

        // =============== 超详细诊断日志 ===============
        log.info("【解锁检查】lockKey:{}", lockKey);
        log.info("【解锁检查】加锁线程ID:{} | 当前解锁线程ID:{}", lockThreadId, currentThreadId);
        log.info("【解锁检查】线程是否一致:{}", lockThreadId == currentThreadId);

        // 1. 检查锁是否存在
        return lock.isLocked()
                .doOnNext(locked -> log.info("【解锁检查】Redis中锁是否存在:{}", locked))
                .flatMap(locked -> {
                    if (!locked) {
                        log.info("【解锁】锁已不存在，无需操作");
                        return Mono.empty();
                    }

                    // 2. 检查是否被当前线程持有（用你提供的API）
                    return lock.isHeldByThread(lockThreadId)
                            .doOnNext(held -> log.info("【解锁检查】当前线程是否持有锁:{}", held))
                            .flatMap(held -> {
                                Mono<Void> unlockMono;
                                if (held) {
                                    // 有权限：正常解锁
                                    unlockMono = lock.unlock(lockThreadId)
                                            .doOnSuccess(v -> log.info("【解锁】✅ 正常解锁成功:{}", lockKey));
                                } else {
                                    // 无权限：线程切换导致，直接强制解锁（核心！）
                                    // .then() 把 Mono<Boolean> 转为 Mono<Void>，解决类型红线
                                    unlockMono = lock.forceUnlock()
                                            .doOnSuccess(v -> log.info("【解锁】⚠️ 线程不一致，强制解锁成功:{}", lockKey))
                                            .then();
                                }

                                // 统一捕获异常，永不报错
                                return unlockMono
                                        .onErrorResume(e -> {
                                            log.error("【解锁】❌ 解锁失败，强制兜底清理锁:{}", lockKey, e);
                                            return lock.forceUnlock().then();
                                        });
                            });
                });
    }

    private Mono<Void> processQueue(Integer userId, String sessionUuid) {
        String userIdStr = userId.toString();
        String userSessionKey = userId + ":" + sessionUuid;

        // 1. 触发初始任务
        return aiChatQueueRedisUtil.getQueueSize(userIdStr, sessionUuid)
                .filter(size -> size > 0)
                .flatMap(size -> executeSingleTask(userId, sessionUuid))
                // 2. 使用 expand 递归处理后续任务
                .expand(lastTaskResult -> aiChatQueueRedisUtil.getQueueSize(userIdStr, sessionUuid)
                        .flatMap(size -> {
                            if (size > 0) {
                                return executeSingleTask(userId, sessionUuid);
                            }
                            return Mono.empty(); // 队列空了，停止 expand
                        }))
                .then() // 将 Flux 转换为 Mono<Void>
                .doFinally(signal -> {
                    CURRENT_EXECUTING_TASK.remove(userSessionKey);
                    log.info("【队列调度】所有任务处理完毕，清理状态");
                });
    }

    /**
     * 核心逻辑提取：只负责拉取、执行并删除【一个】任务
     */
    private Mono<Boolean> executeSingleTask(Integer userId, String sessionUuid) {
        String userIdStr = userId.toString();
        String userSessionKey = userId + ":" + sessionUuid;

        return aiChatQueueRedisUtil.peekFirstTask(userIdStr, sessionUuid)
                .flatMap(currentTask -> {
                    // ========== 打印1：成功从队列拿到任务 ==========
                    log.info("【executeSingleTask】拿到队列首任务，taskId:{}，userId:{}，sessionUuid:{}",
                            currentTask.getTaskId(), userId, sessionUuid);

                    String taskUniqueKey = sessionUuid + ":" + currentTask.getTaskId();
                    CURRENT_EXECUTING_TASK.put(userSessionKey, taskUniqueKey);

                    // 构建 DTO 等逻辑...
                    AiChatDTO dto = new AiChatDTO();
                    dto.setSessionUuid(sessionUuid);
                    dto.setTaskId(currentTask.getTaskId());
                    dto.setMessage(currentTask.getMessage());
                    dto.setRoleId(currentTask.getRoleId());
                    dto.setUserId(userId);
                    dto.setUserSendTimestamp(currentTask.getUserSendTimestamp());
                    dto.setUserMessageId(currentTask.getUserMessageId());
                    dto.setIsActiveMessage(currentTask.getIsActiveMessage());

                    return startAiStreamChatTask(dto)
                            .onErrorResume(e -> {
                                // ========== 打印2：任务执行报错 ==========
                                log.error("【executeSingleTask】任务执行失败，taskId:{}", currentTask.getTaskId(), e);
                                return Mono.empty();
                            })
                            // ========== 打印3：准备删除队列首任务 ==========
                            .then(Mono.fromRunnable(() -> {
                                log.info("【executeSingleTask】准备删除队列首任务，taskId:{}", currentTask.getTaskId());
                            }))
                            // 执行完删除当前任务
                            .then(aiChatQueueRedisUtil.removeFirstTask(userIdStr, sessionUuid))
                            // ========== 打印4：任务+删除都执行完成 ==========
                            .thenReturn(true)
                            .doOnSuccess(result -> {
                                log.info("【executeSingleTask】任务处理完成，已删除队列任务，返回结果:{}", result);
                            });
                })
                // ========== 打印5：队列空，没有拿到任务 ==========
                .defaultIfEmpty(false)
                .doOnSuccess(result -> {
                    if (!result) {
                        log.info("【executeSingleTask】队列无任务，直接返回结果:{}", result);
                    }
                });
    }

    /**
     * 真正执行AI流式对话
     */
    @Override
    public Mono<Void> startAiStreamChatTask(AiChatDTO aiChatDTO) {
        String uniqueKey = aiChatDTO.getSessionUuid() + ":" + aiChatDTO.getTaskId();

        // 1. 注册取消与完成信号器
        Sinks.Empty<Void> cancelSink = Sinks.empty();
        CHAT_CANCELLER_MAP.put(uniqueKey, cancelSink);
        Sinks.One<Void> completionSink = Sinks.one();
        TASK_COMPLETION_MAP.put(uniqueKey, completionSink);

        // 2. 取消控制状态
        AtomicBoolean cancelRequested = new AtomicBoolean(false);
        final int POST_CANCEL_CHUNKS = 1;
        AtomicInteger producerRemaining = new AtomicInteger(0);

        // 3. 取消信号监听（仅监听，不阻塞主流程）
        Mono<Void> cancelHandler = cancelSink.asMono()
                .doOnSuccess(v -> {
                    log.info("【流式任务】[{}] 收到取消信号，上游将继续生成 {} 个分片后终止", uniqueKey, POST_CANCEL_CHUNKS);
                    cancelRequested.set(true);
                    producerRemaining.set(POST_CANCEL_CHUNKS);
                });

        // 4. 主任务逻辑
        Mono<Void> mainTask = fluxChatService.newStreamChatWithStorageAndPush(aiChatDTO)
                .takeWhile(chunk -> {
                    if (!cancelRequested.get()) {
                        return true;
                    }
                    int left = producerRemaining.getAndDecrement();
                    boolean shouldContinue = left > 0;
                    if (!shouldContinue) {
                        log.info("【流式任务】[{}] 已达到额外生成数量，主动终止流", uniqueKey);
                    }
                    return shouldContinue;
                })
                .then(Mono.defer(() -> {
                    if (cancelRequested.get()) {
                        log.info("【流式任务】[{}] 额外分片已处理完毕，现在发送取消结束帧", uniqueKey);
                        fluxChatService.sendCancellationFrame(uniqueKey,aiChatDTO.getUserId());
                    }
                    return Mono.empty();
                }))
                // 🌟 关键修复：主任务开始时，同时订阅取消监听（不阻塞）
                .doOnSubscribe(subscription -> cancelHandler.subscribe()).then();

        // 5. 只等待主任务完成！取消监听是后台运行，不阻塞流程
        return mainTask
                .doFinally(signal -> {
                    CHAT_CANCELLER_MAP.remove(uniqueKey);
                    TASK_COMPLETION_MAP.remove(uniqueKey);
                    log.info("【流式任务】[{}] 全部流程完成，最终清理，信号类型: {}", uniqueKey, signal);
                    completionSink.tryEmitEmpty();
                });
    }

    /**
     * 🔥 停止AI流式对话（核心：取消订阅 + 清理资源）
     * @param sessionUuid 会话ID
     * @param taskId 任务ID
     */
    public void stopAiStreamChatTask(String sessionUuid, String taskId) {
        String uniqueKey = sessionUuid + ":" + taskId;
        Sinks.Empty<Void> sink = CHAT_CANCELLER_MAP.get(uniqueKey);

        if (sink != null) {
            // 触发信号，上面的 takeUntilOther 会立刻感知并停止流
            sink.tryEmitEmpty();
            log.info("【流式任务】遥控取消信号已发出: {}", uniqueKey);
        }
    }


    // ====================== 【新增】AI主动消息专用流式任务实现 ======================
    @Override
    public void startAiStreamChatTaskForAiActive(AiChatDTO aiChatDTO) {
//        // 1. 参数提取（完全不变）
//        String sessionUuid = aiChatDTO.getSessionUuid();
//        String taskId = aiChatDTO.getTaskId();
//        Integer roleId = aiChatDTO.getRoleId();
//        if (roleId == null) roleId = 1;
//
//        String uniqueKey = sessionUuid + ":" + taskId;
//        String redisKey = AI_CHAT_STREAM_KEY_PREFIX + uniqueKey;
//
//        // 2. 响应式流 + 响应式Redis（核心：调用主动消息专用接口）
//        Disposable disposable = fluxChatService.summarySlidingWindowChatForAiActive(aiChatDTO)
//                .doOnNext(chunk -> {
//                    // 响应式Redis存储（不变）
//                    reactiveRedisTemplate.opsForList()
//                            .rightPush(redisKey, chunk)
//                            .then(reactiveRedisTemplate.expire(redisKey, CACHE_EXPIRE_TIME))
//                            .subscribe();
//
//                    // WebSocket推送（不变）
//                    if (sessionManager.isConnected(uniqueKey)) {
//                        webSocketHandler.sendMessage(sessionManager.getSession(uniqueKey), chunk);
//                    }
//                })
//                .doOnCancel(() -> {
//                    CHAT_STREAM_TASK_MAP.remove(uniqueKey);
//                    log.info("【AI主动消息-流式任务】✅ 主动取消成功：{}", uniqueKey);
//                })
//                .doOnComplete(() -> {
//                    CHAT_STREAM_TASK_MAP.remove(uniqueKey);
//                    log.info("【AI主动消息-流式任务】正常结束：{}", uniqueKey);
//                })
//                .doOnError(e -> {
//                    CHAT_STREAM_TASK_MAP.remove(uniqueKey);
//                    log.error("【AI主动消息-流式任务】异常终止：{}", uniqueKey, e);
//                })
//                .subscribe();
//
//        CHAT_STREAM_TASK_MAP.put(uniqueKey, disposable);
        log.info("【AI主动消息-流式任务】已启动：" // uniqueKey
        );
    }
}