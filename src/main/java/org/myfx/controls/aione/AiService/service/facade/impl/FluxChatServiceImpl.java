package org.myfx.controls.aione.AiService.service.facade.impl;

import io.netty.channel.Channel;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.aiClient.AMTest.AiModelClient;
import org.myfx.controls.aione.AiService.aiClient.advisor.*;
import org.myfx.controls.aione.AiService.aiClient.advisor.dto.ChatInformationDTO;
import org.myfx.controls.aione.AiService.aiClient.advisor.dto.ChatStreamContentMetaVO;
import org.myfx.controls.aione.AiService.aiClient.advisor.dto.ChatStreamErrorMetaVO;
import org.myfx.controls.aione.AiService.aiClient.advisor.dto.ChatStreamMetaDTO;
import org.myfx.controls.aione.AiService.aiClient.advisor.score.ChatScoreAdvisor;
import org.myfx.controls.aione.AiService.aiClient.business.TokenCountingAdvisor;
import org.myfx.controls.aione.AiService.aiClient.llmModels.MainLlmClient;
import org.myfx.controls.aione.AiService.common.exception.TokenInsufficientException;
import org.myfx.controls.aione.AiService.dto.AiChatDTO;
import org.myfx.controls.aione.AiService.dto.ChatChunkDTO;
import org.myfx.controls.aione.AiService.service.facade.FluxChatService;
import org.myfx.controls.aione.AiService.utils.PromptTemplateReader;
import org.myfx.controls.aione.ConnectService.dto.WebSocketMessage;
import org.myfx.controls.aione.ConnectService.utils.ChannelManager;
import org.myfx.controls.aione.ServiceCommon.context.UserContext;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuples;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Component
@RequiredArgsConstructor
@Slf4j
public class FluxChatServiceImpl implements FluxChatService {
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

    @Resource(name = "aiChatChunkReactiveRedisTemplate")
    private ReactiveRedisTemplate<String, ChatChunkDTO> aiChatChunkReactiveRedisTemplate;

    private final MainLlmClient mainLlmClient;

    @Resource(name = "streamTestChatClient")
    private AiModelClient streamTestChatClient;

    private final ObjectMapper objectMapper;


    // 注入其他依赖（如Advisor、PromptTemplateReader等）
    // 所有的advisor
    private final ConversationStoreAdvisor conversationStoreAdvisor;

    private final SummarySlidingWindowAdvisor summarySlidingWindowAdvisor;

    private final SingleActiveSessionAdvisor singleActiveSessionAdvisor;

    private final ChatScoreAdvisor chatScoreAdvisor;

    private final StructuredDataAdvisor structureDataAdvisor;

    private final PersonaFillAdvisor personaFillAdvisor;

    private final PromptTemplateRenderAdvisor promptTemplateRenderAdvisor;

    private final AppendOnlyAdvisor appendOnlyAdvisor;

    private final GetLatestChatSessionAdvisor getLatestChatSessionAdvisor;

    private final GetConversationContextAdvisor getConversationContextAdvisor;

    private final CurrentExecutingEventAdvisor currentExecutingEventAdvisor;

    private final TokenCountingAdvisor tokenCountingAdvisor;

    // 1. 注入模板读取工具类（已修改为返回PromptTemplate）
    private final PromptTemplateReader promptTemplateReader;

    private final ChannelManager channelManager;

    @Override
    public void sendCancellationFrame(String uniqueKey, Integer userId) {
        // 2. 新增：userId 空值校验（外部传入，直接使用）
        if (userId == null) {
            log.warn("【取消帧】[{}] userId为空，跳过取消帧推送", uniqueKey);
            return;
        }

        // 3. 拆分 uniqueKey 获取 sessionUuid/taskId（保留原有逻辑，仅用于构建取消帧）
        String[] keyParts = uniqueKey.split(":", 2);
        String sessionUuid = keyParts.length > 0 ? keyParts[0] : null;
        String taskId = keyParts.length > 1 ? keyParts[1] : null;

        try {
            // 4. 构建取消帧（完全对齐原有格式，无修改）
            ChatChunkDTO cancelFrame = new ChatChunkDTO(
                    sessionUuid,
                    taskId,
                    false,
                    true
            );

            // ===================== 核心修改：Channel 推送逻辑 =====================
            Channel userChannel = channelManager.getChannel(userId);
            if (userChannel != null && userChannel.isActive()) {
                // 直接推送取消帧（和你正常消息推送逻辑完全一致）
                userChannel.writeAndFlush(WebSocketMessage.aiPush(cancelFrame));
                log.info("【取消帧】[{}] 用户{}推送成功", uniqueKey, userId);
            } else {
                log.warn("❌ 用户{}不在线，跳过WebSocket取消帧推送", userId);
            }

        } catch (Exception e) {
            log.error("【取消帧】[{}] 用户{}推送异常", uniqueKey, userId, e);
        }
    }

    // 假设你已有 ChatChunkDTO 类，此处仅聚焦核心替换逻辑
    // @Override 需确保该类实现了修改后的 FluxChatService 接口
    @Override
    public Flux<String> mainInterfaceChat(String msg, String sessionUuid) {
        // 复用流式执行器逻辑 (Reuse the streaming executor logic)
        List<Advisor> advisors = null;
        return executeChatStream(msg, sessionUuid, "main-template.txt", advisors, "MAIN_INTERFACE_FLUX");
    }

    @Override
    public Flux<String> slidingWindowChat(String msg, String sessionUuid) {
        return null;
    }

    // ===================== 🔥 新版：无手动序列化，纯DTO处理 =====================
    @Override
    public Flux<String> newStreamChatWithStorageAndPush(AiChatDTO aiChatDTO) {
        String uniqueKey = aiChatDTO.getSessionUuid() + ":" + aiChatDTO.getTaskId();
        String redisKey = AI_CHAT_STREAM_KEY_PREFIX + uniqueKey;
        Integer userId = aiChatDTO.getUserId();

        // 原有AI流式流（内部已封装错误为标准ChatChunkDTO，带isError=true）
        Flux<ChatChunkDTO> originalFlux = summarySlidingWindowChatForAiActive(aiChatDTO);

        return originalFlux
                .publishOn(Schedulers.boundedElastic())
                // ===================== Redis 自动存储：错误Chunk也会正常存储 =====================
                .concatMap(chunk ->
                        aiChatChunkReactiveRedisTemplate.opsForList()
                                .rightPush(redisKey, chunk)
                                .then(aiChatChunkReactiveRedisTemplate.expire(redisKey, CACHE_EXPIRE_TIME))
                                .thenReturn(chunk)
                                .onErrorResume(e -> {
                                    log.error("Redis 存储失败，继续推送消息", e);
                                    return Mono.just(chunk);
                                })
                )
                // ===================== WebSocket 自动推送：错误Chunk也会正常推送 =====================
                .doOnNext(chunk -> {
                    try {
                        Channel userChannel = channelManager.getChannel(userId);
                        if (userChannel != null && userChannel.isActive()) {
                            // ✅ 正常推送：错误消息/正常消息 统一走这里！
                            userChannel.writeAndFlush(WebSocketMessage.aiPush(chunk));
                        } else {
                            log.warn("❌ 用户{}不在线，跳过WebSocket推送", userId);
                        }
                    } catch (Exception e) {
                        log.error("❌ WebSocket推送异常，userId:{}", userId, e);
                    }
                })
                // ===================== 转为字符串返回 =====================
                .map(chunk -> {
                    return new ObjectMapper().writeValueAsString(chunk);
                }).doOnError(Exception.class, e -> {
                    log.error("AI 流式处理异常", e);

                });
    }

    @Override
    public Flux<ChatChunkDTO> summarySlidingWindowChat(AiChatDTO aiChatDTO) {
        // 核心：直接传入DTO，无多余参数
        return executeChatStreamWithContext(
                aiChatDTO,
                "SSW.txt",
                Arrays.asList(
                        singleActiveSessionAdvisor,
                        summarySlidingWindowAdvisor,
                        promptTemplateRenderAdvisor,
                        tokenCountingAdvisor,
                        conversationStoreAdvisor,
                        new MySmartSplitterAdvisor(20),
                        chatScoreAdvisor
                ),
                "总结型滑动窗口"
        );
    }

    @Override
    public Flux<ChatChunkDTO> summarySlidingWindowChatForAiActive(AiChatDTO aiChatDTO) {
        // 🔥 AI主动消息：无UserContext，直接使用外部预填充的userId，不做任何修改
        return executeChatStreamWithContext(
                aiChatDTO,
                "AEDSSW.txt",
                Arrays.asList(
                        singleActiveSessionAdvisor,
                        currentExecutingEventAdvisor,
                        personaFillAdvisor,
                        summarySlidingWindowAdvisor,
                        promptTemplateRenderAdvisor,
                        tokenCountingAdvisor,
                        conversationStoreAdvisor,
                        new MySmartSplitterAdvisor(20)
                ),
                "AI主动消息-总结型滑动窗口" // 标记专属日志标签，区分场景
        );
    }

    @Override
    public Flux<String> appendOnlyChat(String msg, String sessionUuid) {
        return null;
    }

    @Override
    public Flux<String> eventDrivenMainInterfaceChat(String msg, String sessionUuid) {
        return null;
    }

    // 其他方法（slidingWindowChat/summarySlidingWindowChat等）类似，仅调整模板/Advisor配置
    // Other methods (slidingWindowChat/summarySlidingWindowChat, etc.) are similar, only adjust the template/Advisor configuration

    @Override
    public Flux<String> callApi(String msg, String sessionUuid) {
        return mainInterfaceChat(msg, sessionUuid); // 核心API调用复用主接口逻辑 (Core API call reuses main interface logic)
    }

    @Override
    public Flux<String> callApi(String msg) {
        return callApi(msg, null); // 重载方法默认传null会话UUID (Overload method passes null session UUID by default)
    }


    /**
     * 流式 AI 调用执行器（和同步版本逻辑完全对齐，仅返回流式响应）
     * Streaming AI call executor (Fully aligned with the synchronous version logic, only returns streaming responses)
     */
    private Flux<String> executeChatStream(String msg, String sessionUuid, String templateFile, List<Advisor> advisors, String logTag) {
        try {
            // 1. 公共逻辑：读取模板和获取上下文（和同步版本完全一致）
            // 1. Common logic: Read template and get context (Fully consistent with the synchronous version)
            PromptTemplate promptTemplate = promptTemplateReader.readTemplateFile(templateFile);
            Integer userId = UserContext.getUserId();
            // 处理UUID空值：null时设为默认标识（替代原-1L，因UUID是字符串）
            // Handle UUID null value: set to default identifier when null (replace original -1L, since UUID is a string)
            String finalSessionUuid = (sessionUuid == null) ? "default-uuid-placeholder" : sessionUuid;

            log.debug("开始流式调用AI模型[{}]，用户ID：{}，会话UUID：{}，消息：{}",
                    logTag, userId, finalSessionUuid, msg);

            // 2. 链式调用：替换同步call()为流式stream()
            // 2. Chained call: Replace synchronous call() with streaming stream()
            return
                    streamTestChatClient.getChatClient()
                    // mainLlmClient.getClient()
                    .prompt()
                    .user(msg)
                    .advisors(advisorSpec -> {
                        advisorSpec.advisors(advisors); // 复用原有Advisor数组 (Reuse the original Advisor array)
                        advisorSpec.param("userId", userId);
                        advisorSpec.param("sessionUuid", finalSessionUuid); // sessionId → sessionUuid
                        advisorSpec.param("promptTemplate", promptTemplate);
                    })
                    .stream() // 核心修改：同步call() → 流式stream() (Core modification: synchronous call() → streaming stream())
                    .content() // 提取流式的文本内容，返回Flux<String> (Extract streaming text content, return Flux<String>)

                    .doOnError(e -> log.error("流式调用AI模型[{}]失败，会话UUID：{}，消息：{}",
                            logTag, sessionUuid, msg, e))
                    .onErrorMap(e -> new RuntimeException("AI流式聊天接口[" + logTag + "]调用失败", e));

        } catch (Exception e) {
            // 捕获初始化阶段的异常（如模板读取失败），转为Flux错误信号
            // Catch exceptions during initialization (e.g., template read failure) and convert to Flux error signal
            log.error("初始化流式AI调用[{}]失败，会话UUID：{}，消息：{}",
                    logTag, sessionUuid, msg, e);
            return Flux.error(new RuntimeException("AI流式聊天接口[" + logTag + "]初始化失败", e));
        }
    }

    /**
     * 流式 AI 调用执行器（最终版：返回前端ChatChunkDTO流 + 首帧携带Meta元数据），解决空首帧BUG
     */
    private Flux<ChatChunkDTO> executeChatStreamWithContext(AiChatDTO aiChatDTO, String templateFile, List<Advisor> advisors, String logTag) {
        try {
            String respSessionUuid = aiChatDTO.getSessionUuid();
            String taskId = aiChatDTO.getTaskId();
            ChatInformationDTO chatInformationDTO = buildChatInformationDTO(aiChatDTO, respSessionUuid);

            AtomicReference<ChatStreamMetaDTO> metaDTORef = new AtomicReference<>();
            AtomicBoolean isFirstChunk = new AtomicBoolean(true);

            return streamTestChatClient.getChatClient()
                    .prompt()
                    .user(chatInformationDTO.getUserMessage())
                    .advisors(advisorSpec -> {
                        advisorSpec.advisors(advisors);
                        advisorSpec.param(ChatInformationDTO.CHAT_INFORMATION_DTO_KEY, chatInformationDTO);
                    })
                    .stream()
                    .chatClientResponse()
                    .doOnNext(response -> {
                        ChatInformationDTO currentDto = ChatInformationDTO.getFromContext(response.context());
                        if (currentDto == null) {
                            return;
                        }
                        ChatStreamMetaDTO metaDTO = new ChatStreamMetaDTO();
                        BeanUtils.copyProperties(currentDto, metaDTO);
                        metaDTORef.set(metaDTO);
                    })
                    .doOnComplete(() -> log.debug("[{}] 流式调用完成", logTag))
                    .map(response -> {
                        String aiFragment = response.chatResponse().getResult().getOutput().getText();
                        String content = (aiFragment != null) ? aiFragment : "";
                        return Tuples.of(content, isFirstChunk.get());
                    })
                    .filter(tuple -> tuple.getT2() || !tuple.getT1().isBlank())
                    .map(tuple -> {
                        String content = tuple.getT1();
                        boolean first = isFirstChunk.compareAndSet(true, false);

                        Object sendContent;
                        if (first) {
                            sendContent = metaDTORef.get();
                        } else {
                            ChatStreamContentMetaVO metaVO = new ChatStreamContentMetaVO();
                            metaVO.setTaskId(taskId);
                            metaVO.setAiReplyContent(content);
                            sendContent = metaVO;
                        }
                        return new ChatChunkDTO(respSessionUuid, sendContent, first, false);
                    })
                    // 公共结束包（只写一次）
                    .concatWith(buildEndChunk(respSessionUuid, taskId))

                    // 🔥 优化：异常处理无重复代码
                    // 🔥 修复：捕获包装后的异常，判断根因是否为Token不足
                    .onErrorResume(e -> {
                        // 1. 判断根因是不是 TokenInsufficientException
                        if (e.getCause() instanceof TokenInsufficientException tokenEx) {
                            log.error("流式调用失败[{}] - Token不足: {}", logTag, tokenEx.getMessage());
                            return buildErrorResponse(respSessionUuid, taskId, tokenEx.getClass().getSimpleName(), tokenEx.getMessage());
                        }
                        // 2. 其他所有异常：走未知异常
                        try {
                            throw e;
                        } catch (Throwable ex) {
                            throw new RuntimeException(ex);
                        }
                    })
                    // 原有未知异常处理（保持不变）
                    .onErrorResume(Exception.class, e -> {
                        log.error("流式调用失败[{}] - 未知异常", logTag, e);
                        return buildErrorResponse(respSessionUuid, taskId, e.getClass().getSimpleName(), "AI服务异常，请稍后重试")
                                .thenMany(Flux.error(new RuntimeException("AI流式调用失败", e)));
                    })
                    .doOnError(e -> log.error("流式转换失败[{}]", logTag, e));

        } catch (Exception e) {
            log.error("初始化失败[{}]", logTag, e);
            return Flux.error(new RuntimeException("初始化失败", e));
        }
    }

    // ===================== 抽离公共方法：消除重复 =====================
    /**
     * 构建统一的【结束包】
     */
    private Flux<ChatChunkDTO> buildEndChunk(String sessionUuid, String taskId) {
        return Flux.just(new ChatChunkDTO(sessionUuid, taskId, false, true));
    }

    /**
     * 构建统一的【错误响应包】：错误块 + 结束块
     */
    private Flux<ChatChunkDTO> buildErrorResponse(String sessionUuid, String taskId, String errorType, String errorMsg) {
        // 1. 构建错误VO
        ChatStreamErrorMetaVO errorVO = new ChatStreamErrorMetaVO();
        errorVO.setTaskId(taskId);
        errorVO.setErrorType(errorType);
        errorVO.setErrorMsg(errorMsg);

        // 2. 构建错误Chunk
        ChatChunkDTO errorChunk = new ChatChunkDTO();
        errorChunk.setSessionUuid(sessionUuid);
        errorChunk.setContent(errorVO);
        errorChunk.setIsFirst(false);
        errorChunk.setIsEnd(false);
        errorChunk.setError(true);

        // 3. 错误块 + 结束块
        return Flux.just(errorChunk)
                .concatWith(buildEndChunk(sessionUuid, taskId));
    }

    // ==================== ✅ 抽离的私有方法：专注构建 ChatInformationDTO ====================
    private ChatInformationDTO buildChatInformationDTO(AiChatDTO aiChatDTO,
                                                       String sessionUuid) {
        ChatInformationDTO dto = new ChatInformationDTO();
        // 基础用户/会话信息
        dto.setUserId(aiChatDTO.getUserId());
        dto.setSessionUuid(sessionUuid);
        dto.setTaskId(aiChatDTO.getTaskId());
        dto.setRoleId(aiChatDTO.getRoleId());

        // 提示词信息
        dto.setUserMessage(aiChatDTO.getMessage());

        // 消息时间/ID信息
        dto.setUserSendTimestamp(aiChatDTO.getUserSendTimestamp());
        dto.setUserMessageId(aiChatDTO.getUserMessageId());

        // 标记主动信息
        dto.setIsActiveMessage(aiChatDTO.getIsActiveMessage());

        // 输入输出类型
        dto.setInputTypeId(streamTestChatClient.getInputTypeId());
        dto.setOutputTypeId(streamTestChatClient.getOutputTypeId());

        return dto;
    }

}