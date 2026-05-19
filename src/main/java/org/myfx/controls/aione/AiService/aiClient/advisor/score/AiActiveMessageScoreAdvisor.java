package org.myfx.controls.aione.AiService.aiClient.advisor.score;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.aiClient.advisor.dto.ChatInformationDTO;
import org.myfx.controls.aione.AiService.service.base.status.ai_behavior_db.upper.AiBehaviorImpactScoreService;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * AI主动消息分值Advisor（AI Active Message Score Advisor）
 * 核心能力：AI主动发送消息后，仅执行主动消息分值更新
 * order（越小, before执行越早）：70
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AiActiveMessageScoreAdvisor implements BaseAdvisor {

    // 仅注入需要的AI行为分值处理服务
    private final AiBehaviorImpactScoreService aiBehaviorImpactScoreService;

    /**
     * 前置逻辑处理（抽离复用，同步/流式场景共用）
     */
    private ChatClientRequest handleBeforeLogic(ChatClientRequest request) {
        // 无前置处理，直接返回原请求
        return request;
    }

    /**
     * 后置逻辑处理（仅调用 AI主动发送消息 分值方法）
     */
    private void handleAfterLogic(Map<String, Object> adviseContext) {
        // 一行获取DTO + 强制非空校验（必须传入DTO，否则直接抛出异常）
        ChatInformationDTO chatInfoDTO = ChatInformationDTO.getFromContext(adviseContext);
        Assert.notNull(chatInfoDTO, "ChatInformationDTO 不能为空");

        // 从DTO直接取值
        Integer userId = chatInfoDTO.getUserId();
        Long sessionId = chatInfoDTO.getSessionId();

        // 2. 判空保护
        if (userId == null || sessionId == null) {
            log.warn("AI主动消息后置处理：用户ID/会话ID为空，跳过分值调整逻辑（userId={}, sessionId={}）", userId, sessionId);
            return;
        }

        try {
            // ====================== 核心：仅调用AI主动发送消息分值方法 ======================
            aiBehaviorImpactScoreService.handleAiActiveSendMsgBehavior(userId);

            log.info("AI主动消息后置处理：用户ID={}，已完成主动消息分值更新", userId);
        } catch (Exception e) {
            // 异常兜底，不影响主流程
            log.error("用户{} AI主动消息分值处理执行异常", userId, e);
        }
    }

    /**
     * 同步场景：后置逻辑
     */
    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        handleAfterLogic(chatClientResponse.context());
        return chatClientResponse;
    }

    /**
     * 同步场景：前置逻辑
     */
    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        return handleBeforeLogic(chatClientRequest);
    }

    /**
     * 流式场景：流结束后执行分值处理
     */
    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        ChatClientRequest augmentedRequest = handleBeforeLogic(request);

        return chain.nextStream(augmentedRequest)
                .doAfterTerminate(() -> {
                    handleAfterLogic(request.context());
                    log.info("[AiActiveMessageScoreAdvisor] 流式主动对话结束，已执行AI主动消息分值调整（userId={}, sessionId={}）",
                            request.context().get("userId"), request.context().get("sessionId"));
                })
                .doOnError(e -> {
                    log.error("[AiActiveMessageScoreAdvisor] 流式主动对话异常，执行分值兜底逻辑", e);
                    handleAfterLogic(request.context());
                });
    }

    /**
     * 自定义Advisor名称
     */
    @Override
    public String getName() {
        return "AiActiveMessageScoreAdvisor";
    }

    /**
     * 执行顺序：与原Advisor保持一致
     */
    @Override
    public int getOrder() {
        return 70;
    }
}