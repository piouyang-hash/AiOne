package org.myfx.controls.aione.AiService.aiClient.advisor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.aiClient.SpringAiPromptConvertUtil;
import org.myfx.controls.aione.AiService.aiClient.advisor.dto.ChatInformationDTO;
import org.myfx.controls.aione.AiService.engineering.summary_sliding_window.SummarySlidingWindowAopEnhanceService;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiChatMessage;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

import java.util.List;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import java.util.Map;

/**
 * 总结型滑动窗口Advisor
 * 核心能力：
 * 1. 同步场景：前置加载历史对话填充模板，后置执行滑动窗口总结
 * 2. 流式场景：前置加载历史对话填充模板，流式结束后异步执行滑动窗口总结
 * order（越小执行最早）：20（需小于会话存储Advisor的order，确保消息先入库再总结）
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SummarySlidingWindowAdvisor implements BaseAdvisor {

    private final SummarySlidingWindowAopEnhanceService summarySlidingWindowAopEnhanceService;

    // --- 1. 同步逻辑：保留原有代码，复用抽离的核心方法 ---
    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        return handleBeforeLogic(chatClientRequest); // 复用前置核心逻辑
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        handleAfterLogic(chatClientResponse.context()); // 复用后置核心逻辑
        return chatClientResponse;
    }

    // --- 2. 流式逻辑：全新升级，彻底取消流式下的 sync 阻塞 ---
    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        // 1. Before 逻辑：保持原样
        ChatClientRequest augmentedRequest = handleBeforeLogic(request);

        return chain.nextStream(augmentedRequest)
                // 2. 使用 doOnTerminate 拦截正常完成和错误信号
                .doOnTerminate(() -> {
                    // 执行总结逻辑
                    handleAfterLogic(request.context());
                })
                // 3. 使用 doOnCancel 拦截用户取消信号
                .doOnCancel(() -> {
                    handleAfterLogic(request.context());
                });
    }

    // --- 3. 抽离出的核心业务逻辑 (复用) ---
    private ChatClientRequest handleBeforeLogic(ChatClientRequest request) {
        // 一行获取DTO + 强制非空校验（必须传入DTO，否则直接抛出异常）
        ChatInformationDTO chatInfoDTO = ChatInformationDTO.getFromContext(request.context());
        Assert.notNull(chatInfoDTO, "ChatInformationDTO 不能为空");

        // 从DTO直接取值
        Integer userId = chatInfoDTO.getUserId();
        Long sessionId = chatInfoDTO.getSessionId();

        // 核心业务逻辑：仅用户ID和会话ID都存在时处理
        if (userId != null && sessionId != null) {
            // 1. 加载历史对话消息 → 直接赋值 List<Message> 类型字段
            List<AiChatMessage> aiChatMessageList = summarySlidingWindowAopEnhanceService.preLoadConversationHistory(userId, sessionId);
            List<Message> historyMessagesFormatted = null;
            if (!CollectionUtils.isEmpty(aiChatMessageList)) {
                // 直接转换为 List<Message>，不再转字符串，完全匹配字段类型
                historyMessagesFormatted = SpringAiPromptConvertUtil.convertToMessageList(aiChatMessageList);
            }
            // 赋值到DTO字段
            chatInfoDTO.setHistoryMessagesFormatted(historyMessagesFormatted);

            // 2. 加载历史对话摘要 → 直接赋值 String 类型字段
            String historySummary = summarySlidingWindowAopEnhanceService.preGetLatestHistorySummary(userId, sessionId);
            // 空值处理：无摘要则赋值null
            chatInfoDTO.setHistorySummary(StringUtils.hasText(historySummary) ? null : historySummary);
            log.debug("已加载历史对话背景摘要");
        }

        return request;
    }

    private void handleAfterLogic(Map<String, Object> context) {
        // 一行获取DTO + 强制非空校验
        ChatInformationDTO chatInfoDTO = ChatInformationDTO.getFromContext(context);
        Assert.notNull(chatInfoDTO, "ChatInformationDTO 不能为空");

        // 从DTO直接取值
        Integer userId = chatInfoDTO.getUserId();
        Long sessionId = chatInfoDTO.getSessionId();

        // 核心后置业务逻辑（不变）
        if (userId != null && sessionId != null) {
            summarySlidingWindowAopEnhanceService.postSummaryConversationContext(userId, sessionId);
        } else {
            log.warn("[SummaryAdvisor] 滑动窗口总结跳过：userId或sessionId为空（userId={}, sessionId={}）", userId, sessionId);
        }
    }

    /**
     * 自定义Advisor名称（方便日志/调试识别）
     */
    @Override
    public String getName() {
        return "SummarySlidingWindowAdvisor";
    }

    /**
     * 执行顺序（数值越小越先执行，按需调整）
     * 建议：比会话存储Advisor先执行（如order=20）
     */
    @Override
    public int getOrder() {
        return 20;
    }
}