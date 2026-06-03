package org.myfx.controls.aione.AiService.service.facade.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.dto.AiChatDTO;
import org.myfx.controls.aione.AiService.dto.redis.AiChatQueueTask;
import org.myfx.controls.aione.AiService.service.facade.ChatTaskService;
import org.myfx.controls.aione.AiService.service.facade.FluxChatService;
import org.myfx.controls.aione.AiService.utils.AiChatQueueRedisUtil;
import org.myfx.controls.aione.ServiceCommon.context.UserContext;
import org.myfx.controls.aione.ServiceCommon.utils.SnowflakeGenerator;
import org.redisson.api.RLockReactive;
import org.redisson.api.RedissonReactiveClient;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
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
        // 暂时为了测试TG，所以正常逻辑被我注释了，万一TG调试完毕，可以修改回来
        // Integer userId = aiChatDTO.getUserId();
        Integer userId = UserContext.getUserId();
        Long userSendTimestamp = aiChatDTO.getUserSendTimestamp();
        String userSessionKey = STR."\{userId}:\{sessionUuid}"; // 会话唯一标识
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

    @Override
    public void createAndSubmitAiChatTask(String message) {
        // 1. 初始化AiChatDTO
        AiChatDTO aiChatDTO = new AiChatDTO();

        // ===================== 核心：全自动组装字段（对齐控制器） =====================
        // 会话UUID（自动生成）
        aiChatDTO.setSessionUuid("f8b12087-af0b-4370-bad5-84f41b6bd6a7");

        // 流式任务ID（自动生成）
        aiChatDTO.setTaskId(UUID.randomUUID().toString());

        // 用户消息（唯一入参）
        aiChatDTO.setMessage(message);

        // 角色ID（默认1）
        aiChatDTO.setRoleId(1);

        // 用户消息时间戳（当前系统时间）
        aiChatDTO.setUserSendTimestamp(System.currentTimeMillis());

        // 用户消息ID（雪花ID生成）
        aiChatDTO.setUserMessageId(SnowflakeGenerator.generateId());

        // 非主动消息
        aiChatDTO.setIsActiveMessage(Boolean.FALSE);

        // 用户ID【硬编码→待填写】（从上下文获取/手动指定，你后续补充）
        aiChatDTO.setUserId(2);

        // ===================== 提交任务到队列（和控制器完全一致） =====================
        addAiChatTaskToQueue(aiChatDTO);
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
        String lockKey = STR."lock:ai:chat:queue:\{userId}:\{sessionUuid}";
        String userSessionKey = STR."\{userId}:\{sessionUuid}";
        RLockReactive lock = redissonReactiveClient.getLock(lockKey);

        log.info("【队列调度】开始调度 → userId:{}, lockKey:{}", userId, lockKey);

        return Mono.usingWhen(
                        // 1. 尝试抢锁：抢锁成功则向下传递 lock 对象，失败则走空流
                        lock.tryLock(0, -1, TimeUnit.SECONDS)
                                .flatMap(success -> {
                                    if (success) {
                                        log.info("【队列调度】✅ 抢锁成功，准备执行队列...");
                                        return Mono.just(lock); // 抢锁成功，把锁传给下一步
                                    } else {
                                        log.warn("【队列调度】❌ 抢锁失败，当前已有任务在执行");
                                        return Mono.empty();    // 抢锁失败，直接中止下游
                                    }
                                }),

                        // 2. 执行业务：只有抢锁成功（拿到 lock）才会走到这里
                        currentLock -> processQueue(userId, sessionUuid),

                        // 3. 正常完成解锁
                        currentLock -> safeUnlock(currentLock, "正常完成"),

                        // 4. 异常时解锁
                        (currentLock, err) -> safeUnlock(currentLock, "执行异常"),

                        // 5. 取消时解锁
                        currentLock -> safeUnlock(currentLock, "流程取消")
                )
                // 6. 全局异常与清理兜底
                .onErrorResume(e -> {
                    log.error("【队列调度】全局异常兜底", e);
                    return Mono.empty();
                })
                .doFinally(signalType -> {
                    // 无论如何，最终移除内存中的执行标记
                    CURRENT_EXECUTING_TASK.remove(userSessionKey);
                });
    }

    /**
     * 真正安全的响应式解锁逻辑（与线程完全解耦）
     */
    private Mono<Void> safeUnlock(RLockReactive lock, String triggerType) {
        return lock.unlock()
                .doOnSubscribe(s -> log.info("【队列调度·解锁】触发源: [{}], 开始释放锁: {}", triggerType, lock.getName()))
                .doOnSuccess(v -> log.info("【队列调度·解锁】✅ 锁释放成功"))
                .onErrorResume(IllegalMonitorStateException.class, e -> {
                    // 解释：如果业务太慢导致锁超时被 Redis 删了，Redisson 解锁会抛这个错，直接无视即可
                    log.warn("【队列调度·解锁】⚠️ 锁可能已超时自动释放，无需重复解锁");
                    return Mono.empty();
                })
                .onErrorResume(e -> {
                    log.error("【队列调度·解锁】❌ 解锁时发生未知网络异常", e);
                    return Mono.empty();
                });
    }

    private Mono<Void> processQueue(Integer userId, String sessionUuid) {
        String userIdStr = userId.toString();
        String userSessionKey = STR."\{userId}:\{sessionUuid}";

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
                    if (Boolean.FALSE.equals(result)) {
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
                // 主任务开始时，同时订阅取消监听（不阻塞）
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