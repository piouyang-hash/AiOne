package org.myfx.controls.aione.AiService.aiClient.advisor;

import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.aiClient.advisor.dto.ChatInformationDTO;
import org.myfx.controls.aione.AiService.utils.TextSplitterUtils;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;


/**
 * 智能文本切分Advisor（MySmartSplitterAdvisor）
 * <p>
 * 核心能力：
 * 1. 对ChatClient流式返回的文本进行智能切分，按指定最小长度切分文本并移除标点符号；
 * 2. 实时同步原始带标点文本、切分后的文本列表JSON到请求上下文（Context），便于下游逻辑复用；
 * 3. 处理用户取消请求、流正常终止等场景，触发最终的数据存储逻辑；
 * 4. 全程生成短traceId（6位UUID）用于日志追踪，提升问题排查效率。
 * <p>
 * order（执行顺序，值越小执行越早）：60（该Advisor的before执行较晚，输出会较早执行，处理ai的回复，确保上游基础逻辑（如统计token）完成后再做文本切分）
 */
@Slf4j
public class MySmartSplitterAdvisor implements BaseAdvisor {

    private final int minLength;

    public MySmartSplitterAdvisor(int minLength) {
        this.minLength = minLength;
    }

    // 这段注释完全看不懂，所以留下吧
    // 3. 重新包装回 ChatClientResponse（这一步需要拿到原始 response 的 context，所以稍作处理）
    // 为了简单且保留 Context，我们通常会在原始流上用 zip 或 switchMap，
    // 但最简单的办法是在 splitStream 时把对象一起带进去。
    @Override
    @NonNull
    public Flux<ChatClientResponse> adviseStream(@NonNull ChatClientRequest request, @NonNull StreamAdvisorChain chain) {
        // 一行获取DTO + 强制非空校验
        ChatInformationDTO chatInfoDTO = ChatInformationDTO.getFromContext(request.context());
        Assert.notNull(chatInfoDTO, "ChatInformationDTO 不能为空");

        String traceId = UUID.randomUUID().toString().substring(0, 6);
        // 【修正】存储 内容+时间戳
        List<Map<String, Object>> allSentencesWithTime = new ArrayList<>();
        StringBuilder rawBuffer = new StringBuilder();
        AtomicReference<ChatClientResponse> lastResponseRef = new AtomicReference<>();
        AtomicInteger stopState = new AtomicInteger(0);

        return Flux.defer(() -> chain.nextStream(request)
                .doOnNext(res -> {
                    if (stopState.get() < 2) {
                        lastResponseRef.set(res);
                        String text = res.chatResponse().getResult().getOutput().getText();
                        if (text != null) {
                            rawBuffer.append(text);
                        }
                    }
                })
                .doOnCancel(() -> {
                    log.info("[{}] 用户取消，准备补完最后一节...", traceId);
                    stopState.compareAndSet(0, 1);
                })
                .transform(upstream -> {
                    Flux<String> stringFlux = upstream
                            .mapNotNull(res -> res.chatResponse().getResult().getOutput().getText());
                    return TextSplitterUtils.splitStream(stringFlux, this.minLength);
                })
                .filter(sentence -> stopState.get() < 2)
                .doOnNext(sentence -> {
                    // ===================== 核心：生成实时时间戳 =====================
                    long createTime = System.currentTimeMillis();
                    Map<String, Object> sentenceWithTime = new HashMap<>();
                    sentenceWithTime.put("content", sentence);
                    sentenceWithTime.put("timestamp", createTime);
                    allSentencesWithTime.add(sentenceWithTime);

                    // 同步到DTO（不变）
                    chatInfoDTO.setAiReplyContent(sentence);

                    // 生成带时间戳的JSON
                    String json = TextSplitterUtils.generateJsonFromList(allSentencesWithTime);
                    log.info("[{}] 【JSON生成】带时间戳完整列表 | 列表大小: {} | JSON: {}",
                            traceId, allSentencesWithTime.size(), json);
                    chatInfoDTO.setSplitContentJson(json);

                    if (stopState.get() == 1) {
                        stopState.set(2);
                    }
                })
                .map(sentence -> copyResponseWithNewContent(lastResponseRef.get(), sentence))
                .takeUntil(res -> stopState.get() == 2)
                // 【修正】收尾方法传入正确的列表
                .doOnTerminate(() -> {
                    handleFinalStorage(request, chatInfoDTO, allSentencesWithTime, rawBuffer, SignalType.ON_COMPLETE, traceId);
                })
                .doOnCancel(() -> {
                    handleFinalStorage(request, chatInfoDTO, allSentencesWithTime, rawBuffer, SignalType.CANCEL, traceId);
                }));
    }

    /**
     * 核心收尾逻辑：确保数据完整性并存入 DTO
     * 【适配修改】支持带时间戳的分段数据，兼容正常终止/用户取消中断
     */
    private void handleFinalStorage(ChatClientRequest request, ChatInformationDTO chatInfoDTO,
                                    // 【关键修改】替换为带时间戳的列表
                                    List<Map<String, Object>> allSentencesWithTime,
                                    StringBuilder rawBuffer,
                                    SignalType signalType, String traceId) {

        // 【中断兜底】用户取消 + 句子列表为空 + 原始缓冲区有数据 → 补全最后一段（带时间戳）
        if (signalType == SignalType.CANCEL && allSentencesWithTime.isEmpty() && !rawBuffer.isEmpty()) {
            // 清理文本
            String cleanContent = TextSplitterUtils.cleanSingleSentence(rawBuffer.toString());
            // 绑定兜底文本的时间戳（中断时的实时时间）
            Map<String, Object> sentenceWithTime = new HashMap<>();
            sentenceWithTime.put("content", cleanContent);
            sentenceWithTime.put("timestamp", System.currentTimeMillis());
            allSentencesWithTime.add(sentenceWithTime);
        }

        // 【修改】判断带时间戳的列表是否为空
        if (!allSentencesWithTime.isEmpty()) {
            // 【关键修改】调用新版工具方法，生成带时间戳的JSON
            String json = TextSplitterUtils.generateJsonFromList(allSentencesWithTime);
            // 最终切分JSON（带时间戳）存入DTO
            chatInfoDTO.setSplitContentJson(json);
            // 最终原文存入DTO（不变）
            chatInfoDTO.setAiReplyContent(rawBuffer.toString());

            log.info("[{}] 最终收割完成. 信号: {}, 句子数: {}", traceId, signalType, allSentencesWithTime.size());
        }
    }

    private ChatClientResponse copyResponseWithNewContent(ChatClientResponse original, String newContent) {
        Generation generation = new Generation(new AssistantMessage(newContent));
        ChatResponse chatResponse = new ChatResponse(List.of(generation));

        if (original != null) {
            return original.mutate().chatResponse(chatResponse).build();
        }
        return ChatClientResponse.builder()
                .chatResponse(chatResponse)
                .build();
    }

    @Override
    @NonNull
    public String getName() { return "MySmartSplitterAdvisor"; }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        return null;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        return null;
    }

    @Override
    public int getOrder() { return 60; }


    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        return null;
    }
}