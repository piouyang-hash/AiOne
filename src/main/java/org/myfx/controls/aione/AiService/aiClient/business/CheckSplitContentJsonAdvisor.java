package org.myfx.controls.aione.AiService.aiClient.business;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@Slf4j
public class CheckSplitContentJsonAdvisor implements StreamAdvisor {

    @Override
    @NonNull
    public Flux<ChatClientResponse> adviseStream(@NonNull ChatClientRequest request, @NonNull StreamAdvisorChain chain) {
        // 1. 先调用 chain.nextStream(request)
        // 这会触发下游所有的 Advisor（包括 MySmartSplitter）
        return chain.nextStream(request)
                // 2. 使用 doOnTerminate 而不是 doAfterTerminate
                // 或者干脆用 transformDeferred 确保在流的最后阶段
                .doOnTerminate(() -> {
                    // 当流流到这里时，说明内部的 MySmartSplitter 已经完成了它的 doOnComplete
                    Object json = request.context().get("splitContentJson");
                    if (json == null) {
                        log.warn("[Check] ❌ 依然没找到！Context 只有: {}", request.context().keySet());
                    } else {
                        log.info("[Check] ✅ 检查通过，JSON 为: {}", json);
                    }
                });
    }

    @Override
    public int getOrder() {
        // 重要：Order 越小，在链条中越靠“外”。
        // 我们希望 Check 在最外面包裹着 Splitter，这样 Splitter 结束了，Check 才会触发 terminate
        return -100;
    }

    @Override
    public String getName() {
        return "CheckSplitContentJsonAdvisor";
    }
}