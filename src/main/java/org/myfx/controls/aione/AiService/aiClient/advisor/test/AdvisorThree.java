package org.myfx.controls.aione.AiService.aiClient.advisor.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;

@Component
@Slf4j
public class AdvisorThree implements BaseAdvisor {

    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
        log.info("========== [AdvisorThree (order=3)] before 执行 ==========");
        return request;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
        log.info("========== [AdvisorThree (order=3)] after 执行 ==========");
        return response;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        log.info("========== [AdvisorThree (order=3)] adviseStream 开始执行 ==========");

        // 手动调用before
        ChatClientRequest processedRequest = before(request, chain);

        // 2. 核心修改：替换doAfterTerminate为doFinally，处理SignalType参数
        return chain.nextStream(processedRequest)
                // doFinally接收SignalType参数（表示流终止的原因）
                .doOnTerminate(() -> {
                    // 这个逻辑会在信号传给下游（下一个 Advisor）之前执行
                    after(null, null);
                    log.info("========== [AdvisorThree (order=3)] adviseStream 结束，触发 after ==========");
                });
    }

    @Override
    public String getName() {
        return "AdvisorThree";
    }

    @Override
    public int getOrder() {
        return 3; // 优先级最低，最后执行
    }
}