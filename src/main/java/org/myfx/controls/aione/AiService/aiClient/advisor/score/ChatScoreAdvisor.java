package org.myfx.controls.aione.AiService.aiClient.advisor.score;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.aiClient.advisor.dto.ChatInformationDTO;
import org.myfx.controls.aione.AiService.service.base.status.UserAiBehaviorScoreSyncService;
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
 * 聊天分值Advisor（Chat Score Advisor）
 * 核心能力：对话完成后计算/更新用户聊天行为分值
 * order（越小执行最早）：70
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChatScoreAdvisor implements BaseAdvisor {

    // ===================== 注入封装好的 双向行为分值同步事务服务 =====================
    private final UserAiBehaviorScoreSyncService userAiBehaviorScoreSyncService;
    private final AiBehaviorImpactScoreService aiBehaviorImpactScoreService;

    /**
     * 前置逻辑处理（抽离复用，同步/流式场景共用）
     */
    private ChatClientRequest handleBeforeLogic(ChatClientRequest request) {
        // 原before逻辑：无前置处理，直接返回原请求
        return request;
    }

    /**
     * 后置逻辑处理（抽离复用，同步/流式场景共用）
     * 根据 isActiveMessage 区分：AI主动消息 / 用户发送消息，执行不同的分值处理逻辑
     */
    private void handleAfterLogic(Map<String, Object> adviseContext) {
        // 1. 强制获取并校验聊天信息DTO（为空直接抛异常，保证后续逻辑安全）
        ChatInformationDTO chatInfoDTO = ChatInformationDTO.getFromContext(adviseContext);
        Assert.notNull(chatInfoDTO, "ChatInformationDTO 不能为空");

        // 2. 从DTO中提取核心参数
        Integer userId = chatInfoDTO.getUserId();
        Long sessionId = chatInfoDTO.getSessionId();
        // 🔥 关键：获取是否为AI主动消息的标识
        Boolean isActiveMessage = chatInfoDTO.getIsActiveMessage();

        // 3. 基础参数判空保护，空则跳过
        if (userId == null || sessionId == null || isActiveMessage == null) {
            log.warn("聊天后置处理：参数不完整，跳过分值调整逻辑（userId={}, sessionId={}, isActiveMessage={}）",
                    userId, sessionId, isActiveMessage);
            return;
        }

        try {
            // 4. 🔥 核心分支：根据消息类型执行不同的分值逻辑
            if (Boolean.TRUE.equals(isActiveMessage)) {
                // AI主动发送消息 → 执行主动消息分值逻辑
                aiBehaviorImpactScoreService.handleAiActiveSendMsgBehavior(userId);
                log.info("聊天后置处理：用户ID={}，执行【AI主动消息】分值更新完成", userId);
            } else {
                // 用户主动发送消息 → 执行用户消息分值逻辑
                userAiBehaviorScoreSyncService.syncUserSendMsgScore(userId);
                log.info("聊天后置处理：用户ID={}，执行【用户发送消息】分值更新完成", userId);
            }
        } catch (Exception e) {
            // 异常兜底：只打印日志，绝对不影响核心聊天主流程
            log.error("用户[{}]聊天后置分值处理执行异常", userId, e);
        }
    }

    /**
     * 同步场景：后置逻辑（复用handleAfterLogic）
     */
    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        handleAfterLogic(chatClientResponse.context());
        return chatClientResponse;
    }

    /**
     * 同步场景：前置逻辑（复用handleBeforeLogic）
     */
    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        return handleBeforeLogic(chatClientRequest);
    }

    /**
     * 流式场景：显式实现adviseStream，确保后置逻辑执行
     * 【统一改造】对齐上方逻辑：doOnTerminate + doOnCancel
     */
    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        // 1. 前置逻辑：复用同步场景的前置处理（保持不变）
        ChatClientRequest augmentedRequest = handleBeforeLogic(request);

        // 2. 流式处理：链式调用 + 统一终止/取消钩子（和上方代码完全一致）
        return chain.nextStream(augmentedRequest)
                // 覆盖：正常完成、异常终止
                .doOnTerminate(() -> {
                    handleAfterLogic(request.context());
                    log.info("[ChatScoreAdvisor] 流式对话结束，已执行用户/AI分值调整逻辑（userId={}, sessionId={}）",
                            request.context().get("userId"), request.context().get("sessionId"));
                })
                // 覆盖：流被主动取消（新消息打断、手动取消）
                .doOnCancel(() -> {
                    handleAfterLogic(request.context());
                    log.info("[ChatScoreAdvisor] 流式对话已取消，已执行用户/AI分值调整逻辑（userId={}, sessionId={}）",
                            request.context().get("userId"), request.context().get("sessionId"));
                });
    }

    /**
     * 自定义Advisor名称（英文，便于日志/调试识别）
     */
    @Override
    public String getName() {
        return "ChatScoreAdvisor";
    }

    /**
     * 执行顺序：设为70，在存储消息Advisor之后执行（可选，也可按业务调整）
     */
    @Override
    public int getOrder() {
        return 70;
    }
}