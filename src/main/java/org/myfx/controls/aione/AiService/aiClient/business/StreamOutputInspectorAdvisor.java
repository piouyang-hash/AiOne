package org.myfx.controls.aione.AiService.aiClient.business;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;

import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 流量监控 Advisor：检查输出是逐字(Token-by-Token)还是逐句(Sentence-by-Sentence)
 */
@Component
public class StreamOutputInspectorAdvisor implements StreamAdvisor {

    private static final Logger log = LoggerFactory.getLogger(StreamOutputInspectorAdvisor.class);

    @Override
    public int getOrder() {
        return -1; // 确保在你的 TextSplitterAdvisor 之后执行
    }

    @Override
    @NonNull
    public Flux<ChatClientResponse> adviseStream(@NonNull ChatClientRequest request, @NonNull StreamAdvisorChain chain) {
        AtomicInteger chunkCount = new AtomicInteger(0);

        return chain.nextStream(request)
                .doOnNext(response -> {
                    String content = response.chatResponse().getResult().getOutput().getText();
                    if (content == null || content.isEmpty()) return;

                    int count = chunkCount.incrementAndGet();
                    int length = content.length();

                    // 核心逻辑判断
                    String type = (length > 1) ? "【逐句/分段】" : "【逐字】";

                    log.info("第 {} 次回调 | 长度: {} | 模式: {} | 内容: {}",
                            String.format("%02d", count),
                            String.format("%2d", length),
                            type,
                            content.replace("\n", "\\n"));
                })
                .doFinally(signal -> log.info("流输出结束，信号类型: {}", signal));
    }

    @Override
    public String getName() {
        return "StreamOutputInspector";
    }
}