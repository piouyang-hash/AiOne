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
public class AdvisorOne implements BaseAdvisor {

    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
        // 核心：打印before执行日志，标记order和名称
        log.info("========== [AdvisorOne (order=1)] before 执行 ==========");
        // 无业务逻辑，直接返回原请求
        return request;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
        // 核心：打印after执行日志，标记order和名称
        log.info("========== [AdvisorOne (order=1)] after 执行 ==========");
        // 无业务逻辑，直接返回原响应
        return response;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        log.info("========== [AdvisorOne (order=1)] adviseStream 开始执行 ==========");

        // 手动调用before（流式场景不会自动触发）
        ChatClientRequest processedRequest = before(request, chain);

        // 2. 核心修改：替换doAfterTerminate为doFinally，处理SignalType参数
        return chain.nextStream(processedRequest)
                // doFinally接收SignalType参数（表示流终止的原因）
                // 使用 doOnTerminate 替换 doFinally
                .doOnTerminate(() -> {
                    // 这个逻辑会在信号传给下游（下一个 Advisor）之前执行
                    after(null, null);
                    log.info("========== [AdvisorOne (order=1)] adviseStream 结束，触发 after ==========");
                });
    }

    @Override
    public String getName() {
        return "AdvisorOne";
    }

    @Override
    public int getOrder() {
        return 1; // 优先级最高，最先执行
    }
}