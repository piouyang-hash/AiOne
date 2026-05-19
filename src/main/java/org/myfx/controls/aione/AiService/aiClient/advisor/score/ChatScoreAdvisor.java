package org.myfx.controls.aione.AiService.aiClient.advisor.score;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.aiClient.advisor.dto.ChatInformationDTO;
import org.myfx.controls.aione.AiService.service.base.status.UserAiBehaviorScoreSyncService;
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

    /**
     * 前置逻辑处理（抽离复用，同步/流式场景共用）
     */
    private ChatClientRequest handleBeforeLogic(ChatClientRequest request) {
        // 原before逻辑：无前置处理，直接返回原请求
        return request;
    }

    /**
     * 后置逻辑处理（抽离复用，同步/流式场景共用）
     * 简化：仅调用封装的事务方法，业务逻辑全部下沉
     */
    private void handleAfterLogic(Map<String, Object> adviseContext) {
        // 1. 获取并校验聊天信息DTO
        ChatInformationDTO chatInfoDTO = ChatInformationDTO.getFromContext(adviseContext);
        Assert.notNull(chatInfoDTO, "ChatInformationDTO 不能为空");

        // 2. 获取用户ID
        Integer userId = chatInfoDTO.getUserId();

        // 3. 用户ID为空，直接跳过
        if (userId == null) {
            log.warn("聊天后置处理：用户ID为空，跳过分值调整逻辑");
            return;
        }

        try {
            // ===================== 核心：调用封装的事务方法 =====================
            userAiBehaviorScoreSyncService.syncUserSendMsgScore(userId);
        } catch (Exception e) {
            // 仅打印异常日志，不影响核心聊天主流程
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