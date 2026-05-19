package org.myfx.controls.aione.AiService.aiClient.advisor.test;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * 直接实现顶级接口 CallAdvisor + StreamAdvisor
 * 功能：拦截AI请求，直接返回伪造回复，不调用真实AI、不抛出异常
 */
@Component
public class FakeResponseDirectAdvisor implements CallAdvisor, StreamAdvisor {

    // 自定义伪造的回复内容
    private final String fakeReply;

    // 默认构造：使用默认回复
    public FakeResponseDirectAdvisor() {
        this("请求已拦截，返回预设伪造AI回复");
    }

    // 自定义伪造回复
    public FakeResponseDirectAdvisor(String fakeReply) {
        Assert.hasText(fakeReply, "伪造回复内容不能为空");
        this.fakeReply = fakeReply;
    }

    // --------------------- 同步调用拦截：直接返回伪造响应 ---------------------
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        Assert.notNull(chatClientRequest, "请求不能为空");
        Assert.notNull(callAdvisorChain, "责任链不能为空");

        // 核心：跳过真实AI调用（不执行 callAdvisorChain.nextCall()）
        // 直接构造并返回伪造响应
        return buildFakeResponse(chatClientRequest);
    }

    // --------------------- 流式调用拦截：直接返回伪造响应流 ---------------------
    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        Assert.notNull(chatClientRequest, "请求不能为空");
        Assert.notNull(streamAdvisorChain, "责任链不能为空");

        // 核心：跳过真实AI流调用（不执行 streamAdvisorChain.nextStream()）
        // 直接返回包含伪造响应的Flux
        return Flux.just(buildFakeResponse(chatClientRequest));
    }

    // --------------------- Advisor 接口必须实现：获取名称 ---------------------
    @Override
    public String getName() {
        return "FakeResponseDirectAdvisor";
    }

    // --------------------- 工具方法：构造标准伪造AI响应 ---------------------
    private ChatClientResponse buildFakeResponse(@NonNull ChatClientRequest request) {
        // 1. 构建伪造的对话生成内容
        // 1. 构建 AssistantMessage（核心修正）
        AssistantMessage assistantMessage = new AssistantMessage(fakeReply);

        // 2. 构建 Generation（仅接收 AssistantMessage + 元数据，匹配源码）
        Generation generation = new Generation(
                assistantMessage,
                ChatGenerationMetadata.builder().finishReason("stop").build()
        );


        // 2. 构建伪造的AI响应
        ChatResponse chatResponse = new ChatResponse(
                List.of(generation),
                ChatResponseMetadata.builder().model("fake-ai").build()
        );

        // 3. 封装为框架标准的 ChatClientResponse
        return ChatClientResponse.builder()
                .chatResponse(chatResponse)
                .context(request.context())
                .build();
    }

    @Override
    public int getOrder() {
        return 4;
    }
}